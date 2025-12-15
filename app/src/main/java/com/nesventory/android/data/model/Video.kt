package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Video model from the NesVentory API.
 */
@Serializable
data class Video(
    val id: String,
    @SerialName("item_id")
    val itemId: String,
    val filename: String,
    @SerialName("mime_type")
    val mimeType: String? = null,
    val path: String,
    @SerialName("uploaded_at")
    val uploadedAt: String
)
