package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Item model from the NesVentory API.
 */
@Serializable
data class Item(
    val id: String,
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    @SerialName("model_number")
    val modelNumber: String? = null,
    @SerialName("serial_number")
    val serialNumber: String? = null,
    @SerialName("purchase_date")
    val purchaseDate: String? = null,
    @SerialName("purchase_price")
    val purchasePrice: Double? = null,
    @SerialName("estimated_value")
    val estimatedValue: Double? = null,
    @SerialName("estimated_value_ai_date")
    val estimatedValueAiDate: String? = null,
    @SerialName("estimated_value_user_date")
    val estimatedValueUserDate: String? = null,
    @SerialName("estimated_value_user_name")
    val estimatedValueUserName: String? = null,
    val retailer: String? = null,
    val upc: String? = null,
    @SerialName("location_id")
    val locationId: String? = null,
    @SerialName("is_living")
    val isLiving: Boolean = false,
    val birthdate: String? = null,
    @SerialName("contact_info")
    val contactInfo: JsonObject? = null,
    @SerialName("relationship_type")
    val relationshipType: String? = null,
    @SerialName("is_current_user")
    val isCurrentUser: Boolean = false,
    @SerialName("associated_user_id")
    val associatedUserId: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val photos: List<Photo> = emptyList(),
    val videos: List<Video> = emptyList(),
    val documents: List<Document> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val warranties: List<Warranty> = emptyList()
)

/**
 * Warranty information for an item.
 */
@Serializable
data class Warranty(
    val type: WarrantyType,
    val provider: String? = null,
    @SerialName("policy_number")
    val policyNumber: String? = null,
    @SerialName("duration_months")
    val durationMonths: Int? = null,
    @SerialName("expiration_date")
    val expirationDate: String? = null,
    val notes: String? = null
)

/**
 * Warranty type enum.
 */
@Serializable
enum class WarrantyType {
    @SerialName("manufacturer")
    MANUFACTURER,
    @SerialName("extended")
    EXTENDED
}

/**
 * Photo model from the NesVentory API.
 */
@Serializable
data class Photo(
    val id: String,
    @SerialName("item_id")
    val itemId: String,
    val path: String,
    @SerialName("mime_type")
    val mimeType: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean = false,
    @SerialName("is_data_tag")
    val isDataTag: Boolean = false,
    @SerialName("photo_type")
    val photoType: String? = null,
    @SerialName("uploaded_at")
    val uploadedAt: String
)

/**
 * Document model from the NesVentory API.
 */
@Serializable
data class Document(
    val id: String,
    @SerialName("item_id")
    val itemId: String,
    val filename: String,
    @SerialName("mime_type")
    val mimeType: String? = null,
    val path: String,
    @SerialName("document_type")
    val documentType: String? = null,
    @SerialName("uploaded_at")
    val uploadedAt: String
)

/**
 * Tag model from the NesVentory API.
 */
@Serializable
data class Tag(
    val id: String,
    val name: String,
    @SerialName("is_predefined")
    val isPredefined: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
