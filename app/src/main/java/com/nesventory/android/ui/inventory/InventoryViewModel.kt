package com.nesventory.android.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Location
import com.nesventory.android.data.repository.ApiResult
import com.nesventory.android.data.repository.NesVentoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val items: List<Item> = emptyList(),
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: NesVentoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = repository.getItems()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        items = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
            }
            
            when (val result = repository.getLocations()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        locations = result.data
                    )
                }
                is ApiResult.Error -> {
                    // Don't override error if items already failed
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
