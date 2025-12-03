package com.nesventory.android.ui.serversettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.preferences.PreferencesManager
import com.nesventory.android.data.preferences.ServerSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Server Settings screen.
 * Manages server configuration including remote URL, local WiFi URL, and SSID.
 */
@HiltViewModel
class ServerSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _remoteUrl = MutableStateFlow("")
    val remoteUrl: StateFlow<String> = _remoteUrl.asStateFlow()

    private val _localUrl = MutableStateFlow("")
    val localUrl: StateFlow<String> = _localUrl.asStateFlow()

    private val _localSsid = MutableStateFlow("")
    val localSsid: StateFlow<String> = _localSsid.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadCurrentSettings()
    }

    /**
     * Load current server settings from preferences.
     */
    private fun loadCurrentSettings() {
        viewModelScope.launch {
            val settings = preferencesManager.serverSettings.first()
            _remoteUrl.value = settings.remoteUrl
            _localUrl.value = settings.localUrl
            _localSsid.value = settings.localSsid
        }
    }

    /**
     * Update remote URL field.
     */
    fun updateRemoteUrl(url: String) {
        _remoteUrl.value = url
    }

    /**
     * Update local WiFi URL field.
     */
    fun updateLocalUrl(url: String) {
        _localUrl.value = url
    }

    /**
     * Update local WiFi SSID field.
     */
    fun updateLocalSsid(ssid: String) {
        _localSsid.value = ssid
    }

    /**
     * Save server settings.
     */
    fun saveSettings(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val settings = ServerSettings(
                    remoteUrl = _remoteUrl.value.trim(),
                    localUrl = _localUrl.value.trim(),
                    localSsid = _localSsid.value.trim()
                )
                preferencesManager.saveServerSettings(settings)
                _saveSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                // Handle error if needed
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Validate that at least one URL is provided.
     */
    fun isValid(): Boolean {
        return _remoteUrl.value.trim().isNotBlank() || _localUrl.value.trim().isNotBlank()
    }
}
