package com.tokendad.nesventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.SecurePreferencesManager
import com.tokendad.nesventory.data.repository.UserRepository
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
    val localUrl: String = "",
    val userRole: String? = null,
    val isAdmin: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val securePreferencesManager: SecurePreferencesManager,
    private val userRepository: UserRepository,
    forbiddenEventBus: ForbiddenEventBus
) : ViewModel() {

    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute: StateFlow<String?> = _pendingRoute.asStateFlow()
    val forbiddenEvents = forbiddenEventBus.events
    private val _authToken = MutableStateFlow(securePreferencesManager.getAccessToken())
    private val _userRole = MutableStateFlow<String?>(null)
    private val _isAdmin = MutableStateFlow(false)

    // PKCE state for OIDC flow
    var pendingOidcState: String? = null
        private set
    private var pendingCodeVerifier: String? = null

    val uiState: StateFlow<MainUiState> = preferencesManager.serverSettings
        .combine(_authToken) { settings, token -> settings to token }
        .combine(_userRole) { pair, userRole -> Triple(pair.first, pair.second, userRole) }
        .combine(_isAdmin) { triple, isAdmin ->
            val settings = triple.first
            val token = triple.second
            val userRole = triple.third
            MainUiState(
                isLoggedIn = !token.isNullOrBlank(),
                remoteUrl = settings.remoteUrl,
                localUrl = settings.localUrl,
                userRole = userRole,
                isAdmin = isAdmin
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState()
        )

    init {
        refreshAuthState()
    }

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
            _authToken.value = null
            _userRole.value = null
            _isAdmin.value = false
        }
    }

    fun handleOidcToken(token: String) {
        viewModelScope.launch {
            securePreferencesManager.saveAccessToken(token)
            NetworkModule.updateCachedToken(token)
            _authToken.value = token
            refreshUserRole()
        }
    }

    fun refreshAuthState() {
        viewModelScope.launch {
            val token = securePreferencesManager.getAccessToken()
            _authToken.value = token
            if (token.isNullOrBlank()) {
                _userRole.value = null
                _isAdmin.value = false
            } else {
                refreshUserRole()
            }
        }
    }

    private suspend fun refreshUserRole() {
        try {
            val profile = userRepository.getMyProfile()
            _userRole.value = profile.role
            _isAdmin.value = profile.is_admin ?: profile.role.equals("admin", ignoreCase = true)
        } catch (_: Exception) {
            _userRole.value = null
            _isAdmin.value = false
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