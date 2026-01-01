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
    private val bluetoothManager: BluetoothPrinterManager
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
                
                // 1. Connect (Packet)
                val connectSuccess = bluetoothManager.sendData(NiimbotProtocol.createConnectPacket())
                if (!connectSuccess) throw Exception("Failed to send connect packet")
                
                kotlinx.coroutines.delay(1000) // Wait for ack

                // 2. Generate Bitmap
                val bitmap = createTestBitmap(model.width, model.width) // Square test
                
                // 3. Protocol Data
                val packets = NiimbotProtocol.createPrintData(bitmap, model, density = config.density)
                
                // 4. Send
                packets.forEachIndexed { index, packet -> 
                    val sent = bluetoothManager.sendData(packet)
                    if (!sent) throw Exception("Failed to send packet $index")
                    kotlinx.coroutines.delay(50) // Reduced delay slightly
                }
                
                // 5. Success
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

    private fun createTestBitmap(width: Int, height: Int): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 4f
        }
        // Border
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Text
        paint.style = android.graphics.Paint.Style.FILL
        paint.textSize = if(width < 100) 24f else 36f
        paint.isAntiAlias = true
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        // Center text
        val text = "TEST"
        val bounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = (width - bounds.width()) / 2f
        val y = (height + bounds.height()) / 2f
        
        canvas.drawText(text, x, y, paint)
        
        return bitmap
    }
}