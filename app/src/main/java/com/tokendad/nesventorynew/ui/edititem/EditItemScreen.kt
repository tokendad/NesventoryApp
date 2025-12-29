package com.tokendad.nesventorynew.ui.edititem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventorynew.ui.additem.CompactTextField
import com.tokendad.nesventorynew.ui.maintenance.MaintenanceTaskRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    onBackClick: () -> Unit,
    onItemUpdated: () -> Unit,
    viewModel: EditItemViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Details", "Media", "Maintenance")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Edit Item", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = { viewModel.enrichData() }, enabled = !viewModel.isLoading) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Enrich")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> DetailsTab(viewModel, onItemUpdated)
                1 -> MediaTab(viewModel)
                2 -> MaintenanceTab(viewModel)
            }
        }
    }
}

@Composable
fun DetailsTab(viewModel: EditItemViewModel, onItemUpdated: () -> Unit) {
    val highlightColor = Color(0xFFFF0000)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Name & Brand
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = "Name *",
                modifier = Modifier.weight(1.2f)
            )
            CompactTextField(
                value = viewModel.brand,
                onValueChange = { viewModel.brand = it },
                label = "Brand",
                modifier = Modifier.weight(0.8f),
                textColor = if (viewModel.isFieldModified("brand", viewModel.brand)) highlightColor else Color.Unspecified
            )
        }

        // Model & Serial
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactTextField(
                value = viewModel.modelNumber,
                onValueChange = { viewModel.modelNumber = it },
                label = "Model",
                modifier = Modifier.weight(1f),
                textColor = if (viewModel.isFieldModified("modelNumber", viewModel.modelNumber)) highlightColor else Color.Unspecified
            )
            CompactTextField(
                value = viewModel.serialNumber,
                onValueChange = { viewModel.serialNumber = it },
                label = "Serial",
                modifier = Modifier.weight(1f),
                textColor = if (viewModel.isFieldModified("serialNumber", viewModel.serialNumber)) highlightColor else Color.Unspecified
            )
        }

        // Retailer & Location
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactTextField(
                value = viewModel.retailer,
                onValueChange = { viewModel.retailer = it },
                label = "Retailer",
                modifier = Modifier.weight(1f)
            )
            
            // Location Selector
            var locationExpanded by remember { mutableStateOf(false) }
            val selectedLocationName = viewModel.availableLocations
                .find { it.id == viewModel.selectedLocationId }?.name ?: ""

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedLocationName,
                    onValueChange = {},
                    label = { Text("Location", style = MaterialTheme.typography.bodySmall) },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    trailingIcon = {
                        IconButton(onClick = { locationExpanded = !locationExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
                DropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    viewModel.availableLocations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                viewModel.selectedLocationId = loc.id
                                locationExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Price, Value, Date
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactTextField(
                value = viewModel.purchasePrice,
                onValueChange = { viewModel.purchasePrice = it },
                label = "Price",
                modifier = Modifier.weight(0.8f)
            )
            CompactTextField(
                value = viewModel.estimatedValue,
                onValueChange = { viewModel.estimatedValue = it },
                label = "Value",
                modifier = Modifier.weight(0.8f),
                textColor = if (viewModel.isFieldModified("estimatedValue", viewModel.estimatedValue)) highlightColor else Color.Unspecified
            )
            CompactTextField(
                value = viewModel.purchaseDate,
                onValueChange = { viewModel.purchaseDate = it },
                label = "Date",
                modifier = Modifier.weight(1f)
            )
        }

        // Description
        CompactTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = "Description",
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
            textColor = if (viewModel.isFieldModified("description", viewModel.description)) highlightColor else Color.Unspecified
        )

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (viewModel.isReviewingEnrichment) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "AI Enrichment Preview",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Review the highlighted changes above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.discardEnrichment() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard Changes")
                        }
                        Button(
                            onClick = { viewModel.acceptEnrichment() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept Changes")
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = { viewModel.updateItem(onSuccess = onItemUpdated) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading,
                contentPadding = PaddingValues(8.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update Item", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun MediaTab(viewModel: EditItemViewModel) {
    if (viewModel.itemMedia.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No photos available for this item.")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.itemMedia) { photo ->
                val imageUrl = if (photo.path.startsWith("http")) photo.path 
                else "https://nesdemo.welshrd.com/${photo.path.removePrefix("/")}"
                
                Box {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.deletePhoto(photo.id) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                    if (photo.is_primary) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text("Primary", modifier = Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenanceTab(viewModel: EditItemViewModel) {
    if (viewModel.maintenanceTasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No maintenance history for this item.")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.maintenanceTasks.forEach { task ->
                MaintenanceTaskRow(task = task, onToggle = { viewModel.toggleMaintenanceTask(task) })
            }
        }
    }
}
