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
    val total_count: Int = 0,
    val total_size: Long = 0,
    val photo_count: Int = 0,
    val document_count: Int = 0
)

/**
 * Response from POST /api/printer/print-label
 */
data class PrintLabelResponse(
    val success: Boolean = false,
    val message: String? = null,
    val job_id: String? = null
)
