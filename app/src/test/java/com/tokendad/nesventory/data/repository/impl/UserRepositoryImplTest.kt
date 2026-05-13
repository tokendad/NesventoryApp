package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.SetPasswordRequest
import com.tokendad.nesventory.data.remote.StatusResponse
import com.tokendad.nesventory.data.remote.UserProfile
import com.tokendad.nesventory.data.remote.UserProfileUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class UserRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = UserRepositoryImpl(api)

    @Test
    fun `getMyProfile delegates to api`() = runTest {
        val expected = UserProfile(
            id = UUID.randomUUID(),
            username = "demouser",
            email = "demo@example.com",
            full_name = "Demo User",
            created_at = "2025-01-01T00:00:00Z"
        )
        coEvery { api.getMyProfile() } returns expected

        val actual = repository.getMyProfile()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getMyProfile() }
    }

    @Test
    fun `updateProfile delegates to api`() = runTest {
        val userId = UUID.randomUUID()
        val update = UserProfileUpdate(email = "new@example.com", full_name = "New Name")
        val expected = UserProfile(
            id = userId,
            username = "demouser",
            email = "new@example.com",
            full_name = "New Name",
            created_at = "2025-01-01T00:00:00Z"
        )
        coEvery { api.updateProfile(userId, update) } returns expected

        val actual = repository.updateProfile(userId, update)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.updateProfile(userId, update) }
    }

    @Test
    fun `setPassword delegates to api`() = runTest {
        val request = SetPasswordRequest(current_password = "old-password", new_password = "new-password")
        val expected = StatusResponse(status = "ok")
        coEvery { api.setPassword(request) } returns expected

        val actual = repository.setPassword(request)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.setPassword(request) }
    }
}
