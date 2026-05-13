package com.tokendad.nesventory.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.tokendad.nesventory.data.local.SyncEntityType
import com.tokendad.nesventory.data.local.SyncOperation
import com.tokendad.nesventory.data.local.SyncQueueDao
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.remote.NesVentoryApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.util.UUID

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val preferencesManager: PreferencesManager,
    private val api: NesVentoryApi
) : CoroutineWorker(appContext, workerParams) {
    private val gson = Gson()

    private data class DeleteSyncPayload(val profileId: String? = null)

    override suspend fun doWork(): Result {
        val activeProfileId = preferencesManager.serverProfiles.first().activeProfileId
        val pending = syncQueueDao.getPending()
        var hasRetryableFailure = false

        pending.forEach { entry ->
            val shouldDeleteQueueEntry = try {
                if (!isMatchingProfile(entry.payload, activeProfileId)) {
                    false
                } else {
                when {
                    entry.operation == SyncOperation.DELETE && entry.entityType == SyncEntityType.ITEM -> {
                        api.deleteItem(UUID.fromString(entry.entityId))
                        true
                    }
                    entry.operation == SyncOperation.DELETE && entry.entityType == SyncEntityType.LOCATION -> {
                        api.deleteLocation(UUID.fromString(entry.entityId))
                        true
                    }
                    else -> true // Drop unknown/unsupported entries so they don't block the queue.
                }
                }
            } catch (http: HttpException) {
                if (http.code() == 404 || http.code() == 410) {
                    true // Already gone remotely; treat as idempotent success.
                } else {
                    hasRetryableFailure = true
                    false
                }
            } catch (_: IllegalArgumentException) {
                true // Bad UUID payload; drop corrupted queue entry.
            } catch (_: Exception) {
                hasRetryableFailure = true
                false
            }

            if (shouldDeleteQueueEntry) {
                syncQueueDao.deleteById(entry.id)
            }
        }

        return if (hasRetryableFailure) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    private fun isMatchingProfile(payload: String, activeProfileId: String?): Boolean {
        if (payload.isBlank()) return true
        val payloadProfileId = runCatching {
            gson.fromJson(payload, DeleteSyncPayload::class.java).profileId
        }.getOrNull()
        if (payloadProfileId.isNullOrBlank()) return true
        return payloadProfileId == activeProfileId
    }
}
