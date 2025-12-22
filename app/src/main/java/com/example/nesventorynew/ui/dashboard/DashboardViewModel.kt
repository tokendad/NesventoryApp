package com.example.nesventorynew.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.remote.Item
import com.example.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    var statusMessage by mutableStateOf("Loading system status...")
    var itemStats by mutableStateOf("Fetching stats...")
    
    var recentItems by mutableStateOf<List<Item>>(emptyList())
    var searchQuery by mutableStateOf("")
    var isItemsLoading by mutableStateOf(false)

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                // Fetch Status
                val status = api.getStatus()
                val media = api.getMediaStats()
                statusMessage = "Server Version: ${status["version"] ?: "Unknown"}"
                itemStats = "Total Media Files: ${media["total_count"] ?: 0}"

                // Fetch Recent Items
                isItemsLoading = true
                val allItems = api.getItems()
                // Sort by created_at descending (assuming ISO 8601 string format)
                recentItems = allItems.sortedByDescending { it.created_at }
                    .take(5)
            } catch (e: Exception) {
                statusMessage = "Error connecting to Dashboard: ${e.localizedMessage}"
                // Keep recentItems empty if failed
            } finally {
                isItemsLoading = false
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        searchQuery = query
        // Filter logic could go here if we were filtering local list
    }
}