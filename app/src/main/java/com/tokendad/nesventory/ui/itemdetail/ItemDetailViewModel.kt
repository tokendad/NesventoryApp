package com.tokendad.nesventory.ui.itemdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.repository.ItemRepository
import com.tokendad.nesventory.ui.printer.PrintJobRouter
import com.tokendad.nesventory.ui.printer.PrintResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val preferencesManager: PreferencesManager,
    private val printJobRouter: PrintJobRouter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var item by mutableStateOf<Item?>(null)
    var itemCollections by mutableStateOf<List<Collection>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    
    // Default to known base, update from prefs
    var serverUrl by mutableStateOf(com.tokendad.nesventory.util.Constants.DEFAULT_REMOTE_URL)

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
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val initial = preferencesManager.serverSettings.first()
            if (initial.remoteUrl.isNotBlank()) {
                serverUrl = initial.remoteUrl.trimEnd('/')
            }

            preferencesManager.serverSettings.collect { settings ->
                if (settings.remoteUrl.isNotBlank()) {
                    serverUrl = settings.remoteUrl.trimEnd('/')
                }
            }
        }
    }

    fun printLabel() {
        val currentItem = item ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val result = printJobRouter.printItem(currentItem)) {
                is PrintResult.Success -> successMessage = result.message
                is PrintResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }
    
    fun fetchItem(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                item = itemRepository.getItem(id)
                itemCollections = runCatching { itemRepository.getItemCollections(id) }
                    .getOrDefault(emptyList())
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
                itemRepository.deleteItem(currentItem.id)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.w("ItemDetailViewModel", "Delete failed", e)
                errorMessage = "Failed to delete item: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
