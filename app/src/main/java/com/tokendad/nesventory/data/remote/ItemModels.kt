package com.tokendad.nesventory.data.remote

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
    val upc: String? = null,
    val is_living: Boolean = false,
    val birthdate: String? = null,
    val relationship_type: String? = null,
    val is_current_user: Boolean = false,
    val associated_user_id: UUID? = null,
    val contact_info: ContactInfo? = null,
    val location_id: UUID? = null,
    val created_at: String,
    val updated_at: String,
    val photos: List<Photo> = emptyList(),
    val custom_fields: Map<String, Any>? = null,
    val warranties: List<Warranty> = emptyList(),
    val tags: List<Tag> = emptyList()
)

data class Photo(
    val id: UUID,
    val item_id: UUID,
    val filename: String,
    val path: String,
    val thumbnail_path: String? = null,
    val is_primary: Boolean = false,
    val photo_type: String? = null,
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
    val upc: String? = null,
    val is_living: Boolean = false,
    val birthdate: String? = null,
    val relationship_type: String? = null,
    val is_current_user: Boolean = false,
    val associated_user_id: UUID? = null,
    val contact_info: ContactInfo? = null,
    val location_id: UUID? = null,
    val warranties: List<WarrantyCreate>? = null,
    val tag_ids: List<UUID>? = null
)

data class ItemUpdate(
    val name: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val model_number: String? = null,
    val serial_number: String? = null,
    val purchase_price: String? = null,
    val purchase_date: String? = null,
    val estimated_value: String? = null,
    val retailer: String? = null,
    val upc: String? = null,
    val is_living: Boolean? = null,
    val birthdate: String? = null,
    val relationship_type: String? = null,
    val is_current_user: Boolean? = null,
    val associated_user_id: UUID? = null,
    val contact_info: ContactInfo? = null,
    val location_id: UUID? = null,
    val warranties: List<WarrantyCreate>? = null,
    val tag_ids: List<UUID>? = null
)

data class ContactInfo(
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_phone: String? = null,
    val notes: String? = null
)

data class Warranty(
    val type: String,
    val provider: String? = null,
    val policy_number: String? = null,
    val duration_months: Int? = null,
    val expiration_date: String? = null,
    val notes: String? = null
)

data class WarrantyCreate(
    val type: String,
    val provider: String? = null,
    val policy_number: String? = null,
    val duration_months: Int? = null,
    val expiration_date: String? = null,
    val notes: String? = null
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
    @com.google.gson.annotations.SerializedName("name")
    val title: String,
    val description: String? = null,
    @com.google.gson.annotations.SerializedName("next_due_date")
    val due_date: String,
    @com.google.gson.annotations.SerializedName("recurrence_type")
    val frequency: String? = null,
    val recurrence_interval: Int? = null,
    val color: String? = null,
    val completed: Boolean = false,
    @com.google.gson.annotations.SerializedName("last_completed")
    val completed_date: String? = null,
    val created_at: String,
    val updated_at: String
)

data class MaintenanceTaskCreate(
    val item_id: UUID,
    @com.google.gson.annotations.SerializedName("name")
    val title: String,
    val description: String? = null,
    @com.google.gson.annotations.SerializedName("next_due_date")
    val due_date: String,
    @com.google.gson.annotations.SerializedName("recurrence_type")
    val frequency: String? = null,
    val recurrence_interval: Int? = null,
    val color: String? = null
)

data class MaintenanceTaskUpdate(
    @com.google.gson.annotations.SerializedName("name")
    val title: String? = null,
    val completed: Boolean? = null,
    @com.google.gson.annotations.SerializedName("last_completed")
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
    // Legacy Android fields (kept for backward compatibility)
    val gemini_configured: Boolean = false,
    val openai_configured: Boolean = false,
    val plugins_configured: Int = 0,
    // Upstream API fields (v6.11.x)
    val enabled: Boolean = false,
    val model: String? = null,
    val plugins_enabled: Boolean = false,
    val plugin_count: Int = 0
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