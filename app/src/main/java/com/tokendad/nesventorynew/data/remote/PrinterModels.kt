package com.tokendad.nesventorynew.data.remote

import java.util.UUID

data class PrinterConfig(
    val driver: String = "niimbot", // "niimbot" is the primary one supported
    val model: String = "D11",      // "D11", "D110", "B1", "B21", etc.
    val interface_type: String = "bluetooth", // "usb", "bluetooth", "serial", "tcp"
    val address: String? = null,    // MAC address (BLE) or Port (USB/Serial) or IP:Port
    val density: Int = 2,           // Print density/darkness
    val label_width: Int = 12,      // Label width in mm (optional, depending on backend)
    val label_height: Int = 40      // Label height in mm
)

data class PrintJobRequest(
    val entity_id: UUID,
    val entity_type: String, // "item" or "location"
    val quantity: Int = 1
)

data class PrinterStatus(
    val connected: Boolean,
    val message: String? = null
)