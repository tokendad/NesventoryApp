package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.SetPasswordRequest
import com.tokendad.nesventory.data.remote.StatusResponse
import com.tokendad.nesventory.data.remote.UserProfile
import com.tokendad.nesventory.data.remote.UserProfileUpdate
import com.tokendad.nesventory.data.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : UserRepository {
    override suspend fun getMyProfile(): UserProfile = api.getMyProfile()

    override suspend fun updateProfile(userId: UUID, update: UserProfileUpdate): UserProfile =
        api.updateProfile(userId, update)

    override suspend fun setPassword(request: SetPasswordRequest): StatusResponse =
        api.setPassword(request)
}
