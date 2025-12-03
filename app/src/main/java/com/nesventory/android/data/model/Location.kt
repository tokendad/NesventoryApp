package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Location model from the NesVentory API.
 */
@Serializable
data class Location(
    val id: String,
    val name: String,
    @SerialName("parent_id")
    val parentId: String? = null,
    @SerialName("is_primary_location")
    val isPrimaryLocation: Boolean = false,
    @SerialName("is_container")
    val isContainer: Boolean = false,
    @SerialName("friendly_name")
    val friendlyName: String? = null,
    val description: String? = null,
    val address: String? = null,
    @SerialName("full_path")
    val fullPath: String? = null,
    @SerialName("location_type")
    val locationType: LocationType? = null,
    @SerialName("owner_info")
    val ownerInfo: JsonObject? = null,
    @SerialName("landlord_info")
    val landlordInfo: JsonObject? = null,
    @SerialName("tenant_info")
    val tenantInfo: JsonObject? = null,
    @SerialName("insurance_info")
    val insuranceInfo: JsonObject? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

/**
 * Location type enum matching the backend.
 */
@Serializable
enum class LocationType {
    @SerialName("residential")
    RESIDENTIAL,
    @SerialName("commercial")
    COMMERCIAL,
    @SerialName("retail")
    RETAIL,
    @SerialName("industrial")
    INDUSTRIAL,
    @SerialName("apartment_complex")
    APARTMENT_COMPLEX,
    @SerialName("condo")
    CONDO,
    @SerialName("multi_family")
    MULTI_FAMILY,
    @SerialName("other")
    OTHER
}
