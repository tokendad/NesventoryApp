package com.tokendad.nesventorynew.ui.printer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.data.remote.PrinterConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val api: NesVentoryApi,
    private val bluetoothManager: BluetoothPrinterManager,
    private val labelGenerator: LabelBitmapGenerator
) : ViewModel() {

    var config by mutableStateOf(PrinterConfig())
        private set
        
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    val supportedModels = listOf("D11", "D110", "D11_H", "D110M_V4", "B1", "B18", "B21")
    val supportedInterfaces = listOf("bluetooth", "usb", "serial", "tcp")

    val scannedDevices = bluetoothManager.scannedDevices
    val connectionState = bluetoothManager.connectionState

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            isLoading = true
            try {
                config = api.getPrinterConfig()
            } catch (e: Exception) {
                errorMessage = "Failed to load printer config: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun onModelChange(model: String) {
        config = config.copy(model = model)
    }

    fun onInterfaceChange(interfaceType: String) {
        config = config.copy(interface_type = interfaceType)
    }

    fun onAddressChange(address: String) {
        config = config.copy(address = address)
    }
    
    fun onDensityChange(density: Int) {
        config = config.copy(density = density)
    }

    fun saveConfig() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                config = api.updatePrinterConfig(config)
                successMessage = "Printer configuration saved successfully!"
            } catch (e: Exception) {
                errorMessage = "Failed to save config: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun startScan() {
        bluetoothManager.startScan()
    }

    fun connect(device: android.bluetooth.BluetoothDevice) {
        bluetoothManager.connect(device)
    }
    
    fun disconnect() {
        bluetoothManager.disconnect()
    }

    fun printTest() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            android.util.Log.d("PrinterViewModel", "Starting test print...")
            try {
                // Determine model
                val model = when (config.model) {
                    "D11_H" -> PrinterModel.D11_H
                    "D110M_V4" -> PrinterModel.D110M_V4
                    else -> PrinterModel.D110 // Default to D110/Standard
                }
                android.util.Log.d("PrinterViewModel", "Selected Model: ${model.name} (Config: ${config.model})")
                
                // 1. Connect (Packet)
                val connectSuccess = bluetoothManager.sendData(NiimbotProtocol.createConnectPacket())
                if (!connectSuccess) throw Exception("Failed to send connect packet")
                
                kotlinx.coroutines.delay(1000) // Wait for ack

                // 2. Generate Bitmap
                // Width = Model width. Height = Arbitrary (e.g. 100-150 for 40-50mm label)
                val height = 150 
                val bitmap = labelGenerator.generateLabel(
                    width = model.width,
                    height = height,
                    title = "Test Label",
                    subtitle = "1234-5678-ABCD",
                    qrContent = "https://nesventory.com/test",
                    iconType = "box"
                )
                
                // 3. Protocol Data
                val packets = NiimbotProtocol.createPrintData(bitmap, model, density = config.density)
                
                // 4. Send
                packets.forEachIndexed { index, packet -> 
                    val sent = bluetoothManager.sendData(packet)
                    if (!sent) throw Exception("Failed to send packet $index")
                    kotlinx.coroutines.delay(20) // Fast packet sending
                }
                
                // 5. Wait and Finalize (Specific for V4/V5)
                if (model == PrinterModel.D110M_V4) {
                    android.util.Log.d("PrinterViewModel", "Waiting for print to finish (V4)...")
                    kotlinx.coroutines.delay(5000) // Wait 5 seconds for print to complete
                    val endPacket = NiimbotProtocol.createPrintEndPacket()
                    bluetoothManager.sendData(endPacket)
                    kotlinx.coroutines.delay(100)
                    val heartbeatPacket = NiimbotProtocol.createHeartbeatPacket()
                    bluetoothManager.sendData(heartbeatPacket)
                }
                
                // 6. Success
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.util.Log.d("PrinterViewModel", "Test print sent successfully")
                    successMessage = "Test print sent! (${model.name})"
                }
            } catch (e: Exception) {
                android.util.Log.e("PrinterViewModel", "Print failed", e)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    errorMessage = "Print failed: ${e.localizedMessage}"
                }
            }
        }
    }
}