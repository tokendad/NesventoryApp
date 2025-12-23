package com.tokendad.nesventorynew.data.remote

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
    val insurance_info: InsuranceInfo? = null,
    val created_at: String,
    val updated_at: String,
    val location_photos: List<LocationPhoto> = emptyList()
)

data class InsuranceInfo(
    val company_name: String? = null,
    val company_address: String? = null,
    val company_email: String? = null,
    val company_phone: String? = null,
    val agent_name: String? = null,
    val policy_number: String? = null,
    val primary_holder: PolicyHolder? = null,
    val additional_holders: List<PolicyHolder> = emptyList(),
    val purchase_date: String? = null,
    val purchase_price: Double? = null,
    val build_date: String? = null
)

data class PolicyHolder(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

data class LocationPhoto(
    val id: UUID,
    val location_id: UUID,
    val filename: String,
    val path: String,
    val is_primary: Boolean,
    val uploaded_at: String
)

data class LocationCreate(
    val name: String,
    val description: String? = null,
    val friendly_name: String? = null,
    val address: String? = null,
    val parent_id: UUID? = null,
    val is_primary_location: Boolean = false,
    val is_container: Boolean = false,
    val estimated_property_value: String? = null,
    val insurance_info: InsuranceInfo? = null
)
