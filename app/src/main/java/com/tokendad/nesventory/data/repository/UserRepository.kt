package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.SetPasswordRequest
import com.tokendad.nesventory.data.remote.StatusResponse
import com.tokendad.nesventory.data.remote.UserProfile
import com.tokendad.nesventory.data.remote.UserProfileUpdate
import java.util.UUID

interface UserRepository {
    suspend fun getMyProfile(): UserProfile
    suspend fun updateProfile(userId: UUID, update: UserProfileUpdate): UserProfile
    suspend fun setPassword(request: SetPasswordRequest): StatusResponse
}
