package com.tokendad.nesventorynew.ui.itemdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.remote.Photo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val item = viewModel.item
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = item?.name ?: "Item Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (item != null) {
                            IconButton(onClick = { viewModel.printLabel() }) {
                                Icon(Icons.Default.Print, contentDescription = "Print Label")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Item")
                            }
                        }
                    }
                )
                // Tabs
                if (item != null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Details") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Media") }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && item == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null && item == null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (item != null) {
                when (selectedTab) {
                    0 -> DetailsTab(item, viewModel.serverUrl)
                    1 -> MediaTab(item.photos, viewModel.serverUrl)
                }
            }
        }
    }
}

@Composable
fun DetailsTab(item: Item, serverUrl: String) {
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
                "$serverUrl/${photo.path.removePrefix("/")}"
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

        // Serial Number
        if (!item.serial_number.isNullOrBlank()) {
            Text(text = "Serial Number", style = MaterialTheme.typography.titleMedium)
            Text(text = item.serial_number, style = MaterialTheme.typography.bodyLarge)
        }

        // Purchase Info
        if (item.purchase_price != null || item.purchase_date != null || item.retailer != null) {
            HorizontalDivider()
            Text(text = "Purchase Information", style = MaterialTheme.typography.titleMedium)

            item.purchase_price?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Purchase Price", style = MaterialTheme.typography.bodyMedium)
                    Text("$$it", style = MaterialTheme.typography.bodyMedium)
                }
            }
            item.purchase_date?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Purchase Date", style = MaterialTheme.typography.bodyMedium)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item.retailer?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Retailer", style = MaterialTheme.typography.bodyMedium)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Value
        item.estimated_value?.let {
            HorizontalDivider()
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

@Composable
fun MediaTab(photos: List<Photo>, serverUrl: String) {
    if (photos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No photos available for this item",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos) { photo ->
                val imageUrl = if (photo.path.startsWith("http")) {
                    photo.path
                } else {
                    "$serverUrl/${photo.path.removePrefix("/")}"
                }

                ElevatedCard {
                    Box {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = photo.filename,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Primary badge
                        if (photo.is_primary) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Primary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
