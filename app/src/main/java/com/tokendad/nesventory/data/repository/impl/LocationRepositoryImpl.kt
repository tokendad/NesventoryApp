package com.tokendad.nesventory.data.repository.impl

import com.google.gson.Gson
import com.tokendad.nesventory.data.local.LocationDao
import com.tokendad.nesventory.data.local.LocationEntity
import com.tokendad.nesventory.data.local.SyncEntityType
import com.tokendad.nesventory.data.local.SyncOperation
import com.tokendad.nesventory.data.local.SyncQueueDao
import com.tokendad.nesventory.data.local.SyncQueueEntity
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.LocationCreate
import com.tokendad.nesventory.data.remote.LocationPhoto
import com.tokendad.nesventory.data.remote.LocationUpdate
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.PaintInfo
import com.tokendad.nesventory.data.repository.LocationRepository
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi,
    private val locationDao: LocationDao,
    private val syncQueueDao: SyncQueueDao,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) : LocationRepository {

    private data class DeleteSyncPayload(val profileId: String? = null)
    @Volatile private var activeCacheProfileId: String? = null

    private fun Location.toEntity(): LocationEntity = LocationEntity(
        id = id.toString(),
        name = name,
        payload = gson.toJson(this),
        updatedAtMillis = System.currentTimeMillis()
    )

    private fun LocationEntity.toModel(): Location = gson.fromJson(payload, Location::class.java)

    override suspend fun getLocations(page: Int?, limit: Int?): List<Location> {
        ensureProfileScopedCache()
        val cached = locationDao.getAll()
            .map { it.toModel() }
            .sortedBy { it.id.toString() }
        val pageNumber = page ?: 1
        val pageSize = limit?.takeIf { it > 0 }
        val isCanonicalCacheQuery = page == null && limit == null

        return try {
            val remote = api.getLocations(page = page, limit = limit)
            if (isCanonicalCacheQuery) {
                locationDao.clearAll()
                locationDao.upsertAll(remote.map { it.toEntity() })
            }
            remote
        } catch (error: Exception) {
            if (cached.isNotEmpty()) {
                sliceForPage(cached, pageNumber, pageSize)
            } else {
                throw error
            }
        }
    }

    override suspend fun getLocation(id: UUID): Location {
        ensureProfileScopedCache()
        return try {
            val remote = api.getLocation(id)
            locationDao.upsert(remote.toEntity())
            remote
        } catch (error: Exception) {
            locationDao.getById(id.toString())?.toModel() ?: throw error
        }
    }

    override suspend fun createLocation(location: LocationCreate): Location {
        ensureProfileScopedCache()
        val created = api.createLocation(location)
        locationDao.upsert(created.toEntity())
        return created
    }

    override suspend fun updateLocation(id: UUID, location: LocationUpdate): Location {
        ensureProfileScopedCache()
        val updated = api.updateLocation(id, location)
        locationDao.upsert(updated.toEntity())
        return updated
    }

    override suspend fun deleteLocation(id: UUID) {
        ensureProfileScopedCache()
        try {
            api.deleteLocation(id)
            locationDao.deleteById(id.toString())
        } catch (error: Exception) {
            val shouldQueueDelete = error is IOException ||
                (error is HttpException && error.code() >= 500)
            if (!shouldQueueDelete) throw error
            syncQueueDao.insert(
                SyncQueueEntity(
                    operation = SyncOperation.DELETE,
                    entityType = SyncEntityType.LOCATION,
                    entityId = id.toString(),
                    payload = gson.toJson(DeleteSyncPayload(activeProfileId()))
                )
            )
            locationDao.deleteById(id.toString())
        }
    }

    override suspend fun uploadLocationPhoto(
        locationId: UUID,
        file: MultipartBody.Part,
        isPrimary: Boolean,
        photoType: String?
    ): LocationPhoto = api.uploadLocationPhoto(locationId, file, isPrimary, photoType)

    override suspend fun deleteLocationPhoto(locationId: UUID, photoId: UUID) {
        api.deleteLocationPhoto(locationId, photoId)
    }

    override suspend fun getLocationCategories(): List<String> = api.getLocationCategories()

    override suspend fun parsePaintLabel(file: MultipartBody.Part): PaintInfo =
        api.parsePaintLabel(file)

    private fun sliceForPage(items: List<Location>, page: Int, limit: Int?): List<Location> {
        val pageSize = limit ?: return items
        val safePage = page.coerceAtLeast(1)
        val fromIndex = ((safePage - 1) * pageSize).coerceAtMost(items.size)
        val toIndex = (fromIndex + pageSize).coerceAtMost(items.size)
        return items.subList(fromIndex, toIndex)
    }

    private suspend fun activeProfileId(): String? {
        return preferencesManager.serverProfiles.first().activeProfileId
    }

    private suspend fun ensureProfileScopedCache() {
        val profileId = activeProfileId()
        if (profileId != activeCacheProfileId) {
            locationDao.clearAll()
            activeCacheProfileId = profileId
        }
    }
}
