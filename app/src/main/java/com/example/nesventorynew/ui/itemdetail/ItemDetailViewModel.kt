package com.example.nesventorynew.ui.itemdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.remote.Item
import com.example.nesventorynew.data.remote.NesVentoryApi
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
}
