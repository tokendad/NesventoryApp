package com.tokendad.nesventory.ui.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.SecurePreferencesManager
import com.tokendad.nesventory.data.preferences.ServerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerProfilesViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val securePreferencesManager: SecurePreferencesManager
) : ViewModel() {
    private val profilesState = preferencesManager.serverProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.tokendad.nesventory.data.preferences.ServerProfileList())

    val profiles: StateFlow<List<ServerProfile>> = profilesState
        .map { it.profiles }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeProfileId: StateFlow<String?> = profilesState
        .map { it.activeProfileId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setActiveProfile(profileId: String) {
        viewModelScope.launch {
            preferencesManager.setActiveProfile(profileId)
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            val deletingActive = profilesState.value.activeProfileId == profileId
            preferencesManager.deleteServerProfile(profileId)
            securePreferencesManager.deleteAccessToken(profileId)
            if (deletingActive) {
                securePreferencesManager.clearAccessToken()
            }
        }
    }

    fun saveProfile(profile: ServerProfile, makeActive: Boolean) {
        viewModelScope.launch {
            preferencesManager.upsertServerProfile(profile, makeActive = makeActive)
        }
    }

    fun getProfileById(profileId: String?): ServerProfile? {
        if (profileId.isNullOrBlank()) return null
        return profilesState.value.profiles.firstOrNull { it.id == profileId }
    }
}
