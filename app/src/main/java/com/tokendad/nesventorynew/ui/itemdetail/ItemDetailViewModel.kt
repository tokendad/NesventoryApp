package com.tokendad.nesventorynew.ui.itemdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val api: NesVentoryApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var item by mutableStateOf<Item?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    init {
        val itemIdString: String? = savedStateHandle["itemId"]
        if (itemIdString != null) {
             try {
                 val id = UUID.fromString(itemIdString)
                 fetchItem(id)
             } catch (e: IllegalArgumentException) {
                 errorMessage = "Invalid Item ID format"
             }
        }
    }

    fun printLabel() {
        val currentItem = item ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val request = com.tokendad.nesventorynew.data.remote.PrintJobRequest(
                    entity_id = currentItem.id,
                    entity_type = "item"
                )
                api.printLabel(request)
                successMessage = "Print job sent successfully!"
            } catch (e: Exception) {
                errorMessage = "Failed to send print job: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchItem(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                item = api.getItem(id)
            } catch (e: Exception) {
                errorMessage = "Failed to load item details: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteItem(onSuccess: () -> Unit) {
        val currentItem = item ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                api.deleteItem(currentItem.id)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to delete item: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }
}
