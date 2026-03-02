package com.tokendad.nesventorynew.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Request model for Google OAuth authentication.
 * Uses "credential" field name as expected by the backend.
 */
data class GoogleAuthRequest(
    val credential: String  // The Google ID token
)

/**
 * Response model for Google OAuth authentication.
 * Matches the backend API response from /api/auth/google.
 *
 * Uses @SerializedName alternates to handle backend field-name variations:
 * the upstream server may return snake_case ("access_token") or camelCase
 * ("accessToken") depending on its Pydantic / serialisation settings.
 */
data class GoogleAuthResponse(
    @SerializedName(value = "access_token", alternate = ["accessToken", "token"])
    val access_token: String?,
    @SerializedName(value = "token_type", alternate = ["tokenType", "type"])
    val token_type: String?,
    @SerializedName(value = "is_new_user", alternate = ["isNewUser", "new_user"])
    val is_new_user: Boolean = false
)

/**
 * Response model for Google OAuth status check.
 */
data class GoogleAuthStatus(
    val enabled: Boolean,
    @SerializedName(value = "client_id", alternate = ["clientId"])
    val client_id: String? = null
)
