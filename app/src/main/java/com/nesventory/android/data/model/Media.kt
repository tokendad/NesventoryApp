package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Media statistics response from the NesVentory API.
 * Contains information about total media counts and storage usage.
 */
@Serializable
data class MediaStats(
    @SerialName("total_photos")
    val totalPhotos: Int,
    @SerialName("total_videos")
    val totalVideos: Int,
    @SerialName("total_storage_bytes")
    val totalStorageBytes: Long,
    @SerialName("total_storage_mb")
    val totalStorageMb: Double,
    val directories: List<String>
)

/**
 * Media item in the media list response.
 * Can represent a photo, video, or location photo.
 */
@Serializable
data class MediaItem(
    val id: String,
    val type: String, // "photo", "video", or "location_photo"
    val path: String,
    @SerialName("mime_type")
    val mimeType: String? = null,
    @SerialName("uploaded_at")
    val uploadedAt: String,
    @SerialName("item_id")
    val itemId: String? = null,
    @SerialName("item_name")
    val itemName: String? = null,
    @SerialName("location_id")
    val locationId: String? = null,
    @SerialName("location_name")
    val locationName: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean? = null,
    @SerialName("is_data_tag")
    val isDataTag: Boolean? = null,
    @SerialName("photo_type")
    val photoType: String? = null,
    val filename: String? = null,
    @SerialName("video_type")
    val videoType: String? = null
)

/**
 * Response containing a list of media items.
 */
@Serializable
data class MediaListResponse(
    val media: List<MediaItem>
)

/**
 * Request to bulk delete media files.
 */
@Serializable
data class MediaBulkDeleteRequest(
    @SerialName("media_ids")
    val mediaIds: List<String>,
    @SerialName("media_types")
    val mediaTypes: List<String> // Corresponding types: 'photo', 'video', 'location_photo'
)

/**
 * Request to update media metadata.
 */
@Serializable
data class MediaUpdateRequest(
    @SerialName("item_id")
    val itemId: String? = null,
    @SerialName("location_id")
    val locationId: String? = null,
    @SerialName("photo_type")
    val photoType: String? = null,
    @SerialName("video_type")
    val videoType: String? = null
)
