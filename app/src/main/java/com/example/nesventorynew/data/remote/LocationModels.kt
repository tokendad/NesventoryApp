package com.example.nesventorynew.data.remote

import java.util.UUID

data class Location(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val friendly_name: String? = null,
    val parent_id: UUID? = null,
    val is_primary_location: Boolean = false,
    val is_container: Boolean = false,
    val address: String? = null,
    val estimated_property_value: String? = null,
    val estimated_value_with_items: String? = null,
    val full_path: String? = null,
    val created_at: String,
    val updated_at: String,
    val location_photos: List<LocationPhoto> = emptyList()
)

data class LocationPhoto(
    val id: UUID,
    val location_id: UUID,
    val filename: String,
    val path: String,
    val is_primary: Boolean,
    val uploaded_at: String
)
