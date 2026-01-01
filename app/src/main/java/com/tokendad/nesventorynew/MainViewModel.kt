package com.tokendad.nesventorynew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import kotlinx.coroutines.launch

// Make sure this is OUTSIDE the class
data class MainUiState(val isLoggedIn: Boolean = false)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Explicitly define the return type StateFlow<MainUiState>
    val uiState: StateFlow<MainUiState> = preferencesManager.userSession
        .map { session ->
            MainUiState(isLoggedIn = session.accessToken.isNotBlank())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState()
        )

    fun logout() {
        viewModelScope.launch {
            preferencesManager.clearAccessToken()
        }
    }
}