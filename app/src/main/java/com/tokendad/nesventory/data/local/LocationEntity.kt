package com.tokendad.nesventory.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val payload: String,
    val updatedAtMillis: Long
)
