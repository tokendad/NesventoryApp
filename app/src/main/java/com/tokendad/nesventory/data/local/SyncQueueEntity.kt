package com.tokendad.nesventory.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val operation: String,
    val entityType: String,
    val entityId: String,
    val payload: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

object SyncOperation {
    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}

object SyncEntityType {
    const val ITEM = "ITEM"
    const val LOCATION = "LOCATION"
}
