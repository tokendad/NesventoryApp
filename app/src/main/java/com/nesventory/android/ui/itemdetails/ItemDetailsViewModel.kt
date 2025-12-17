package com.nesventory.android.ui.itemdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Photo
import com.nesventory.android.data.repository.ApiResult
import com.nesventory.android.data.repository.NesVentoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailsUiState(
    val item: Item? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    
    // Editable fields
    val editName: String = "",
    val editDescription: String = "",
    val editBrand: String = "",
    val editModelNumber: String = "",
    val editSerialNumber: String = "",
    val editEstimatedValue: String = ""
)

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val repository: NesVentoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ItemDetailsUiState())
    val uiState: StateFlow<ItemDetailsUiState> = _uiState.asStateFlow()

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Get all items and find the one with matching ID
            when (val result = repository.getItems()) {
                is ApiResult.Success -> {
                    val item = result.data.find { it.id == itemId }
                    if (item != null) {
                        _uiState.value = _uiState.value.copy(
                            item = item,
                            isLoading = false,
                            editName = item.name,
                            editDescription = item.description ?: "",
                            editBrand = item.brand ?: "",
                            editModelNumber = item.modelNumber ?: "",
                            editSerialNumber = item.serialNumber ?: "",
                            editEstimatedValue = item.estimatedValue?.toString() ?: ""
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Item not found",
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun startEditing() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    fun cancelEditing() {
        val item = _uiState.value.item
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            editName = item?.name ?: "",
            editDescription = item?.description ?: "",
            editBrand = item?.brand ?: "",
            editModelNumber = item?.modelNumber ?: "",
            editSerialNumber = item?.serialNumber ?: "",
            editEstimatedValue = item?.estimatedValue?.toString() ?: ""
        )
    }

    fun updateEditName(name: String) {
        _uiState.value = _uiState.value.copy(editName = name)
    }

    fun updateEditDescription(description: String) {
        _uiState.value = _uiState.value.copy(editDescription = description)
    }

    fun updateEditBrand(brand: String) {
        _uiState.value = _uiState.value.copy(editBrand = brand)
    }

    fun updateEditModelNumber(modelNumber: String) {
        _uiState.value = _uiState.value.copy(editModelNumber = modelNumber)
    }

    fun updateEditSerialNumber(serialNumber: String) {
        _uiState.value = _uiState.value.copy(editSerialNumber = serialNumber)
    }

    fun updateEditEstimatedValue(value: String) {
        _uiState.value = _uiState.value.copy(editEstimatedValue = value)
    }

    fun saveItem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // TODO: Implement actual API call when PATCH /api/items/{id} is added
            // For now, simulate a successful save
            try {
                delay(1000) // Simulate network delay
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEditing = false,
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

    fun movePhotoUp(photo: Photo) {
        val item = _uiState.value.item ?: return
        val photos = item.photos.toMutableList()
        val index = photos.indexOf(photo)
        if (index > 0) {
            photos.removeAt(index)
            photos.add(index - 1, photo)
            _uiState.value = _uiState.value.copy(
                item = item.copy(photos = photos)
            )
        }
    }

    fun movePhotoDown(photo: Photo) {
        val item = _uiState.value.item ?: return
        val photos = item.photos.toMutableList()
        val index = photos.indexOf(photo)
        if (index < photos.size - 1) {
            photos.removeAt(index)
            photos.add(index + 1, photo)
            _uiState.value = _uiState.value.copy(
                item = item.copy(photos = photos)
            )
        }
    }
}
