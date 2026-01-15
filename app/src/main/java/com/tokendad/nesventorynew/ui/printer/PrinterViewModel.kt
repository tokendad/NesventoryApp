package com.tokendad.nesventorynew.ui.printer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.data.remote.PrinterConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(

    private val api: NesVentoryApi,

    private val preferencesManager: PreferencesManager,

    private val bluetoothManager: BluetoothPrinterManager,

    private val labelGenerator: LabelBitmapGenerator

) : ViewModel() {



    var config by mutableStateOf(PrinterConfig())

        private set

        

    var printMethod by mutableStateOf("local") // "local" or "server"



    var serverPrinterModels by mutableStateOf<List<com.tokendad.nesventorynew.data.remote.PrinterModelInfo>>(emptyList())

        private set



    var rfidInfo by mutableStateOf<NiimbotProtocol.RfidInfo?>(null)

        private set



    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    var successMessage by mutableStateOf<String?>(null)



    val supportedModels = listOf("D110M_V4")

    val supportedInterfaces = listOf("bluetooth", "usb", "serial", "tcp")



    val scannedDevices = bluetoothManager.scannedDevices

    val connectionState = bluetoothManager.connectionState



    init {

        loadSettings()

        loadConfig()

        observeBluetoothData()

    }



    private fun loadSettings() {

        viewModelScope.launch {

            preferencesManager.serverSettings.collect { settings ->

                printMethod = settings.printMethod

            }

        }

    }



    private fun observeBluetoothData() {
        viewModelScope.launch {
            bluetoothManager.receivedData.collect { packet ->
                val rfid = NiimbotProtocol.parseRfidResponse(packet)
                if (rfid != null) {
                    rfidInfo = rfid
                    android.util.Log.d("PrinterViewModel", "RFID Info Parsed: $rfid")
                }
            }
        }
    }
    
    fun checkPaper() {
        viewModelScope.launch {
            try {
                val packet = NiimbotProtocol.createGetRfidPacket()
                val sent = bluetoothManager.sendData(packet)
                if (sent) {
                    android.util.Log.d("PrinterViewModel", "RFID Request Sent")
                } else {
                    errorMessage = "Failed to send RFID request"
                }
            } catch (e: Exception) {
                errorMessage = "Error checking paper: ${e.message}"
            }
        }
    }

    private fun loadConfig() {
        viewModelScope.launch {
            isLoading = true
            try {
                config = api.getPrinterConfig()
                val modelsResponse = api.getPrinterModels()
                serverPrinterModels = modelsResponse.models
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
                
                // Save printMethod to preferences
                val currentSettings = preferencesManager.serverSettings.first()
                preferencesManager.saveServerSettings(currentSettings.copy(printMethod = printMethod))

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
        if (printMethod == "server") {
            testServerPrint()
        } else {
            testLocalPrint()
        }
    }

    private fun testServerPrint() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // Since there's no direct 'test print' endpoint, we verify connection first
                api.updatePrinterConfig(config) // Ensure server has latest config
                // Then try to get status as a 'test'
                val status = api.getPrinterStatus()
                if (status.connected) {
                    successMessage = "Server-side printer connected! ${status.message ?: ""}"
                } else {
                    errorMessage = "Server-side printer connection failed: ${status.message ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                errorMessage = "Server test failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun testLocalPrint() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            android.util.Log.d("PrinterViewModel", "Starting local test print...")
            try {
                val model = PrinterModel.D110M_V4
                android.util.Log.d("PrinterViewModel", "Using Model: ${model.name}")
                
                // 1. Connect (Packet)
                val connectSuccess = bluetoothManager.sendData(NiimbotProtocol.createConnectPacket())
                if (!connectSuccess) throw Exception("Failed to send connect packet")
                
                android.util.Log.d("PrinterViewModel", "Connect sent. Waiting 1s...")
                kotlinx.coroutines.delay(1000) // Wait for ack

                // 2. Generate Bitmap
                android.util.Log.d("PrinterViewModel", "Generating Label Bitmap...")
                // 40mm @ 300dpi = ~472px length
                val height = 472 
                val bitmap = labelGenerator.generateLabel(
                    width = model.width,
                    height = height,
                    title = "Test Label",
                    subtitle = "1234-5678-ABCD",
                    qrContent = "https://nesventory.com/test",
                    iconType = "box"
                )
                android.util.Log.d("PrinterViewModel", "Bitmap Generated (${bitmap.width}x${bitmap.height}). Generating Packets...")
                
                // 3. Protocol Data
                val packets = NiimbotProtocol.createPrintData(bitmap, model, density = config.density)
                android.util.Log.d("PrinterViewModel", "Packets Generated: ${packets.size}. Sending...")
                
                // 4. Send
                packets.forEachIndexed { index, packet -> 
                    val sent = bluetoothManager.sendData(packet)
                    if (!sent) throw Exception("Failed to send packet $index")
                    kotlinx.coroutines.delay(20) // Fast packet sending
                }
                
                // 5. Wait and Finalize
                android.util.Log.d("PrinterViewModel", "Waiting for print to finish...")
                kotlinx.coroutines.delay(5000) // Wait 5 seconds for print to complete
                val endPacket = NiimbotProtocol.createPrintEndPacket()
                bluetoothManager.sendData(endPacket)
                kotlinx.coroutines.delay(100)
                val heartbeatPacket = NiimbotProtocol.createHeartbeatPacket()
                bluetoothManager.sendData(heartbeatPacket)

                // 6. Success
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.util.Log.d("PrinterViewModel", "Test print sent successfully")
                    successMessage = "Test print sent!"
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