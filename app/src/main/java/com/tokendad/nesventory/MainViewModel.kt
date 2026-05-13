package com.tokendad.nesventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.SecurePreferencesManager
import com.tokendad.nesventory.di.NetworkModule
import com.tokendad.nesventory.network.ForbiddenEventBus
import com.tokendad.nesventory.util.PkceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val remoteUrl: String = "",
    val localUrl: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val securePreferencesManager: SecurePreferencesManager,
    forbiddenEventBus: ForbiddenEventBus
) : ViewModel() {

    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute: StateFlow<String?> = _pendingRoute.asStateFlow()
    val forbiddenEvents = forbiddenEventBus.events

    // PKCE state for OIDC flow
    var pendingOidcState: String? = null
        private set
    private var pendingCodeVerifier: String? = null

    val uiState: StateFlow<MainUiState> = preferencesManager.serverSettings
        .combine(MutableStateFlow(Unit)) { settings, _ ->
            val token = securePreferencesManager.getAccessToken()
            MainUiState(
                isLoggedIn = !token.isNullOrBlank(),
                remoteUrl = settings.remoteUrl,
                localUrl = settings.localUrl
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState()
        )

    fun setPendingRoute(route: String) {
        _pendingRoute.value = route
    }

    fun clearPendingRoute() {
        _pendingRoute.value = null
    }

    fun logout() {
        viewModelScope.launch {
            securePreferencesManager.clearAccessToken()
            NetworkModule.updateCachedToken(null)
        }
    }

    fun handleOidcToken(token: String) {
        viewModelScope.launch {
            securePreferencesManager.saveAccessToken(token)
            NetworkModule.updateCachedToken(token)
        }
    }

    /**
     * Validates an OIDC callback state parameter.
     * Returns true if the state matches the pending state.
     */
    fun validateOidcState(returnedState: String?): Boolean {
        val expected = pendingOidcState
        if (expected == null || returnedState == null || returnedState != expected) {
            Log.w("MainViewModel", "OIDC state mismatch — possible CSRF attack, rejecting")
            return false
        }
        pendingOidcState = null
        return true
    }

    /**
     * Generates PKCE parameters for an OIDC authorization URL.
     * NOTE: The backend currently returns tokens directly via callback (not
     * authorization codes), so the code_verifier is not exchanged yet. When the
     * backend supports standard authorization-code exchange, the code_verifier
     * stored in [pendingCodeVerifier] should be sent to the token endpoint.
     */
    fun buildOidcUrlWithPkce(baseAuthUrl: String): String {
        pendingOidcState = PkceUtil.generateState()
        pendingCodeVerifier = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.deriveCodeChallenge(pendingCodeVerifier!!)

        val separator = if (baseAuthUrl.contains("?")) "&" else "?"
        return "$baseAuthUrl${separator}code_challenge=$challenge&code_challenge_method=S256&state=$pendingOidcState"
    }
}