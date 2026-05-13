package com.tokendad.nesventory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class, LocationEntity::class, SyncQueueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NesVentoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun locationDao(): LocationDao
    abstract fun syncQueueDao(): SyncQueueDao
}
