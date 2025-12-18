package com.nesventory.android.ui.additem

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.model.DetectedItem
import com.nesventory.android.data.repository.ApiResult
import com.nesventory.android.data.repository.NesVentoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val isSaved: Boolean = false,
    val isProcessingAI: Boolean = false,
    val aiDetectedItems: List<DetectedItem> = emptyList(),
    val aiError: String? = null
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

    fun addPhoto(uri: Uri, context: Context) {
        _uiState.value = _uiState.value.copy(
            photoUris = _uiState.value.photoUris + uri
        )
        
        // Process the photo with AI if configured
        processPhotoWithAI(uri, context)
    }

    private fun processPhotoWithAI(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessingAI = true,
                aiError = null
            )
            
            when (val result = repository.processImageWithAI(uri, context)) {
                is ApiResult.Success -> {
                    val detectedItems = result.data.items
                    _uiState.value = _uiState.value.copy(
                        isProcessingAI = false,
                        aiDetectedItems = detectedItems,
                        aiError = null
                    )
                    
                    // Auto-populate fields if only one item detected and fields are empty
                    if (detectedItems.size == 1) {
                        applyDetectedItem(detectedItems[0])
                    }
                }
                is ApiResult.Error -> {
                    // Only show error if it's not a "not configured" error
                    // (AI is optional, so we don't want to alarm users if it's just not set up)
                    val errorMessage = if (result.code == 503 || result.message?.contains("not configured", ignoreCase = true) == true) {
                        null // Silently ignore if AI is not configured
                    } else {
                        result.message
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isProcessingAI = false,
                        aiError = errorMessage
                    )
                }
            }
        }
    }

    fun applyDetectedItem(item: DetectedItem) {
        // Only update empty fields
        _uiState.value = _uiState.value.copy(
            itemName = if (_uiState.value.itemName.isBlank()) item.name else _uiState.value.itemName,
            itemDescription = if (_uiState.value.itemDescription.isBlank()) item.description ?: "" else _uiState.value.itemDescription,
            itemBrand = if (_uiState.value.itemBrand.isBlank()) item.brand ?: "" else _uiState.value.itemBrand
        )
    }

    fun clearAIResults() {
        _uiState.value = _uiState.value.copy(
            aiDetectedItems = emptyList(),
            aiError = null
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
                delay(1000) // Simulate network delay
                
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
