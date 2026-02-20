package com.tokendad.nesventorynew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.combine

// Make sure this is OUTSIDE the class
data class MainUiState(
    val isLoggedIn: Boolean = false,
    val remoteUrl: String = "",
    val localUrl: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _pendingRoute = MutableStateFlow<String?>(null)
    val pendingRoute: StateFlow<String?> = _pendingRoute.asStateFlow()

    // Explicitly define the return type StateFlow<MainUiState>
    val uiState: StateFlow<MainUiState> = combine(
        preferencesManager.userSession,
        preferencesManager.serverSettings
    ) { session, settings ->
        MainUiState(
            isLoggedIn = session.accessToken.isNotBlank(),
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
            preferencesManager.clearAccessToken()
        }
    }

    fun handleOidcToken(token: String) {
        viewModelScope.launch {
            preferencesManager.saveAccessToken(token)
        }
    }
}