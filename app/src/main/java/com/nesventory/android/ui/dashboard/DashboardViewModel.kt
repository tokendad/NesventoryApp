package com.nesventory.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Location
import com.nesventory.android.data.model.User
import com.nesventory.android.data.repository.ApiResult
import com.nesventory.android.data.repository.NesVentoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the dashboard screen.
 */
data class DashboardUiState(
    val user: User? = null,
    val items: List<Item> = emptyList(),
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUsingLocalConnection: Boolean = false,
    val activeBaseUrl: String? = null
)

/**
 * ViewModel for the dashboard screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: NesVentoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Check connection status
            val isLocal = repository.isUsingLocalConnection()
            val baseUrl = repository.getActiveBaseUrl()
            _uiState.value = _uiState.value.copy(
                isUsingLocalConnection = isLocal,
                activeBaseUrl = baseUrl
            )

            // Load user info
            when (val userResult = repository.getCurrentUser()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(user = userResult.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = userResult.message)
                }
            }

            // Load items
            when (val itemsResult = repository.getItems()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(items = itemsResult.data)
                }
                is ApiResult.Error -> {
                    if (_uiState.value.error == null) {
                        _uiState.value = _uiState.value.copy(error = itemsResult.message)
                    }
                }
            }

            // Load locations
            when (val locationsResult = repository.getLocations()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(locations = locationsResult.data)
                }
                is ApiResult.Error -> {
                    if (_uiState.value.error == null) {
                        _uiState.value = _uiState.value.copy(error = locationsResult.message)
                    }
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
