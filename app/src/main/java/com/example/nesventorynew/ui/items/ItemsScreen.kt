package com.example.nesventorynew.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nesventorynew.data.remote.Item
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    onItemClick: (UUID) -> Unit = {},
    viewModel: ItemsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My Inventory") }) }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.items) { item ->
                    ItemRow(item, onClick = { onItemClick(item.id) })
                }
            }
        }
    }
}

@Composable
fun ItemRow(item: Item, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            item.brand?.let { Text("Brand: $it", style = MaterialTheme.typography.bodyMedium) }
            item.estimated_value?.let { Text("Value: $$it", color = MaterialTheme.colorScheme.primary) }
        }
    }
}