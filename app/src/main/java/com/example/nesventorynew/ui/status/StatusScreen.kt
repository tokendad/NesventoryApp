package com.example.nesventorynew.ui.status

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatusScreen(
    statusMessage: String,
    itemStats: String,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Server Status", style = MaterialTheme.typography.headlineMedium)
        
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("System Info", style = MaterialTheme.typography.titleMedium)
                Text(statusMessage, style = MaterialTheme.typography.bodyLarge)
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Inventory Stats", style = MaterialTheme.typography.titleMedium)
                Text(itemStats, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh Data")
        }
    }
}
