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
    val updated_at: String
)