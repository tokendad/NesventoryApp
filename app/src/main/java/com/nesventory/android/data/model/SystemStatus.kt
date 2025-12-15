package com.nesventory.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * System status response from the NesVentory API.
 */
@Serializable
data class SystemStatus(
    val version: String,
    @SerialName("ai_configured")
    val aiConfigured: Boolean = false,
    @SerialName("gdrive_configured")
    val gdriveConfigured: Boolean = false,
    @SerialName("plugins_configured")
    val pluginsConfigured: Boolean = false,
    @SerialName("database_type")
    val databaseType: String? = null
)

/**
 * Plugin information from the NesVentory API.
 */
@Serializable
data class Plugin(
    val id: String,
    val name: String,
    val description: String? = null,
    val version: String? = null,
    val endpoint: String,
    @SerialName("api_key")
    val apiKey: String? = null,
    val priority: Int = 100,
    val enabled: Boolean = true,
    @SerialName("supports_image_processing")
    val supportsImageProcessing: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

/**
 * Plugin status response.
 */
@Serializable
data class PluginStatus(
    val configured: Boolean,
    val plugins: List<Plugin> = emptyList()
)

/**
 * AI status response.
 */
@Serializable
data class AIStatus(
    val configured: Boolean,
    @SerialName("gemini_configured")
    val geminiConfigured: Boolean = false,
    @SerialName("plugin_configured")
    val pluginConfigured: Boolean = false
)
