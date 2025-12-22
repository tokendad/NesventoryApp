package com.example.nesventorynew.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val status = api.getStatus()
                val media = api.getMediaStats()

                statusMessage = "Server Version: ${status["version"] ?: "Unknown"}"
                itemStats = "Total Media Files: ${media["total_count"] ?: 0}"
            } catch (e: Exception) {
                statusMessage = "Error connecting to Dashboard"
            }
        }
    }
}