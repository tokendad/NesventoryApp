package com.tokendad.nesventory.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.SetPasswordRequest
import com.tokendad.nesventory.data.remote.UserProfile
import com.tokendad.nesventory.data.remote.UserProfileUpdate
import com.tokendad.nesventory.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var profile by mutableStateOf<UserProfile?>(null)
    var fullName by mutableStateOf("")
    var email by mutableStateOf("")

    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var isSavingProfile by mutableStateOf(false)
    var isChangingPassword by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    init {
        loadProfile()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val loadedProfile = userRepository.getMyProfile()
                profile = loadedProfile
                fullName = loadedProfile.full_name.orEmpty()
                email = loadedProfile.email.orEmpty()
            } catch (e: Exception) {
                errorMessage = "Failed to load profile: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveProfile() {
        val currentProfile = profile
        if (currentProfile == null) {
            errorMessage = "Profile not loaded yet"
            return
        }

        val normalizedEmail = email.trim()
        if (normalizedEmail.isNotEmpty() && !isValidEmail(normalizedEmail)) {
            errorMessage = "Please enter a valid email address"
            return
        }

        viewModelScope.launch {
            isSavingProfile = true
            errorMessage = null
            successMessage = null
            try {
                val updatedProfile = userRepository.updateProfile(
                    userId = currentProfile.id,
                    update = UserProfileUpdate(
                        email = normalizedEmail.ifBlank { null },
                        full_name = fullName.trim().ifBlank { null }
                    )
                )
                profile = updatedProfile
                fullName = updatedProfile.full_name.orEmpty()
                email = updatedProfile.email.orEmpty()
                successMessage = "Profile updated"
            } catch (e: Exception) {
                errorMessage = "Failed to update profile: ${e.localizedMessage}"
            } finally {
                isSavingProfile = false
            }
        }
    }

    fun changePassword() {
        val currentProfile = profile
        if (currentProfile == null) {
            errorMessage = "Profile not loaded yet"
            return
        }

        if (currentProfile.has_password && currentPassword.isBlank()) {
            errorMessage = "Current password is required"
            return
        }
        if (newPassword.length < 8) {
            errorMessage = "New password must be at least 8 characters"
            return
        }
        if (newPassword != confirmPassword) {
            errorMessage = "New password and confirmation do not match"
            return
        }

        viewModelScope.launch {
            isChangingPassword = true
            errorMessage = null
            successMessage = null
            try {
                val response = userRepository.setPassword(
                    SetPasswordRequest(
                        current_password = currentPassword.takeIf { currentProfile.has_password },
                        new_password = newPassword
                    )
                )
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                successMessage = response.status ?: "Password updated"
            } catch (e: Exception) {
                errorMessage = "Failed to update password: ${e.localizedMessage}"
            } finally {
                isChangingPassword = false
            }
        }
    }

    private fun isValidEmail(value: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return emailPattern.matches(value)
    }
}
