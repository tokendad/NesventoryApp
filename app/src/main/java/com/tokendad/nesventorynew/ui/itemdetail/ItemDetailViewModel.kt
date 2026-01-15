package com.tokendad.nesventorynew.ui.itemdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.ui.printer.BluetoothPrinterManager
import com.tokendad.nesventorynew.ui.printer.LabelBitmapGenerator
import com.tokendad.nesventorynew.ui.printer.NiimbotProtocol
import com.tokendad.nesventorynew.ui.printer.PrinterModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

import com.tokendad.nesventorynew.data.preferences.PreferencesManager

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val api: NesVentoryApi,
    private val preferencesManager: PreferencesManager,
    private val bluetoothManager: BluetoothPrinterManager,
    private val labelGenerator: LabelBitmapGenerator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var item by mutableStateOf<Item?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    
    // Default to known base, update from prefs
    var serverUrl by mutableStateOf("https://nesdemo.welshrd.com")
    private var printMethod by mutableStateOf("local")

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
                api.printLabel(request)
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
        
        if (bluetoothManager.connectionState.value != 2) { // 2 = Connected
            errorMessage = "Printer not connected. Go to Printer Settings."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // 1. Get Config
                val config = api.getPrinterConfig()
                val model = PrinterModel.D110M_V4
                android.util.Log.d("ItemDetailViewModel", "Using Model: ${model.name}")

                // 2. Generate Bitmap
                // Width based on model. Height 472px (40mm @ 300dpi).
                val bitmap = labelGenerator.generateLabel(
                    width = model.width,
                    height = 472,
                    title = currentItem.name,
                    subtitle = currentItem.id.toString().take(8), // Short ID
                    qrContent = "https://nesventory.com/#/item/${currentItem.id}",
                    iconType = "box" 
                )

                // 3. Protocol Data
                val packets = NiimbotProtocol.createPrintData(bitmap, model, density = config.density)

                // 4. Send
                val connectSuccess = bluetoothManager.sendData(NiimbotProtocol.createConnectPacket())
                if (!connectSuccess) throw Exception("Failed to send connect packet")
                delay(500) // Wait for ack

                packets.forEachIndexed { index, packet ->
                    if (!bluetoothManager.sendData(packet)) throw Exception("Failed to send packet $index")
                    delay(20)
                }

                // 5. Wait and Finalize
                delay(5000)
                bluetoothManager.sendData(NiimbotProtocol.createPrintEndPacket())
                delay(100)
                bluetoothManager.sendData(NiimbotProtocol.createHeartbeatPacket())

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
