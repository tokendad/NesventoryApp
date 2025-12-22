package com.example.nesventorynew.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToItems: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("NesVentory Dashboard") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("System Status", style = MaterialTheme.typography.titleMedium)
                    Text(viewModel.statusMessage, style = MaterialTheme.typography.bodyLarge)
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Inventory Summary", style = MaterialTheme.typography.titleMedium)
                    Text(viewModel.itemStats, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Button(
                onClick = { viewModel.loadDashboardData() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Data")
            Button(
                onClick = onNavigateToItems,
                modifier = Modifier.fillMaxWidth()
            ) {
                    Text("View Inventory Items")
              }
            }
        }
    }
}