package com.tokendad.nesventory.data.remote

import java.util.UUID

data class PrinterConfig(
    val driver: String = "niimbot", // "niimbot" is the primary one supported
    val model: String = "D11",      // "D11", "D110", "B1", "B21", etc.
    val interface_type: String = "bluetooth", // "usb", "bluetooth", "serial", "tcp"
    val address: String? = null,    // MAC address (BLE) or Port (USB/Serial) or IP:Port
    val density: Int = 2,           // Print density/darkness
    val label_width: Int = 12,      // Label width in mm (optional, depending on backend)
    val label_height: Int = 40,     // Label height in mm
    val enabled: Boolean = true,
    val connection_type: String = "bluetooth", // Upstream name for interface_type
    val bluetooth_type: String? = null, // "ble" or "classic"
    val label_length_mm: Int? = null,
    val print_direction: String? = null
)

data class PrintJobRequest(
    val entity_id: UUID,
    val entity_type: String, // "item" or "location"
    val quantity: Int = 1,
    val item_id: UUID? = null,
    val item_name: String? = null,
    val location_id: UUID? = null,
    val location_name: String? = null,
    val is_container: Boolean? = null,
    val label_length_mm: Int? = null
)

data class PrinterStatus(
    val connected: Boolean,
    val message: String? = null,
    val serial: String? = null,
    val soft_version: String? = null,
    val hard_version: String? = null
)

data class PrinterModelInfo(
    val value: String,
    val label: String,
    val max_width: Int
)

data class PrinterModelsResponse(
    val models: List<PrinterModelInfo>
)

data class PrinterTestResult(
    val success: Boolean,
    val message: String
)

data class PrinterProfile(
    val id: String,
    val name: String,
    val config: PrinterConfig,
    val is_active: Boolean = false
)

data class PrinterProfilesResponse(
    val profiles: List<PrinterProfile>
)

data class SystemPrinter(
    val name: String,
    val description: String? = null,
    val is_default: Boolean = false,
    val state: String? = null
)

data class SystemPrintersResponse(
    val printers: List<SystemPrinter>
)