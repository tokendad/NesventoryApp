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
    val photos: List<Photo> = emptyList(),
    val custom_fields: Map<String, Any>? = null
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
    val source: String? = null,
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

// --- AI / New Feature Models ---

data class AIStatusResponse(
    val gemini_configured: Boolean,
    val openai_configured: Boolean,
    val plugins_configured: Int
)

data class DataTagInfo(
    val manufacturer: String? = null,
    val brand: String? = null,
    val model_number: String? = null,
    val serial_number: String? = null,
    val production_date: String? = null,
    val estimated_value: Double? = null,
    val estimation_date: String? = null,
    val additional_info: Map<String, Any>? = null,
    val raw_response: String? = null
)

data class BarcodeLookupRequest(
    val upc: String
)

data class BarcodeLookupResult(
    val found: Boolean,
    val name: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val model_number: String? = null,
    val estimated_value: Double? = null,
    val estimation_date: String? = null,
    val category: String? = null,
    val raw_response: String? = null
)

data class BarcodeScanResult(
    val found: Boolean,
    val upc: String? = null,
    val raw_response: String? = null
)

data class EnrichedItemData(
    val description: String? = null,
    val brand: String? = null,
    val model_number: String? = null,
    val serial_number: String? = null,
    val estimated_value: String? = null,
    val estimated_value_ai_date: String? = null,
    val confidence: Double? = null,
    val source: String
)

data class ItemEnrichmentResult(
    val item_id: UUID,
    val enriched_data: List<EnrichedItemData>,
    val message: String
)

data class AITestConnectionResult(
    val provider_id: String,
    val provider_name: String,
    val success: Boolean,
    val message: String,
    val priority: Int,
    val is_plugin: Boolean
)

data class AITestConnectionResponse(
    val overall_success: Boolean,
    val summary: String,
    val results: List<AITestConnectionResult>,
    val total_providers: Int,
    val working_providers: Int,
    val failed_providers: Int
)