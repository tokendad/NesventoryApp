package com.tokendad.nesventory.ui.printer

import android.bluetooth.BluetoothProfile
import android.util.Log
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Shared local Bluetooth print logic extracted from ItemDetailViewModel
 * and LocationDetailViewModel to eliminate duplication.
 */
class PrintJobExecutor @Inject constructor(
    private val bluetoothManager: BluetoothPrinterManager,
    private val labelGenerator: LabelBitmapGenerator
) {
    private companion object {
        const val TAG = "PrintJobExecutor"
    }

    /** Check if the Bluetooth printer is currently connected. */
    fun isConnected(): Boolean =
        bluetoothManager.connectionState.value == BluetoothProfile.STATE_CONNECTED

    /**
     * Generate a label bitmap and send it to the connected Niimbot printer
     * via BLE using the appropriate protocol variant.
     *
     * Must be called from a coroutine on [kotlinx.coroutines.Dispatchers.IO].
     * Throws on any failure so the caller can handle error messaging.
     */
    suspend fun printLabel(
        labelText: String,
        labelSubtitle: String,
        qrContent: String,
        iconType: String,
        model: PrinterModel,
        density: Int
    ) {
        Log.d(TAG, "Using Model: ${model.displayName} (${model.name}), Density: $density")

        // Generate Bitmap with model-specific dimensions
        val bitmap = labelGenerator.generateLabel(
            width = model.width,
            height = model.defaultHeightPx,
            title = labelText,
            subtitle = labelSubtitle,
            qrContent = qrContent,
            iconType = iconType
        )
        Log.d(TAG, "Bitmap: ${bitmap.width}x${bitmap.height}")

        // Protocol Data
        val packets = NiimbotProtocol.createPrintData(bitmap, model, density = density)

        // Send connect packet
        val connectSuccess = bluetoothManager.sendData(NiimbotProtocol.createConnectPacket())
        if (!connectSuccess) throw Exception("Failed to send connect packet")
        delay(500) // Wait for ack

        // Send packets with appropriate timing
        // B1 uses 15ms between rows, others use 20ms
        val rowDelay = if (model.protocol == ProtocolVariant.B1_CLASSIC) 15L else 20L

        packets.forEachIndexed { index, packet ->
            if (!bluetoothManager.sendData(packet)) throw Exception("Failed to send packet $index")
            delay(rowDelay)
        }

        // Wait for print to complete (3 seconds as per backend)
        delay(3000)
        bluetoothManager.sendData(NiimbotProtocol.createPrintEndPacket())
        delay(100)
        bluetoothManager.sendData(NiimbotProtocol.createHeartbeatPacket())
    }
}
