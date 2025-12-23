package com.tokendad.nesventorynew.ui.itemdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventorynew.data.remote.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val item = viewModel.item
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(onSuccess = onBackClick)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = item?.name ?: "Item Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (item != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Item")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (item != null) {
                ItemDetailContent(item)
            }
        }
    }
}

@Composable
fun ItemDetailContent(item: Item) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primary Photo
        val primaryPhoto = item.photos.find { it.is_primary }
        primaryPhoto?.let { photo ->
            val imageUrl = if (photo.path.startsWith("http")) {
                photo.path
            } else {
                 // Assuming relative path from base URL. 
                 // Note: Hardcoding base URL here is not ideal, but quick for now.
                 // Ideally, we should get it from a config or helper.
                 "https://nesdemo.welshrd.com/${photo.path.removePrefix("/")}"
            }
            
            AsyncImage(
                model = imageUrl,
                contentDescription = "Primary Photo for ${item.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Name
        Text(text = item.name, style = MaterialTheme.typography.headlineMedium)
        
        // Brand & Model
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item.brand?.let {
                AssistChip(onClick = {}, label = { Text("Brand: $it") })
            }
            item.model_number?.let {
                 AssistChip(onClick = {}, label = { Text("Model: $it") })
            }
        }

        HorizontalDivider()

        // Description
        if (!item.description.isNullOrBlank()) {
            Text(text = "Description", style = MaterialTheme.typography.titleMedium)
            Text(text = item.description, style = MaterialTheme.typography.bodyLarge)
        }

        // Value
        item.estimated_value?.let {
            Text(text = "Estimated Value", style = MaterialTheme.typography.titleMedium)
            Text(text = "$$it", style = MaterialTheme.typography.bodyLarge)
        }

        HorizontalDivider()

        // Timestamps
        Column {
            Text(text = "Created: ${item.created_at}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Updated: ${item.updated_at}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
