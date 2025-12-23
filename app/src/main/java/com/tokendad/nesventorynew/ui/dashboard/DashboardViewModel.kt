package com.tokendad.nesventorynew.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.preferences.PreferencesManager
import com.example.nesventorynew.data.preferences.ServerSettings
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val api: NesVentoryApi,
    private val preferencesManager: PreferencesManager,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    var statusMessage by mutableStateOf("Loading system status...")
    var itemStats by mutableStateOf("Fetching stats...")
    var localUrl by mutableStateOf("")
    var localSsid by mutableStateOf("")
    var prioritizeLocal by mutableStateOf(false)
    var connectionStatus by mutableStateOf("Unknown")
    
    var theme by mutableStateOf("system")
    var remoteStatus by mutableStateOf<Boolean?>(null)
    var localStatus by mutableStateOf<Boolean?>(null)
    
    var recentItems by mutableStateOf<List<Item>>(emptyList())
    var searchQuery by mutableStateOf("")
    var isItemsLoading by mutableStateOf(false)

    init {
        loadSettings()
        loadDashboardData()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.serverSettings.collect { settings ->
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
                val status = api.getStatus()
                val media = api.getMediaStats()
                statusMessage = "Server Version: ${status["version"] ?: "Unknown"}"
                itemStats = "Total Media Files: ${media["total_count"] ?: 0}"
                connectionStatus = "Connected (Remote)"

                // Fetch Recent Items
                isItemsLoading = true
                val allItems = api.getItems()
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
                api.deleteItem(itemId)
                loadDashboardData()
            } catch (_: Exception) {
                // error message or toast
            } finally {
                isItemsLoading = false
            }
        }
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
            preferencesManager.saveServerSettings(
                ServerSettings(
                    remoteUrl = "https://nesdemo.welshrd.com/", // Default
                    localUrl = localUrl,
                    localSsid = localSsid,
                    prioritizeLocal = prioritizeLocal,
                    theme = theme
                )
            )
        }
    }
    
    fun testConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            // Test Remote
            try {
                // Assuming api is configured for remote by default or current context
                api.getStatus()
                withContext(Dispatchers.Main) { remoteStatus = true }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { remoteStatus = false }
            }

            // Test Local
            if (localUrl.isNotBlank()) {
                try {
                    val request = Request.Builder().url("$localUrl/api/status").build()
                    val response = okHttpClient.newCall(request).execute()
                    val success = response.isSuccessful
                    response.close()
                    withContext(Dispatchers.Main) { localStatus = success }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) { localStatus = false }
                }
            } else {
                withContext(Dispatchers.Main) { localStatus = null }
            }
        }
    }
}
