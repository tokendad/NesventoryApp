package com.tokendad.nesventorynew.data.remote

import java.util.UUID

data class Item(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val model_number: String? = null,
    val serial_number: String? = null,
    val purchase_price: String? = null,
    val purchase_date: String? = null,
    val estimated_value: String? = null,
    val retailer: String? = null,
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

data class ItemCreate(
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val model_number: String? = null,
    val serial_number: String? = null,
    val purchase_price: String? = null,
    val purchase_date: String? = null,
    val estimated_value: String? = null,
    val retailer: String? = null,
    val location_id: UUID? = null
)

data class DetectionResult(
    val items: List<DetectedItem>,
    val raw_response: String? = null
)

data class DetectedItem(
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val estimated_value: Double? = null,
    val confidence: Double? = null,
    val estimation_date: String? = null
)

data class MaintenanceTask(
    val id: UUID,
    val item_id: UUID,
    val title: String,
    val description: String? = null,
    val due_date: String,
    val frequency: String? = null,
    val color: String? = null,
    val completed: Boolean = false,
    val completed_date: String? = null,
    val created_at: String,
    val updated_at: String
)

data class MaintenanceTaskCreate(
    val item_id: UUID,
    val title: String,
    val description: String? = null,
    val due_date: String,
    val frequency: String? = null,
    val color: String? = null
)

data class MaintenanceTaskUpdate(
    val title: String? = null,
    val completed: Boolean? = null,
    val completed_date: String? = null
)

data class Document(
    val id: UUID,
    val item_id: UUID,
    val filename: String,
    val path: String,
    val mime_type: String,
    val document_type: String? = null,
    val created_at: String
)