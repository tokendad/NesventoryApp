package com.tokendad.nesventorynew.ui.itemdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.repository.ItemRepository
import com.tokendad.nesventorynew.data.repository.PrinterRepository
import com.tokendad.nesventorynew.ui.printer.PrintJobExecutor
import com.tokendad.nesventorynew.ui.printer.PrinterModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

import com.tokendad.nesventorynew.data.preferences.PreferencesManager

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val printerRepository: PrinterRepository,
    private val preferencesManager: PreferencesManager,
    private val printJobExecutor: PrintJobExecutor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var item by mutableStateOf<Item?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    
    // Default to known base, update from prefs
    var serverUrl by mutableStateOf(com.tokendad.nesventorynew.util.Constants.DEFAULT_REMOTE_URL)
    private var printMethod by mutableStateOf("local")
    private var selectedModel by mutableStateOf(PrinterModel.D11_H)
    private var localDensity by mutableStateOf(3)

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
            preferencesManager.serverSettings.collect { settings ->
                if (settings.remoteUrl.isNotBlank()) {
                    serverUrl = settings.remoteUrl.trimEnd('/')
                }
                printMethod = settings.printMethod
                // Load selected local printer model
                PrinterModel.fromString(settings.localPrinterModel)?.let {
                    selectedModel = it
                }
                localDensity = settings.localPrinterDensity
            }
        }
    }

    fun printLabel() {
        if (printMethod == "server") {
            printLabelOnServer()
        } else {
            printLabelLocally()
        }
    }

    private fun printLabelOnServer() {
        val currentItem = item ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val request = com.tokendad.nesventorynew.data.remote.PrintJobRequest(
                    entity_id = currentItem.id,
                    entity_type = "item",
                    quantity = 1
                )
                printerRepository.printLabel(request)
                successMessage = "Print job sent to server!"
            } catch (e: Exception) {
                errorMessage = "Server print failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun printLabelLocally() {
        val currentItem = item ?: return

        if (!printJobExecutor.isConnected()) {
            errorMessage = "Printer not connected. Go to Printer Settings."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val qrUrl = "${serverUrl}/api/items/${currentItem.id}"
                printJobExecutor.printLabel(
                    labelText = currentItem.name,
                    labelSubtitle = currentItem.id.toString().take(8),
                    qrContent = qrUrl,
                    iconType = "box",
                    model = selectedModel,
                    density = localDensity
                )
                withContext(Dispatchers.Main) {
                    successMessage = "Label printed successfully!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Print failed: ${e.localizedMessage}"
                }
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
                item = itemRepository.getItem(id)
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
