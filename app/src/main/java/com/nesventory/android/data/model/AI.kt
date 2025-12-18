package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Detected item from AI analysis.
 */
@Serializable
data class DetectedItem(
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    @SerialName("estimated_value")
    val estimatedValue: Double? = null,
    val confidence: Double? = null,
    @SerialName("estimation_date")
    val estimationDate: String? = null
)

/**
 * Detection result from AI image analysis.
 */
@Serializable
data class DetectionResult(
    val items: List<DetectedItem> = emptyList(),
    @SerialName("raw_response")
    val rawResponse: String? = null
)
