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
import com.example.nesventorynew.ui.status.StatusScreen
import java.util.UUID

@Composable
fun MainScreen(
    onItemClick: (UUID) -> Unit,
    onLocationClick: (UUID) -> Unit,
    onAddItemClick: () -> Unit,
    onAddLocationClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    // Sharing ViewModels between tabs if needed, but here tabs are mostly independent.
    // DashboardViewModel is reused for StatusScreen data
    val dashboardViewModel: DashboardViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Items") },
                    label = { Text("Items") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Locations") },
                    label = { Text("Locations") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Maint") },
                    label = { Text("Maint") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Status") },
                    label = { Text("Status") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = dashboardViewModel,
                    onItemClick = onItemClick
                )
                1 -> ItemsScreen(
                    onItemClick = onItemClick,
                    onAddItemClick = onAddItemClick
                )
                2 -> LocationsScreen(
                    onLocationClick = onLocationClick,
                    onAddLocationClick = onAddLocationClick
                )
                3 -> MaintenanceScreen()
                4 -> StatusScreen(
                    statusMessage = dashboardViewModel.statusMessage,
                    itemStats = dashboardViewModel.itemStats,
                    onRefresh = { dashboardViewModel.loadDashboardData() }
                )
            }
        }
    }
}
