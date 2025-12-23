package com.example.nesventorynew.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nesventorynew.ui.dashboard.DashboardScreen
import com.example.nesventorynew.ui.dashboard.DashboardViewModel
import com.example.nesventorynew.ui.items.ItemsScreen
import com.example.nesventorynew.ui.locations.LocationsScreen
import com.example.nesventorynew.ui.maintenance.MaintenanceScreen
import com.example.nesventorynew.ui.server.ServerScreen
import java.util.UUID

@Composable
fun MainScreen(
    onItemClick: (UUID) -> Unit,
    onLocationClick: (UUID) -> Unit,
    onAddItemClick: () -> Unit,
    onAddLocationClick: () -> Unit,
    onExit: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    // Sharing ViewModels between tabs if needed, but here tabs are mostly independent.
    // DashboardViewModel is reused for StatusScreen data
    val dashboardViewModel: DashboardViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
// ... (existing NavigationBar code)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = dashboardViewModel,
                    onItemClick = onItemClick,
                    onExit = onExit
                )
                1 -> ItemsScreen(
                    onItemClick = onItemClick,
                    onAddItemClick = onAddItemClick,
                    onExit = onExit
                )
                2 -> LocationsScreen(
                    onLocationClick = onLocationClick,
                    onAddLocationClick = onAddLocationClick,
                    onExit = onExit
                )
                3 -> MaintenanceScreen()
                4 -> ServerScreen(
                    localUrl = dashboardViewModel.localUrl,
// ...

)
            }
        }
    }
                