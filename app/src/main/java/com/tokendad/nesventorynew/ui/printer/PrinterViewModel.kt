package com.tokendad.nesventorynew.ui.printer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.repository.PrinterRepository
import com.tokendad.nesventorynew.data.remote.PrinterConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(

    private val printerRepository: PrinterRepository,

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



    // Local printer models from enum
    val supportedLocalModels = PrinterModel.entries.map { it.displayName }

    // Selected local printer model
    var selectedLocalModel by mutableStateOf(PrinterModel.D11_H)
        private set

    // Server URL for QR codes
    private var serverUrl by mutableStateOf(com.tokendad.nesventorynew.util.Constants.DEFAULT_REMOTE_URL)

    val supportedInterfaces = listOf("bluetooth", "usb", "serial", "tcp", "cups")



    val scannedDevices = bluetoothManager.scannedDevices

    val connectionState = bluetoothManager.connectionState



    init {

        loadSettings()

        loadConfig()

        observeBluetoothData()

    }



    private fun loadSettings() {
        viewModelScope.launch {
            val settings = preferencesManager.serverSettings.first()
            printMethod = settings.printMethod
            PrinterModel.fromString(settings.localPrinterModel)?.let {
                selectedLocalModel = it
            }
            config = config.copy(density = settings.localPrinterDensity)
            if (settings.remoteUrl.isNotBlank()) {
                serverUrl = settings.remoteUrl.trimEnd('/')
            }
        }
    }

    fun onLocalModelChange(displayName: String) {
        PrinterModel.entries.find { it.displayName == displayName }?.let {
            selectedLocalModel = it
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
                config = printerRepository.getPrinterConfig()
                val modelsResponse = printerRepository.getPrinterModels()
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
                // Save local settings to preferences (always needed for local printing)
                val currentSettings = preferencesManager.serverSettings.first()
                preferencesManager.saveServerSettings(
                    currentSettings.copy(
                        printMethod = printMethod,
                        localPrinterModel = selectedLocalModel.name,
                        localPrinterDensity = config.density
                    )
                )

                // Only update server config if in server mode
                if (printMethod == "server") {
                    config = printerRepository.updatePrinterConfig(config)
                }

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
                val result = printerRepository.testPrinterConnection(config)
                if (result.success) {
                    successMessage = "Server printer test passed: ${result.message}"
                } else {
                    errorMessage = "Server printer test failed: ${result.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Server test failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun printTestLabel() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val result = printerRepository.printTestLabel()
                successMessage = result.message ?: "Test label printed successfully!"
            } catch (e: Exception) {
                errorMessage = "Test label print failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun testLocalPrint() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            android.util.Log.d("PrinterViewModel", "Starting local test print...")
            try {
                val model = selectedLocalModel
                android.util.Log.d("PrinterViewModel", "Using Model: ${model.displayName} (${model.name})")
                
                // 1. Connect (Packet)
                val connectSuccess = bluetoothManager.sendData(NiimbotProtocol.createConnectPacket())
                if (!connectSuccess) throw Exception("Failed to send connect packet")
                
                android.util.Log.d("PrinterViewModel", "Connect sent. Waiting 1s...")
                kotlinx.coroutines.delay(1000) // Wait for ack

                // 2. Generate Bitmap with model-specific dimensions
                android.util.Log.d("PrinterViewModel", "Generating Label Bitmap...")
                val bitmap = labelGenerator.generateLabel(
                    width = model.width,
                    height = model.defaultHeightPx,
                    title = "Test Label",
                    subtitle = "1234-5678-ABCD",
                    qrContent = "${serverUrl}/api/test",
                    iconType = "box"
                )
                android.util.Log.d("PrinterViewModel", "Bitmap Generated (${bitmap.width}x${bitmap.height}) for ${model.displayName}. Generating Packets...")
                
                // 3. Protocol Data
                val packets = NiimbotProtocol.createPrintData(bitmap, model, density = config.density)
                android.util.Log.d("PrinterViewModel", "Packets Generated: ${packets.size}. Sending...")

                // 4. Send with appropriate timing
                // B1 uses 15ms between rows, others use 20ms
                val rowDelay = if (model.protocol == ProtocolVariant.B1_CLASSIC) 15L else 20L

                packets.forEachIndexed { index, packet ->
                    val sent = bluetoothManager.sendData(packet)
                    if (!sent) throw Exception("Failed to send packet $index")
                    kotlinx.coroutines.delay(rowDelay)
                }

                // 5. Wait for print to complete (3 seconds as per backend)
                android.util.Log.d("PrinterViewModel", "Waiting for print to finish...")
                kotlinx.coroutines.delay(3000)
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