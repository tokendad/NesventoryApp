package com.example.nesventorynew.ui.serversettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.preferences.PreferencesManager
import com.example.nesventorynew.data.preferences.ServerSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val serverSettings: StateFlow<ServerSettings> = preferencesManager.serverSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = ServerSettings()
        )

    fun saveSettings(settings: ServerSettings) {
        viewModelScope.launch {
            preferencesManager.saveServerSettings(settings)
        }
    }
}