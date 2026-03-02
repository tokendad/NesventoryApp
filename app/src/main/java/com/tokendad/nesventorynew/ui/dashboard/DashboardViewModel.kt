package com.tokendad.nesventorynew.ui.dashboard

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.preferences.ServerSettings
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.repository.ItemRepository
import com.tokendad.nesventorynew.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val systemRepository: SystemRepository,
    private val preferencesManager: PreferencesManager,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var statusMessage by mutableStateOf("Loading system status...")
    var itemStats by mutableStateOf("Fetching stats...")
    var localUrl by mutableStateOf("")
    var localSsid by mutableStateOf("")
    var prioritizeLocal by mutableStateOf(false)
    var connectionStatus by mutableStateOf("Unknown")
    
    // Server Settings
    var remoteUrl by mutableStateOf(com.tokendad.nesventorynew.util.Constants.DEFAULT_REMOTE_URL + "/")
    var availableSsids by mutableStateOf<List<String>>(emptyList())
    
    var theme by mutableStateOf("system")
    var remoteStatus by mutableStateOf<Boolean?>(null)
    var localStatus by mutableStateOf<Boolean?>(null)
    var aiStatus by mutableStateOf<Boolean?>(null)
    var aiStatusMessage by mutableStateOf<String?>(null)
    var showPermissionRationale by mutableStateOf(false)
    
    var recentItems by mutableStateOf<List<Item>>(emptyList())
    var searchQuery by mutableStateOf("")
    var isItemsLoading by mutableStateOf(false)

    init {
        loadSettings()
        loadDashboardData()
        scanWifiNetworks()
    }
    
    fun dismissPermissionRationale() {
        showPermissionRationale = false
    }
    
    fun scanWifiNetworks() {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifiManager != null) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context, 
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    val results = wifiManager.scanResults
                    @Suppress("DEPRECATION")
                    availableSsids = results
                        .mapNotNull { it.SSID }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    showPermissionRationale = false
                } else {
                    // Only show rationale if we really wanted to scan but couldn't
                    // For init, maybe we don't want to force it, but let's leave it controllable by UI
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun requestSsidScan() {
         if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            showPermissionRationale = true
        } else {
            scanWifiNetworks()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.serverSettings.collect { settings ->
                if (settings.remoteUrl.isNotBlank()) {
                    remoteUrl = settings.remoteUrl
                }
                localUrl = settings.localUrl
                localSsid = settings.localSsid
                prioritizeLocal = settings.prioritizeLocal
                theme = settings.theme
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                // Fetch Status
                val status = systemRepository.getStatus()
                val media = systemRepository.getMediaStats()
                statusMessage = "Server Version: ${status.version ?: "Unknown"}"
                itemStats = "Total Media Files: ${media.total_count}"
                connectionStatus = "Connected (Remote)"

                // Fetch Recent Items
                isItemsLoading = true
                val allItems = itemRepository.getItems()
                // Sort by created_at descending (assuming ISO 8601 string format)
                recentItems = allItems.sortedByDescending { it.created_at }
                    .take(5)
            } catch (e: Exception) {
                statusMessage = "Error connecting to Dashboard: ${e.localizedMessage}"
                connectionStatus = "Disconnected"
                // Keep recentItems empty if failed
            } finally {
                isItemsLoading = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun deleteItem(itemId: UUID) {
        viewModelScope.launch {
            isItemsLoading = true
            try {
                itemRepository.deleteItem(itemId)
                loadDashboardData()
            } catch (e: Exception) {
                android.util.Log.w("DashboardViewModel", "Failed to delete item", e)
            } finally {
                isItemsLoading = false
            }
        }
    }
    
    fun onRemoteUrlChange(url: String) {
        remoteUrl = url
        saveSettings()
    }

    fun onLocalUrlChange(url: String) {
        localUrl = url
        saveSettings()
    }
    
    fun onLocalSsidChange(ssid: String) {
        localSsid = ssid
        saveSettings()
    }
    
    fun onPrioritizeLocalChange(prioritize: Boolean) {
        prioritizeLocal = prioritize
        saveSettings()
    }
    
    fun onThemeChange(newTheme: String) {
        theme = newTheme
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val current = preferencesManager.serverSettings.first()
            preferencesManager.saveServerSettings(
                current.copy(
                    remoteUrl = remoteUrl,
                    localUrl = localUrl,
                    localSsid = localSsid,
                    prioritizeLocal = prioritizeLocal,
                    theme = theme
                )
            )
        }
    }

    fun testAndSaveConnection() {
        viewModelScope.launch {
            // 1. Save Settings First
            preferencesManager.saveServerSettings(
                ServerSettings(
                    remoteUrl = remoteUrl,
                    localUrl = localUrl,
                    localSsid = localSsid,
                    prioritizeLocal = prioritizeLocal,
                    theme = theme
                )
            )

            // 2. Run Connection Tests
            withContext(Dispatchers.IO) {
                // Test Remote
                if (remoteUrl.isBlank()) {
                    withContext(Dispatchers.Main) { remoteStatus = null }
                } else {
                    try {
                        val targetUrl = if (remoteUrl.endsWith("/")) remoteUrl else "$remoteUrl/"
                        val request = Request.Builder().url("${targetUrl}api/status").build()
                        val response = okHttpClient.newCall(request).execute()
                        val success = response.isSuccessful
                        response.close()
                        withContext(Dispatchers.Main) { remoteStatus = success }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) { remoteStatus = false }
                    }
                }

                // Test Local
                if (localUrl.isNotBlank()) {
                    try {
                        val targetUrl = if (localUrl.endsWith("/")) localUrl else "$localUrl/"
                        val request = Request.Builder().url("${targetUrl}api/status").build()
                        val response = okHttpClient.newCall(request).execute()
                        val success = response.isSuccessful
                        response.close()
                        withContext(Dispatchers.Main) { localStatus = success }
                    } catch (e: Exception) {
                        android.util.Log.w("DashboardViewModel", "Local server connectivity test failed", e)
                        withContext(Dispatchers.Main) { localStatus = false }
                    }
                }
                else {
                    withContext(Dispatchers.Main) { localStatus = null }
                }
            }
        }
    }

    fun testAIConnection() {
        viewModelScope.launch {
            try {
                val response = systemRepository.testAIConnection()
                aiStatus = response.overall_success
                aiStatusMessage = response.summary
            } catch (e: Exception) {
                aiStatus = false
                aiStatusMessage = "Connection Failed: ${e.localizedMessage}"
            }
        }
    }
}
