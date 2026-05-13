package com.tokendad.nesventory.data.remote

import java.util.UUID

data class UserProfile(
    val id: UUID,
    val username: String,
    val email: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val role: String? = null,
    val is_admin: Boolean? = null,
    val has_password: Boolean = true,
    val created_at: String
)

data class UserProfileUpdate(
    val email: String? = null,
    val full_name: String? = null
)

data class SetPasswordRequest(
    val current_password: String? = null,
    val new_password: String
)
