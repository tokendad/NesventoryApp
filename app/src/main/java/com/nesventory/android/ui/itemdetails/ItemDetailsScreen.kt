package com.nesventory.android.ui.itemdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nesventory.android.data.model.Photo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    viewModel: ItemDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load item on first composition
    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Handle successful save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Item saved successfully!")
            viewModel.resetSavedState()
            // Reload item to reflect changes
            viewModel.loadItem(itemId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!uiState.isEditing) {
                        IconButton(onClick = { viewModel.startEditing() }) {
                            Icon(Icons.Filled.Edit, "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.item?.let { item ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Photos section
                    if (item.photos.isNotEmpty()) {
                        item {
                            Text(
                                text = "Photos",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(item.photos) { photo ->
                            PhotoCard(
                                photo = photo,
                                isEditing = uiState.isEditing,
                                onMoveUp = { viewModel.movePhotoUp(photo) },
                                onMoveDown = { viewModel.movePhotoDown(photo) },
                                isFirst = item.photos.first() == photo,
                                isLast = item.photos.last() == photo
                            )
                        }
                    }

                    // Item details section
                    item {
                        Text(
                            text = "Item Information",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    if (uiState.isEditing) {
                        // Editing mode
                        item {
                            OutlinedTextField(
                                value = uiState.editName,
                                onValueChange = { viewModel.updateEditName(it) },
                                label = { Text("Item Name *") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = uiState.editDescription,
                                onValueChange = { viewModel.updateEditDescription(it) },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = uiState.editBrand,
                                onValueChange = { viewModel.updateEditBrand(it) },
                                label = { Text("Brand") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = uiState.editModelNumber,
                                onValueChange = { viewModel.updateEditModelNumber(it) },
                                label = { Text("Model Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = uiState.editSerialNumber,
                                onValueChange = { viewModel.updateEditSerialNumber(it) },
                                label = { Text("Serial Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = uiState.editEstimatedValue,
                                onValueChange = { viewModel.updateEditEstimatedValue(it) },
                                label = { Text("Estimated Value") },
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("$") }
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelEditing() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = { viewModel.saveItem() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isLoading && uiState.editName.isNotBlank()
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                                        Text("Save", modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Note: Full item editing with backend integration will be implemented with PATCH /api/items/{id} endpoint.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Display mode
                        item {
                            DetailRow("Name", item.name)
                        }

                        item.description?.let { description ->
                            item {
                                DetailRow("Description", description)
                            }
                        }

                        item.brand?.let { brand ->
                            item {
                                DetailRow("Brand", brand)
                            }
                        }

                        item.modelNumber?.let { modelNumber ->
                            item {
                                DetailRow("Model Number", modelNumber)
                            }
                        }

                        item.serialNumber?.let { serialNumber ->
                            item {
                                DetailRow("Serial Number", serialNumber)
                            }
                        }

                        item.estimatedValue?.let { value ->
                            item {
                                DetailRow("Estimated Value", "$$value")
                            }
                        }

                        item.purchaseDate?.let { date ->
                            item {
                                DetailRow("Purchase Date", date)
                            }
                        }

                        item.purchasePrice?.let { price ->
                            item {
                                DetailRow("Purchase Price", "$$price")
                            }
                        }

                        // Tags
                        if (item.tags.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Tags",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                            }

                            item {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item.tags.forEach { tag ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        ) {
                                            Text(
                                                text = tag.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun PhotoCard(
    photo: Photo,
    isEditing: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = photo.path,
                contentDescription = "Item photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
            
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (photo.isPrimary) "Primary Photo" else "Photo",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    
                    Row {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = !isFirst
                        ) {
                            Icon(Icons.Filled.ArrowUpward, "Move up")
                        }
                        
                        IconButton(
                            onClick = onMoveDown,
                            enabled = !isLast
                        ) {
                            Icon(Icons.Filled.ArrowDownward, "Move down")
                        }
                    }
                }
            } else if (photo.isPrimary) {
                Text(
                    text = "Primary Photo",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
