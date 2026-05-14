package com.tokendad.nesventory.ui.login

import android.content.Context
import android.util.Log
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
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.SecurePreferencesManager
import com.tokendad.nesventory.data.remote.GoogleAuthRequest
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

    // UI State for the Login screen
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberCredentials by mutableStateOf(false)

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var serverConfigured by mutableStateOf(false)
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
            preferencesManager.savedUsername.collect { saved ->
                if (saved.isRemembered) {
                    username = saved.username
                    rememberCredentials = true
                    // Load password from encrypted storage
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
                    if (configured) {
                        checkGoogleAuthStatus()
                    } else {
                        isGoogleSignInAvailable = false
                        googleClientId = null
                    }
                }
        }
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
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("LoginViewModel", "Google Auth status: enabled=${status.enabled}, clientId=${status.client_id?.take(30)}...")
                }
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
        if (!serverConfigured) {
            errorMessage = "No server configured. Tap ⚙ to add your server."
            return
        }
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both username and password"
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
                        // Fallback to root /token for older server versions
                        api.loginFallback(username = username, password = password)
                    } else {
                        throw e
                    }
                }

                // Save the token to DataStore for persistent session management
                val activeProfileId = preferencesManager.serverProfiles.first().activeProfileId
                if (activeProfileId != null) {
                    securePreferencesManager.saveAccessToken(activeProfileId, response.access_token)
                } else {
                    securePreferencesManager.saveAccessToken(response.access_token)
                }
                NetworkModule.updateCachedToken(activeProfileId, response.access_token)

                // Save username to DataStore, password to encrypted storage
                preferencesManager.saveUsername(username, rememberCredentials)
                if (rememberCredentials) {
                    securePreferencesManager.savePassword(password)
                } else {
                    securePreferencesManager.clearPassword()
                }

                // Trigger navigation to Dashboard
                onLoginSuccess()
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.localizedMessage ?: "Invalid credentials"}"
                if (BuildConfig.DEBUG) e.printStackTrace()
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
        if (!serverConfigured) {
            errorMessage = "No server configured. Tap ⚙ to add your server."
            return
        }
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
                // Can be a true user cancel OR a Play Services rejection (e.g. the Google
                // Cloud Console OAuth client isn't authorized for this Android app's SHA-1).
                android.util.Log.w("LoginViewModel", "Google Sign-In cancelled/rejected: ${e.message}", e)
                // Only hide the error when there really was no error to report
                if (errorMessage == null) {
                    errorMessage = if (BuildConfig.DEBUG)
                        "Google Sign-In was cancelled or rejected by Play Services. " +
                        "Ensure your server's Google Web Client ID is authorized for " +
                        "this app's package name and SHA-1 fingerprint in Google Cloud Console."
                    else
                        "Google Sign-In failed. Please try again or use username/password."
                }
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
        if (BuildConfig.DEBUG) {
            android.util.Log.d("LoginViewModel", "Trying Google ID fallback with clientId: ${clientId.take(20)}...")
        }
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
            android.util.Log.w("LoginViewModel", "Fallback also cancelled/rejected: ${e.message}", e)
            errorMessage = if (BuildConfig.DEBUG)
                "Google Sign-In rejected. Check that your server's Google Web Client ID " +
                "is authorized for this app (package + SHA-1) in Google Cloud Console."
            else
                "Google Sign-In failed. Please try again or use username/password."
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
                // Accept both the GetGoogleIdOption type and the GetSignInWithGoogleOption
                // (SIWG) type — both carry the same Bundle payload parsed by createFrom().
                val isGoogleCredential =
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                if (isGoogleCredential) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        android.util.Log.d("LoginViewModel", "Got Google ID token (type=${credential.type})")

                        // Exchange Google ID token for NesVentory access token
                        exchangeGoogleToken(idToken, onSuccess)

                    } catch (e: GoogleIdTokenParsingException) {
                        android.util.Log.e("LoginViewModel", "Failed to parse Google ID token", e)
                        errorMessage = "Invalid Google credential"
                        if (BuildConfig.DEBUG) e.printStackTrace()
                    }
                } else {
                    android.util.Log.e("LoginViewModel", "Unexpected credential type: ${credential.type}")
                    errorMessage = "Unexpected credential type: ${credential.type}"
                }
            }
            else -> {
                android.util.Log.e("LoginViewModel", "Non-custom credential type: ${credential.javaClass.simpleName}")
                errorMessage = "Unexpected credential type"
            }
        }
    }

    /**
     * Exchange Google ID token for NesVentory access token.
     *
     * The backend may deliver the access token in one of two ways:
     *   1. JSON body field – "access_token" / "accessToken" / "token"
     *   2. Set-Cookie header – "access_token=<jwt>" (cookie-based web flow)
     *
     * This method tries the body first, then falls back to the cookie.
     */
    private suspend fun exchangeGoogleToken(idToken: String, onSuccess: () -> Unit) {
        try {
            android.util.Log.d("LoginViewModel", "Exchanging Google token with backend...")
            val response = api.loginWithGoogle(GoogleAuthRequest(credential = idToken))

            val code = response.code()
            android.util.Log.d("LoginViewModel", "Google auth response: HTTP $code")

            // ── Handle non-2xx responses ──────────────────────────────
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("LoginViewModel", "Google auth error: HTTP $code — $errorBody")
                errorMessage = when (code) {
                    403 -> "Account pending admin approval"
                    401 -> "Google authentication rejected by server"
                    409 -> "Account already exists with different login method"
                    else -> "Authentication failed (HTTP $code)"
                }
                return
            }

            // ── Try to obtain the access token ───────────────────────
            val body = response.body()
            android.util.Log.d("LoginViewModel",
                "Response body — access_token null: ${body?.access_token == null}, " +
                "token_type: ${body?.token_type}, is_new_user: ${body?.is_new_user}")

            var accessToken = body?.access_token

            // Fallback: extract token from Set-Cookie header.
            // Some backend versions use cookie-based auth for the Google
            // endpoint (web-first design) — the token lives in a cookie
            // rather than (or in addition to) the JSON body.
            if (accessToken.isNullOrBlank()) {
                accessToken = extractTokenFromCookies(response.headers())
                if (!accessToken.isNullOrBlank()) {
                    android.util.Log.d("LoginViewModel",
                        "Extracted access token from Set-Cookie header (length: ${accessToken.length})")
                }
            }

            if (accessToken.isNullOrBlank()) {
                // Log the raw headers so the next debug session has data
                android.util.Log.e("LoginViewModel",
                    "Google auth: HTTP 200 but no access token in body or cookies. " +
                    "Headers: ${response.headers()}")
                errorMessage = "Authentication failed: No access token received from server. " +
                    "Please check that your server version supports mobile Google Sign-In."
                return
            }

            // ── Persist token and update runtime cache ───────────────
            android.util.Log.d("LoginViewModel", "Saving access token (length: ${accessToken.length})")
            val activeProfileId = preferencesManager.serverProfiles.first().activeProfileId
            if (activeProfileId != null) {
                securePreferencesManager.saveAccessToken(activeProfileId, accessToken)
            } else {
                securePreferencesManager.saveAccessToken(accessToken)
            }
            NetworkModule.updateCachedToken(activeProfileId, accessToken)

            // Clear any saved password credentials (using Google now)
            preferencesManager.saveUsername("", false)
            securePreferencesManager.clearPassword()

            // Navigate to dashboard
            onSuccess()

        } catch (e: Exception) {
            android.util.Log.e("LoginViewModel", "exchangeGoogleToken failed", e)
            val errorMsg = e.localizedMessage ?: "Authentication failed"
            errorMessage = when {
                errorMsg.contains("pending", ignoreCase = true) ->
                    "Account created! Waiting for admin approval."
                errorMsg.contains("403") ->
                    "Account pending admin approval"
                else ->
                    "Google Sign-In failed: $errorMsg"
            }
        }
    }

    /**
     * Attempt to extract an access/session token from Set-Cookie headers.
     *
     * Backends that emphasise cookie-based auth (common in web-first FastAPI
     * deployments) set the token via `Set-Cookie: access_token=<jwt>; ...`
     * instead of (or alongside) the JSON body.
     */
    private fun extractTokenFromCookies(headers: okhttp3.Headers): String? {
        val cookieHeaders = headers.values("Set-Cookie")
        for (cookie in cookieHeaders) {
            // Each Set-Cookie value looks like:
            //   access_token=eyJhbG...; Path=/; HttpOnly; SameSite=Lax
            val attributes = cookie.split(";").map { it.trim() }
            val nameValue = attributes.firstOrNull() ?: continue
            val eqIdx = nameValue.indexOf('=')
            if (eqIdx <= 0) continue

            val name  = nameValue.substring(0, eqIdx).trim()
            val value = nameValue.substring(eqIdx + 1).trim()
                .removeSurrounding("\"")  // strip optional quotes

            if (name.equals("access_token", ignoreCase = true) ||
                name.equals("session", ignoreCase = true) ||
                name.equals("token", ignoreCase = true)) {
                // Ignore deletion cookies
                if (value.isNotBlank() && value != "deleted" && value != "null") {
                    return value
                }
            }
        }
        return null
    }

    suspend fun getSsoUrl(): String {
        val settings = preferencesManager.serverSettings.first()
        val baseUrl = settings.remoteUrl.ifBlank { settings.localUrl }
        if (baseUrl.isBlank()) error("No server configured. Please add a server URL in Settings.")
        val cleanBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        return "$cleanBase/api/auth/oidc/login"
    }
}
