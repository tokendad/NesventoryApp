package com.tokendad.nesventorynew.data.remote

/**
 * Response from GET /api/status
 */
data class StatusResponse(
    val version: String? = null,
    val status: String? = null,
    val database: String? = null,
    val uptime: String? = null
)

/**
 * Response from GET /api/media/stats
 */
data class MediaStatsResponse(
    // Legacy Android fields
    val total_count: Int = 0,
    val total_size: Long = 0,
    val photo_count: Int = 0,
    val document_count: Int = 0,
    // Upstream API fields (v6.11.x)
    val total_photos: Int = 0,
    val total_videos: Int = 0,
    val total_storage_bytes: Long = 0,
    val total_storage_mb: Double = 0.0,
    val directories: List<String> = emptyList()
)

/**
 * Response from POST /api/printer/print-label
 */
data class PrintLabelResponse(
    val success: Boolean = false,
    val message: String? = null,
    val job_id: String? = null
)
