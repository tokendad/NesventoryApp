package com.nesventory.android.ui.additem

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.repository.ApiResult
import com.nesventory.android.data.repository.NesVentoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddItemUiState(
    val itemName: String = "",
    val itemDescription: String = "",
    val itemBrand: String = "",
    val itemModelNumber: String = "",
    val itemSerialNumber: String = "",
    val photoUris: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val repository: NesVentoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    fun updateItemName(name: String) {
        _uiState.value = _uiState.value.copy(itemName = name)
    }

    fun updateItemDescription(description: String) {
        _uiState.value = _uiState.value.copy(itemDescription = description)
    }

    fun updateItemBrand(brand: String) {
        _uiState.value = _uiState.value.copy(itemBrand = brand)
    }

    fun updateItemModelNumber(modelNumber: String) {
        _uiState.value = _uiState.value.copy(itemModelNumber = modelNumber)
    }

    fun updateItemSerialNumber(serialNumber: String) {
        _uiState.value = _uiState.value.copy(itemSerialNumber = serialNumber)
    }

    fun addPhoto(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            photoUris = _uiState.value.photoUris + uri
        )
    }

    fun removePhoto(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            photoUris = _uiState.value.photoUris.filter { it != uri }
        )
    }

    fun saveItem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // TODO: Implement actual API call when POST /api/items/ is added
            // For now, simulate a successful save
            try {
                // Placeholder for API call
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to save item: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
