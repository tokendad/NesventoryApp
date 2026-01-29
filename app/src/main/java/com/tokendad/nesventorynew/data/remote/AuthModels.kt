package com.tokendad.nesventorynew.data.remote

/**
 * Request model for Google OAuth authentication.
 * Uses "credential" field name as expected by the backend.
 */
data class GoogleAuthRequest(
    val credential: String  // The Google ID token
)

/**
 * Response model for Google OAuth authentication.
 */
data class GoogleAuthResponse(
    val access_token: String,
    val token_type: String,
    val is_new_user: Boolean = false
)

/**
 * Response model for Google OAuth status check.
 */
data class GoogleAuthStatus(
    val enabled: Boolean,
    val client_id: String? = null
)
