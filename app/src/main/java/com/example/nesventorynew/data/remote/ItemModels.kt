package com.example.nesventorynew.data.remote

import java.util.UUID

data class Item(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val model_number: String? = null,
    val estimated_value: String? = null,
    val location_id: UUID? = null,
    val created_at: String,
    val updated_at: String,
    val photos: List<Photo> = emptyList()
)

data class Photo(
    val id: UUID,
    val item_id: UUID,
    val filename: String,
    val path: String,
    val is_primary: Boolean,
    val uploaded_at: String
)