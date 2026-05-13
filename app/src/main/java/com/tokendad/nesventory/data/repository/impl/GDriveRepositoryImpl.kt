package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.GDriveBackup
import com.tokendad.nesventory.data.remote.GDriveBackupResult
import com.tokendad.nesventory.data.remote.GDriveConnectResponse
import com.tokendad.nesventory.data.remote.GDriveStatus
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.repository.GDriveRepository
import javax.inject.Inject

class GDriveRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : GDriveRepository {
    override suspend fun getStatus(): GDriveStatus = api.getGDriveStatus()

    override suspend fun connect(): GDriveConnectResponse = api.connectGDrive()

    override suspend fun disconnect() {
        val response = api.disconnectGDrive()
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to disconnect Google Drive (HTTP ${response.code()})")
        }
    }

    override suspend fun triggerBackup(): GDriveBackupResult = api.triggerGDriveBackup()

    override suspend fun listBackups(): List<GDriveBackup> = api.listGDriveBackups()

    override suspend fun deleteBackup(backupId: String) {
        val response = api.deleteGDriveBackup(backupId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to delete backup (HTTP ${response.code()})")
        }
    }
}
