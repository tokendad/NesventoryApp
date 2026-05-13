package com.tokendad.nesventory.ui.profile

import com.tokendad.nesventory.data.remote.SetPasswordRequest
import com.tokendad.nesventory.data.remote.StatusResponse
import com.tokendad.nesventory.data.remote.UserProfile
import com.tokendad.nesventory.data.repository.UserRepository
import com.tokendad.nesventory.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        coEvery { userRepository.getMyProfile() } returns UserProfile(
            id = UUID.randomUUID(),
            username = "demouser",
            email = "demo@example.com",
            full_name = "Demo User",
            has_password = true,
            created_at = "2025-01-01T00:00:00Z"
        )
        viewModel = ProfileViewModel(userRepository)
    }

    @Test
    fun `init loads profile into editable fields`() = runTest {
        advanceUntilIdle()

        assertEquals("Demo User", viewModel.fullName)
        assertEquals("demo@example.com", viewModel.email)
        assertEquals("demouser", viewModel.profile?.username)
    }

    @Test
    fun `saveProfile blocks invalid email`() = runTest {
        advanceUntilIdle()
        viewModel.email = "invalid-email"

        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals("Please enter a valid email address", viewModel.errorMessage)
        coVerify(exactly = 0) { userRepository.updateProfile(any(), any()) }
    }

    @Test
    fun `changePassword requires current password when user has password`() = runTest {
        advanceUntilIdle()
        viewModel.currentPassword = ""
        viewModel.newPassword = "new-password"
        viewModel.confirmPassword = "new-password"

        viewModel.changePassword()
        advanceUntilIdle()

        assertEquals("Current password is required", viewModel.errorMessage)
        coVerify(exactly = 0) { userRepository.setPassword(any()) }
    }

    @Test
    fun `changePassword omits current password for oauth-only account`() = runTest {
        val oauthOnlyProfile = UserProfile(
            id = UUID.randomUUID(),
            username = "oauthuser",
            has_password = false,
            created_at = "2025-01-01T00:00:00Z"
        )
        coEvery { userRepository.getMyProfile() } returns oauthOnlyProfile
        coEvery { userRepository.setPassword(any()) } returns StatusResponse(status = "ok")

        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        viewModel.newPassword = "new-password"
        viewModel.confirmPassword = "new-password"

        val requestSlot = slot<SetPasswordRequest>()
        coEvery { userRepository.setPassword(capture(requestSlot)) } returns StatusResponse(status = "ok")

        viewModel.changePassword()
        advanceUntilIdle()

        assertNull(requestSlot.captured.current_password)
        assertEquals("new-password", requestSlot.captured.new_password)
        assertEquals("ok", viewModel.successMessage)
    }
}
