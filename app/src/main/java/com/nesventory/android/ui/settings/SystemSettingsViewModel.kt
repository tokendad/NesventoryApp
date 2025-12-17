package com.nesventory.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.model.AIStatus
import com.nesventory.android.data.model.PluginStatus
import com.nesventory.android.data.model.SystemStatus
import com.nesventory.android.data.repository.ApiResult
import com.nesventory.android.data.repository.NesVentoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SystemSettingsUiState(
    val systemStatus: SystemStatus? = null,
    val aiStatus: AIStatus? = null,
    val pluginStatus: PluginStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SystemSettingsViewModel @Inject constructor(
    private val repository: NesVentoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SystemSettingsUiState())
    val uiState: StateFlow<SystemSettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load system status
            when (val result = repository.getSystemStatus()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        systemStatus = result.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
            }
            
            // Load AI status
            when (val result = repository.getAIStatus()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        aiStatus = result.data
                    )
                }
                is ApiResult.Error -> {
                    // Don't override error if system status already failed
                    if (_uiState.value.error == null) {
                        _uiState.value = _uiState.value.copy(
                            error = result.message
                        )
                    }
                }
            }
            
            // Load plugin status
            when (val result = repository.getPluginStatus()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        pluginStatus = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                    // Don't override error if system status already failed
                    if (_uiState.value.error == null) {
                        _uiState.value = _uiState.value.copy(
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
