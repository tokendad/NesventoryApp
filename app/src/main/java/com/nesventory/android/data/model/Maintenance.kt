package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maintenance task model from the NesVentory API.
 */
@Serializable
data class MaintenanceTask(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("item_id")
    val itemId: String? = null,
    @SerialName("location_id")
    val locationId: String? = null,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    val priority: MaintenancePriority? = null,
    val status: MaintenanceStatus = MaintenanceStatus.PENDING,
    @SerialName("recurrence_pattern")
    val recurrencePattern: String? = null,
    val color: String? = null,
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

/**
 * Maintenance priority enum.
 */
@Serializable
enum class MaintenancePriority {
    @SerialName("low")
    LOW,
    @SerialName("medium")
    MEDIUM,
    @SerialName("high")
    HIGH
}

/**
 * Maintenance status enum.
 */
@Serializable
enum class MaintenanceStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("in_progress")
    IN_PROGRESS,
    @SerialName("completed")
    COMPLETED,
    @SerialName("cancelled")
    CANCELLED
}
