package com.nesventory.android

import androidx.lifecycle.ViewModel
import com.nesventory.android.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Main ViewModel for tracking authentication state across the app.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesManager: PreferencesManager
) : ViewModel() {
    
    /**
     * Flow indicating whether the user is currently logged in.
     */
    val isLoggedIn = preferencesManager.userSession.map { it.isLoggedIn }
    
    /**
     * Flow indicating whether server settings are configured.
     */
    val isServerConfigured = preferencesManager.serverSettings.map { it.isConfigured() }
}
