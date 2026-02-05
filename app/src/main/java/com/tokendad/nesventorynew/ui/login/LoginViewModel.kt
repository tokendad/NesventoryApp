package com.tokendad.nesventorynew.ui.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.remote.GoogleAuthRequest
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: NesVentoryApi,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // UI State for the Login screen
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberCredentials by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Google Sign-In state
    var isGoogleSignInAvailable by mutableStateOf(false)
        private set

    var googleClientId by mutableStateOf<String?>(null)
        private set

    var isGoogleLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            preferencesManager.savedCredentials.collect { credentials ->
                if (credentials.isRemembered) {
                    username = credentials.username
                    password = credentials.password
                    rememberCredentials = true
                }
            }
        }
        checkGoogleAuthStatus()
    }

    /**
     * Check if Google OAuth is enabled on the server.
     */
    private fun checkGoogleAuthStatus() {
        viewModelScope.launch {
            try {
                val status = api.getGoogleAuthStatus()
                isGoogleSignInAvailable = status.enabled
                googleClientId = status.client_id
                android.util.Log.d("LoginViewModel", "Google Auth status: enabled=${status.enabled}, clientId=${status.client_id?.take(30)}...")
            } catch (e: Exception) {
                // Google auth not available - keep button hidden
                android.util.Log.w("LoginViewModel", "Google Auth check failed: ${e.message}")
                isGoogleSignInAvailable = false
                googleClientId = null
            }
        }
    }

    /**
     * Attempts to log in using Form-URL-Encoded data as required
     * by the FastAPI OAuth2 Password flow.
     */
    fun login(onLoginSuccess: () -> Unit) {
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

                // Save or clear credentials based on user preference
                preferencesManager.saveCredentials(username, password, rememberCredentials)

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

    /**
     * Initiates Google Sign-In using Credential Manager.
     *
     * @param context Activity context needed for Credential Manager
     * @param onSuccess Callback when login succeeds
     */
    fun signInWithGoogle(context: Context, onSuccess: () -> Unit) {
        val clientId = googleClientId
        if (clientId == null) {
            errorMessage = "Google Sign-In is not configured on the server"
            return
        }

        viewModelScope.launch {
            isGoogleLoading = true
            errorMessage = null

            try {
                val credentialManager = CredentialManager.create(context)

                // Use GetSignInWithGoogleOption for the full sign-in flow
                // This shows the Google Sign-In button/dialog
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                // Launch credential picker
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                handleGoogleSignInResult(result, onSuccess)

            } catch (e: GetCredentialCancellationException) {
                // User cancelled - don't show error
                android.util.Log.d("LoginViewModel", "Google Sign-In cancelled by user")
                errorMessage = null
            } catch (e: NoCredentialException) {
                // Try fallback with GetGoogleIdOption
                android.util.Log.d("LoginViewModel", "NoCredentialException - trying fallback: ${e.message}")
                tryGoogleIdFallback(context, clientId, onSuccess)
            } catch (e: GetCredentialException) {
                android.util.Log.e("LoginViewModel", "GetCredentialException: ${e.type} - ${e.message}", e)
                errorMessage = "Google Sign-In failed: ${e.type} - ${e.localizedMessage}"
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Unexpected error in Google Sign-In", e)
                errorMessage = "Google Sign-In failed: ${e.localizedMessage}"
            } finally {
                isGoogleLoading = false
            }
        }
    }

    /**
     * Fallback to GetGoogleIdOption if GetSignInWithGoogleOption fails.
     */
    private suspend fun tryGoogleIdFallback(context: Context, clientId: String, onSuccess: () -> Unit) {
        android.util.Log.d("LoginViewModel", "Trying Google ID fallback with clientId: ${clientId.take(20)}...")
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            android.util.Log.d("LoginViewModel", "Fallback got credential result")
            handleGoogleSignInResult(result, onSuccess)

        } catch (e: GetCredentialCancellationException) {
            android.util.Log.d("LoginViewModel", "Fallback cancelled by user")
            errorMessage = null
        } catch (e: Exception) {
            android.util.Log.e("LoginViewModel", "Fallback failed: ${e.javaClass.simpleName} - ${e.message}", e)
            errorMessage = "Google Sign-In not available: ${e.localizedMessage}"
        }
    }

    /**
     * Handle the credential response from Google Sign-In.
     */
    private suspend fun handleGoogleSignInResult(
        result: GetCredentialResponse,
        onSuccess: () -> Unit
    ) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        // Exchange Google ID token for NesVentory access token
                        exchangeGoogleToken(idToken, onSuccess)

                    } catch (e: GoogleIdTokenParsingException) {
                        errorMessage = "Invalid Google credential"
                        e.printStackTrace()
                    }
                } else {
                    errorMessage = "Unexpected credential type"
                }
            }
            else -> {
                errorMessage = "Unexpected credential type"
            }
        }
    }

    /**
     * Exchange Google ID token for NesVentory access token.
     */
    private suspend fun exchangeGoogleToken(idToken: String, onSuccess: () -> Unit) {
        try {
            val response = api.loginWithGoogle(GoogleAuthRequest(credential = idToken))

            // Save the access token
            preferencesManager.saveAccessToken(response.access_token)

            // Clear any saved password credentials (using Google now)
            preferencesManager.saveCredentials("", "", false)

            // Navigate to dashboard
            onSuccess()

        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Authentication failed"
            errorMessage = when {
                errorMsg.contains("pending", ignoreCase = true) ->
                    "Account created! Waiting for admin approval."
                errorMsg.contains("403") ->
                    "Account pending admin approval"
                else ->
                    "Google Sign-In failed: $errorMsg"
            }
            e.printStackTrace()
        }
    }

    suspend fun getSsoUrl(): String {
        val settings = preferencesManager.serverSettings.first()
        val baseUrl = if (settings.remoteUrl.isNotBlank()) settings.remoteUrl else "https://nesdemo.welshrd.com/"
        val cleanBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        return "$cleanBase/api/auth/oidc/login"
    }
}
