package com.tokendad.nesventory.data.remote

import com.google.gson.annotations.SerializedName

data class CsvImportResult(
    val imported_count: Int,
    val failed_count: Int,
    val errors: List<String> = emptyList()
)

data class EncirclePreviewItem(
    @SerializedName(value = "id", alternate = ["item_id", "identifier"])
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val location_name: String? = null
)

data class EncirclePreviewResult(
    val items: List<EncirclePreviewItem> = emptyList(),
    val location_count: Int = 0,
    val item_count: Int = 0
)

data class NetworkDiscoveredItem(
    @SerializedName(value = "id", alternate = ["item_id", "identifier"])
    val id: String? = null,
    val name: String? = null,
    @SerializedName(value = "ip_address", alternate = ["ip"])
    val ip_address: String? = null
)

data class NetworkScanResult(
    val discovered_items: List<NetworkDiscoveredItem> = emptyList(),
    val scan_duration_ms: Long = 0L
)
