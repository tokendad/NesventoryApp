package com.tokendad.nesventorynew.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventorynew.data.remote.Item
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    onItemClick: (UUID) -> Unit = {},
    onAddItemClick: () -> Unit = {},
    onExit: () -> Unit = {},
    viewModel: ItemsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Inventory", style = MaterialTheme.typography.titleMedium) },
                    actions = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                        }
                    }
                )
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .height(50.dp),
                    placeholder = { Text("Search items...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItemClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(viewModel.filteredItems) { item ->
                    val locationName = item.location_id?.let { viewModel.locationNames[it] }
                    ItemRow(item, locationName, onClick = { onItemClick(item.id) })
                }
            }
        }
    }
}

@Composable
fun ItemRow(item: Item, locationName: String?, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Primary Photo
            val primaryPhoto = item.photos.find { it.is_primary }
            val imageUrl = primaryPhoto?.let { photo ->
                if (photo.path.startsWith("http")) photo.path 
                else "https://nesdemo.welshrd.com/${photo.path.removePrefix("/")}"
            }

            Card(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Img", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(item.name, style = MaterialTheme.typography.titleSmall)
                locationName?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                } ?: run {
                    Text("No Location", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}