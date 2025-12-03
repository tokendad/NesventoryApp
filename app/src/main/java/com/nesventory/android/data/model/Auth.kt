package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

/**
 * Authentication token response from the NesVentory API.
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String
)

/**
 * Login request body.
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * User information from the API.
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String? = null,
    val role: UserRole,
    @SerialName("is_approved")
    val isApproved: Boolean = false,
    @SerialName("google_id")
    val googleId: String? = null,
    @SerialName("api_key")
    val apiKey: String? = null,
    @SerialName("ai_schedule_enabled")
    val aiScheduleEnabled: Boolean = false,
    @SerialName("ai_schedule_interval_days")
    val aiScheduleIntervalDays: Int = 7,
    @SerialName("ai_schedule_last_run")
    val aiScheduleLastRun: String? = null,
    @SerialName("gdrive_last_backup")
    val gdriveLastBackup: String? = null,
    @SerialName("upc_databases")
    val upcDatabases: JsonArray? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * User role enum matching the backend.
 */
@Serializable
enum class UserRole {
    @SerialName("admin")
    ADMIN,
    @SerialName("editor")
    EDITOR,
    @SerialName("viewer")
    VIEWER
}
