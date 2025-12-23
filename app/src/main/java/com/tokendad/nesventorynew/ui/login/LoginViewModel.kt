package com.tokendad.nesventorynew.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: NesVentoryApi,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // UI State for the Login screen
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Attempts to log in using Form-URL-Encoded data as required
     * by the FastAPI OAuth2 Password flow.
     */
    fun login(username: String, password: String, onLoginSuccess: () -> Unit) {
        // Basic validation before network call
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both username and password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // We pass username and password as direct fields
                // to match the @FormUrlEncoded requirement in NesVentoryApi
                val response = api.login(
                    username = username,
                    password = password
                )

                // Save the token to DataStore for persistent session management
                preferencesManager.saveAccessToken(response.access_token)

                // Trigger navigation to Dashboard
                onLoginSuccess()
            } catch (e: Exception) {
                // This will catch 401 Unauthorized, 404, or Network timeouts
                errorMessage = "Login failed: ${e.localizedMessage ?: "Invalid credentials"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}