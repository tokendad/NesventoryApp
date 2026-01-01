package com.tokendad.nesventorynew.ui.locationdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val bluetoothManager: BluetoothPrinterManager,
    private val labelGenerator: LabelBitmapGenerator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var location by mutableStateOf<Location?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

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
    }

    fun printLabel() {
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
                val model = when (config.model) {
                    "D11_H" -> PrinterModel.D11_H
                    "D110M_V4" -> PrinterModel.D110M_V4
                    else -> PrinterModel.D110
                }

                val bitmap = labelGenerator.generateLabel(
                    width = model.width,
                    height = 150,
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

                if (model == PrinterModel.D110M_V4) {
                    delay(5000)
                    bluetoothManager.sendData(NiimbotProtocol.createPrintEndPacket())
                    delay(100)
                    bluetoothManager.sendData(NiimbotProtocol.createHeartbeatPacket())
                }

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
    
    // ... (rest)

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
