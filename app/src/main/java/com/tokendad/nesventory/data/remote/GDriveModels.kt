package com.tokendad.nesventory.data.remote

data class GDriveStatus(
    val connected: Boolean,
    val email: String? = null,
    val last_backup_at: String? = null,
    val last_backup_size_bytes: Long? = null,
    val backup_count: Int = 0
)

data class GDriveConnectResponse(
    val auth_url: String
)

data class GDriveBackup(
    val id: String,
    val name: String,
    val size_bytes: Long,
    val created_at: String,
    val mime_type: String
)

data class GDriveBackupResult(
    val success: Boolean,
    val backup_id: String? = null,
    val message: String? = null
)
