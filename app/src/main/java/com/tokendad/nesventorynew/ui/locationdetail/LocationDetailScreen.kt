package com.tokendad.nesventorynew.ui.locationdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventorynew.data.remote.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    onBackClick: () -> Unit,
    viewModel: LocationDetailViewModel = hiltViewModel()
) {
    val location = viewModel.location
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Location") },
            text = { Text("Are you sure you want to delete this location? This will likely cascade delete related items/sub-locations.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLocation(onSuccess = onBackClick)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = location?.name ?: "Location Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (location != null) {
                        IconButton(onClick = { viewModel.printLabel() }) {
                            Icon(Icons.Default.Print, contentDescription = "Print Label")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Location")
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
            } else if (location != null) {
                LocationDetailContent(location)
            }
        }
    }
}

@Composable
fun LocationDetailContent(location: Location) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primary Photo
        val primaryPhoto = location.location_photos.find { it.is_primary }
        primaryPhoto?.let { photo ->
            val imageUrl = if (photo.path.startsWith("http")) {
                photo.path
            } else {
                 "https://nesdemo.welshrd.com/${photo.path.removePrefix("/")}"
            }
            
            AsyncImage(
                model = imageUrl,
                contentDescription = "Primary Photo for ${location.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Name
        Text(text = location.name, style = MaterialTheme.typography.headlineMedium)
        
        // Friendly Name
        location.friendly_name?.let {
            Text(text = "($it)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (location.is_primary_location) {
                AssistChip(onClick = {}, label = { Text("Primary Location") })
            }
            if (location.is_container) {
                AssistChip(onClick = {}, label = { Text("Container") })
            }
        }

        HorizontalDivider()

        // Description
        if (!location.description.isNullOrBlank()) {
            Text(text = "Description", style = MaterialTheme.typography.titleMedium)
            Text(text = location.description, style = MaterialTheme.typography.bodyLarge)
        }
        
        // Address
        if (!location.address.isNullOrBlank()) {
            Text(text = "Address", style = MaterialTheme.typography.titleMedium)
            Text(text = location.address, style = MaterialTheme.typography.bodyLarge)
        }

        // Values
        if (location.estimated_property_value != null || location.estimated_value_with_items != null) {
            HorizontalDivider()
            
            location.estimated_property_value?.let {
                Text(text = "Property Value", style = MaterialTheme.typography.titleMedium)
                Text(text = "$$it", style = MaterialTheme.typography.bodyLarge)
            }
            
            location.estimated_value_with_items?.let {
                Text(text = "Value with Items", style = MaterialTheme.typography.titleMedium)
                Text(text = "$$it", style = MaterialTheme.typography.bodyLarge)
            }
        }

        HorizontalDivider()

        // Timestamps
        Column {
            Text(text = "Created: ${location.created_at}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Updated: ${location.updated_at}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
