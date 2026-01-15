package com.tokendad.nesventorynew.ui.locationdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.remote.Location
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

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val api: NesVentoryApi,
    private val preferencesManager: PreferencesManager,
    private val bluetoothManager: BluetoothPrinterManager,
    private val labelGenerator: LabelBitmapGenerator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var location by mutableStateOf<Location?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    
    private var printMethod by mutableStateOf("local")

    init {
        val locationIdString: String? = savedStateHandle["locationId"]
        if (locationIdString != null) {
             try {
                 val id = UUID.fromString(locationIdString)
                 fetchLocation(id)
             } catch (e: IllegalArgumentException) {
                 errorMessage = "Invalid Location ID format"
             }
        }
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.serverSettings.collect { settings ->
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
        val currentLocation = location ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val request = com.tokendad.nesventorynew.data.remote.PrintJobRequest(
                    entity_id = currentLocation.id,
                    entity_type = "location",
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
        val currentLocation = location ?: return
        
        if (bluetoothManager.connectionState.value != 2) {
            errorMessage = "Printer not connected. Go to Printer Settings."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val config = api.getPrinterConfig()
                val model = PrinterModel.D110M_V4
                android.util.Log.d("LocationDetailViewModel", "Using Model: ${model.name}")

                val bitmap = labelGenerator.generateLabel(
                    width = model.width,
                    height = 472,
                    title = currentLocation.name,
                    subtitle = currentLocation.id.toString().take(8),
                    qrContent = "https://nesventory.com/#/location/${currentLocation.id}",
                    iconType = "location"
                )

                val packets = NiimbotProtocol.createPrintData(bitmap, model, density = config.density)

                val connectSuccess = bluetoothManager.sendData(NiimbotProtocol.createConnectPacket())
                if (!connectSuccess) throw Exception("Failed to send connect packet")
                delay(500)

                packets.forEachIndexed { index, packet ->
                    if (!bluetoothManager.sendData(packet)) throw Exception("Failed to send packet $index")
                    delay(20)
                }

                // Wait and Finalize
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
    
    fun fetchLocation(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                location = api.getLocation(id)
            } catch (e: Exception) {
                errorMessage = "Failed to load location details: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteLocation(onSuccess: () -> Unit) {
        val currentLocation = location ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                api.deleteLocation(currentLocation.id)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to delete location: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }
}
