package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.GDriveBackup
import com.tokendad.nesventory.data.remote.GDriveBackupResult
import com.tokendad.nesventory.data.remote.GDriveConnectResponse
import com.tokendad.nesventory.data.remote.GDriveStatus

interface GDriveRepository {
    suspend fun getStatus(): GDriveStatus
    suspend fun connect(): GDriveConnectResponse
    suspend fun disconnect()
    suspend fun triggerBackup(): GDriveBackupResult
    suspend fun listBackups(): List<GDriveBackup>
    suspend fun deleteBackup(backupId: String)
}
