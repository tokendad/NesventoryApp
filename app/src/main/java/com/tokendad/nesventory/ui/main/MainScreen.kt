package com.tokendad.nesventory.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventory.ui.dashboard.DashboardScreen
import com.tokendad.nesventory.ui.dashboard.DashboardViewModel
import com.tokendad.nesventory.ui.items.ItemsScreen
import com.tokendad.nesventory.ui.locations.LocationsScreen
import com.tokendad.nesventory.ui.maintenance.MaintenanceScreen
import com.tokendad.nesventory.ui.collections.CollectionsScreen
import java.util.UUID

@Composable
fun MainScreen(
    onItemClick: (UUID) -> Unit,
    onLocationClick: (UUID) -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (UUID) -> Unit,
    onAddLocationClick: () -> Unit,
    onEditLocationClick: (UUID) -> Unit,
    onProfileClick: () -> Unit,
    onServerSettingsClick: () -> Unit,
    onPrinterSettingsClick: () -> Unit,
    onExit: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val dashboardViewModel: DashboardViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            androidx.compose.foundation.layout.Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Items") },
                        label = { Text("Items", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Place, contentDescription = "Locations") },
                        label = { Text("Locs", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Collections, contentDescription = "Collections") },
                        label = { Text("Collect", style = MaterialTheme.typography.labelSmall, maxLines = 1) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Maint") },
                        label = { Text("Maint", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = dashboardViewModel,
                    onItemClick = onItemClick,
                    onEditItemClick = onEditItemClick,
                    onProfileClick = onProfileClick,
                    onServerSettingsClick = onServerSettingsClick,
                    onExit = onExit
                )
                1 -> ItemsScreen(
                    onItemClick = onItemClick,
                    onAddItemClick = onAddItemClick,
                    onEditItemClick = onEditItemClick,
                    onExit = onExit
                )
                2 -> LocationsScreen(
                    onLocationClick = onLocationClick,
                    onAddLocationClick = onAddLocationClick,
                    onEditLocationClick = onEditLocationClick,
                    onExit = onExit
                )
                3 -> CollectionsScreen()
                4 -> MaintenanceScreen(
                    onExit = onExit
                )
            }
        }
    }
}
