package com.example.nesventorynew.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nesventorynew.data.remote.Item
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onItemClick: (UUID) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("NesVentory") })
                // Search Bar Area
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search items...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Newest Items", style = MaterialTheme.typography.titleLarge)

            if (viewModel.isItemsLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filter items based on search query if needed, 
                    // or just show recentItems filtered. 
                    // For now, let's just show recentItems matching the query if typed.
                    val displayItems = if (viewModel.searchQuery.isBlank()) {
                        viewModel.recentItems
                    } else {
                        viewModel.recentItems.filter { 
                            it.name.contains(viewModel.searchQuery, ignoreCase = true) 
                        }
                    }

                    items(displayItems) { item ->
                        DashboardItemRow(item, onClick = { onItemClick(item.id) })
                    }
                    
                    if (displayItems.isEmpty()) {
                        item {
                            Text("No items found.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardItemRow(item: Item, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            item.estimated_value?.let { Text("$$it", color = MaterialTheme.colorScheme.primary) }
            Text(
                "Created: ${item.created_at}", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}