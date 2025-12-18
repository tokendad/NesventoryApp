package com.nesventory.android.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.preferences.ServerSettings
import com.nesventory.android.data.repository.ApiResult
import com.nesventory.android.data.repository.NesVentoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the login screen.
 */
data class LoginUiState(
    val email: String = "demouser@nesventory.local",
    val password: String = "demo123",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val showServerSettings: Boolean = false,
    val serverSettings: ServerSettings = ServerSettings(),
    val isUsingLocalConnection: Boolean = false,
    val activeBaseUrl: String? = null
)

/**
 * ViewModel for the login screen.
 * Handles login logic and server settings management.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: NesVentoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Temporary state for server settings dialog
    var tempApiToken by mutableStateOf("")
        private set
    var tempRemoteUrl by mutableStateOf("")
        private set
    var tempLocalUrl by mutableStateOf("")
        private set
    var tempLocalSsid by mutableStateOf("")
        private set

    init {
        loadServerSettings()
        checkConnectionStatus()
        // Auto-configure demo server settings
        configureDemoServer()
    }

    private fun configureDemoServer() {
        viewModelScope.launch {
            val demoSettings = ServerSettings(
                apiToken = "",
                remoteUrl = "http://nesdemo.welshrd.com/",
                localUrl = "",
                localSsid = ""
            )
            repository.saveServerSettings(demoSettings)
            _uiState.value = _uiState.value.copy(serverSettings = demoSettings)
            checkConnectionStatus()
        }
    }

    private fun loadServerSettings() {
        viewModelScope.launch {
            val settings = repository.getServerSettings()
            _uiState.value = _uiState.value.copy(serverSettings = settings)
            tempApiToken = settings.apiToken
            tempRemoteUrl = settings.remoteUrl
            tempLocalUrl = settings.localUrl
            tempLocalSsid = settings.localSsid
        }
    }

    private fun checkConnectionStatus() {
        viewModelScope.launch {
            val isLocal = repository.isUsingLocalConnection()
            val baseUrl = repository.getActiveBaseUrl()
            _uiState.value = _uiState.value.copy(
                isUsingLocalConnection = isLocal,
                activeBaseUrl = baseUrl
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onTempApiTokenChange(value: String) {
        tempApiToken = value
    }

    fun onTempRemoteUrlChange(value: String) {
        tempRemoteUrl = value
    }

    fun onTempLocalUrlChange(value: String) {
        tempLocalUrl = value
    }

    fun onTempLocalSsidChange(value: String) {
        tempLocalSsid = value
    }

    fun showServerSettings() {
        val settings = _uiState.value.serverSettings
        tempApiToken = settings.apiToken
        tempRemoteUrl = settings.remoteUrl
        tempLocalUrl = settings.localUrl
        tempLocalSsid = settings.localSsid
        _uiState.value = _uiState.value.copy(showServerSettings = true)
    }

    fun hideServerSettings() {
        _uiState.value = _uiState.value.copy(showServerSettings = false)
    }

    fun saveServerSettings() {
        viewModelScope.launch {
            val settings = ServerSettings(
                apiToken = tempApiToken.trim(),
                remoteUrl = tempRemoteUrl.trim(),
                localUrl = tempLocalUrl.trim(),
                localSsid = tempLocalSsid.trim()
            )
            repository.saveServerSettings(settings)
            _uiState.value = _uiState.value.copy(
                serverSettings = settings,
                showServerSettings = false
            )
            checkConnectionStatus()
        }
    }

    fun login() {
        val state = _uiState.value
        
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Please enter email and password")
            return
        }

        if (!state.serverSettings.isConfigured()) {
            _uiState.value = state.copy(error = "Please configure server settings first")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            when (val result = repository.login(state.email, state.password)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
