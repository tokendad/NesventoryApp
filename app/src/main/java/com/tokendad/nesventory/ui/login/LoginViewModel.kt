package com.tokendad.nesventory.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.SecurePreferencesManager
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.di.NetworkModule
import com.tokendad.nesventory.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: NesVentoryApi,
    private val preferencesManager: PreferencesManager,
    private val securePreferencesManager: SecurePreferencesManager
) : ViewModel() {

    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberCredentials by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var serverConfigured by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            preferencesManager.savedUsername.collect { saved ->
                if (saved.isRemembered) {
                    username = saved.username
                    rememberCredentials = true
                    securePreferencesManager.getPassword()?.let { password = it }
                }
            }
        }
        viewModelScope.launch {
            preferencesManager.serverSettings
                .map { it.isConfigured() }
                .distinctUntilChanged()
                .collect { configured ->
                    serverConfigured = configured
                }
        }
    }

    /**
     * Attempts to log in using Form-URL-Encoded data as required
     * by the FastAPI OAuth2 Password flow.
     *
     * The server accepts the user's email address as the "username" field.
     */
    fun login(onLoginSuccess: () -> Unit) {
        if (!serverConfigured) {
            errorMessage = "No server configured. Tap ⚙ to add your server."
            return
        }
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = try {
                    api.login(username = username, password = password)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 404) {
                        api.loginFallback(username = username, password = password)
                    } else {
                        throw e
                    }
                }

                if (!response.isSuccessful) {
                    errorMessage = "Login failed: HTTP ${response.code()}"
                    return@launch
                }

                // Token may arrive in JSON body or Set-Cookie header depending on server version
                var accessToken = response.body()?.access_token
                if (accessToken.isNullOrBlank()) {
                    accessToken = extractTokenFromCookies(response.headers())
                }
                if (accessToken.isNullOrBlank()) {
                    errorMessage = "Login failed: No access token in server response"
                    return@launch
                }

                val activeProfileId = preferencesManager.serverProfiles.first().activeProfileId
                if (activeProfileId != null) {
                    securePreferencesManager.saveAccessToken(activeProfileId, accessToken)
                } else {
                    securePreferencesManager.saveAccessToken(accessToken)
                }
                NetworkModule.updateCachedToken(activeProfileId, accessToken)

                preferencesManager.saveUsername(username, rememberCredentials)
                if (rememberCredentials) {
                    securePreferencesManager.savePassword(password)
                } else {
                    securePreferencesManager.clearPassword()
                }

                onLoginSuccess()
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.localizedMessage ?: "Invalid credentials"}"
                if (BuildConfig.DEBUG) e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun getSsoUrl(): String {
        val settings = preferencesManager.serverSettings.first()
        val baseUrl = settings.remoteUrl.ifBlank { settings.localUrl }
        if (baseUrl.isBlank()) error("No server configured. Please add a server URL in Settings.")
        val cleanBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        return "$cleanBase/api/auth/oidc/login"
    }

    private fun extractTokenFromCookies(headers: okhttp3.Headers): String? {
        for (cookie in headers.values("Set-Cookie")) {
            val nameValue = cookie.split(";").firstOrNull() ?: continue
            val eq = nameValue.indexOf('=')
            if (eq <= 0) continue
            val name = nameValue.substring(0, eq).trim()
            val value = nameValue.substring(eq + 1).trim().removeSurrounding("\"")
            if (name.equals("access_token", ignoreCase = true) && value.isNotBlank() && value != "deleted") {
                return value
            }
        }
        return null
    }
}
