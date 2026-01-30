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
import com.tokendad.nesventorynew.ui.components.NesCard
import com.tokendad.nesventorynew.ui.components.NesDropdown
import com.tokendad.nesventorynew.ui.components.NesEmptyState
import com.tokendad.nesventorynew.ui.components.NesPrimaryButton
import com.tokendad.nesventorynew.ui.components.NesSecondaryButton
import com.tokendad.nesventorynew.ui.components.NesTextField
import com.tokendad.nesventorynew.ui.maintenance.MaintenanceTaskRow
import com.tokendad.nesventorynew.ui.theme.NesSpacing

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
                1 -> MediaTab(viewModel, viewModel.serverUrl)
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
            .padding(horizontal = NesSpacing.lg, vertical = NesSpacing.sm)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
    ) {
        // Name & Brand
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NesTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = "Name *",
                modifier = Modifier.weight(1.2f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            NesTextField(
                value = viewModel.brand,
                onValueChange = { viewModel.brand = it },
                label = "Brand",
                modifier = Modifier.weight(0.8f),
                textStyle = if (viewModel.isFieldModified("brand", viewModel.brand)) 
                    MaterialTheme.typography.bodySmall.copy(color = highlightColor) 
                else MaterialTheme.typography.bodySmall
            )
        }

        // Model & Serial
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NesTextField(
                value = viewModel.modelNumber,
                onValueChange = { viewModel.modelNumber = it },
                label = "Model",
                modifier = Modifier.weight(1f),
                textStyle = if (viewModel.isFieldModified("modelNumber", viewModel.modelNumber)) 
                    MaterialTheme.typography.bodySmall.copy(color = highlightColor) 
                else MaterialTheme.typography.bodySmall
            )
            NesTextField(
                value = viewModel.serialNumber,
                onValueChange = { viewModel.serialNumber = it },
                label = "Serial",
                modifier = Modifier.weight(1f),
                textStyle = if (viewModel.isFieldModified("serialNumber", viewModel.serialNumber)) 
                    MaterialTheme.typography.bodySmall.copy(color = highlightColor) 
                else MaterialTheme.typography.bodySmall
            )
        }

        // Retailer & Location
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NesTextField(
                value = viewModel.retailer,
                onValueChange = { viewModel.retailer = it },
                label = "Retailer",
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            
            // Location Selector
            val selectedLocationName = viewModel.availableLocations
                .find { it.id == viewModel.selectedLocationId }?.name ?: ""

            NesDropdown(
                label = "Location",
                options = viewModel.availableLocations.map { it.name },
                selectedOption = selectedLocationName,
                onOptionSelected = { name ->
                    viewModel.availableLocations.find { it.name == name }?.let {
                        viewModel.selectedLocationId = it.id
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Price, Value, Date
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NesTextField(
                value = viewModel.purchasePrice,
                onValueChange = { viewModel.purchasePrice = it },
                label = "Price",
                modifier = Modifier.weight(0.8f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            NesTextField(
                value = viewModel.estimatedValue,
                onValueChange = { viewModel.estimatedValue = it },
                label = "Value",
                modifier = Modifier.weight(0.8f),
                textStyle = if (viewModel.isFieldModified("estimatedValue", viewModel.estimatedValue)) 
                    MaterialTheme.typography.bodySmall.copy(color = highlightColor) 
                else MaterialTheme.typography.bodySmall
            )
            NesTextField(
                value = viewModel.purchaseDate,
                onValueChange = { viewModel.purchaseDate = it },
                label = "Date",
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        // Description
        NesTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = "Description",
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
            textStyle = if (viewModel.isFieldModified("description", viewModel.description)) 
                MaterialTheme.typography.bodySmall.copy(color = highlightColor) 
            else MaterialTheme.typography.bodySmall
        )

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (viewModel.isReviewingEnrichment) {
            NesCard(
                title = "AI Enrichment Preview",
                subtitle = "Review the highlighted changes above."
            ) {
                Spacer(modifier = Modifier.height(NesSpacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                    NesSecondaryButton(
                        text = "Discard Changes",
                        onClick = { viewModel.discardEnrichment() },
                        modifier = Modifier.weight(1f)
                    )
                    NesPrimaryButton(
                        text = "Accept Changes",
                        onClick = { viewModel.acceptEnrichment() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            NesPrimaryButton(
                text = "Update Item",
                onClick = { viewModel.updateItem(onSuccess = onItemUpdated) },
                enabled = !viewModel.isLoading,
                loading = viewModel.isLoading
            )
        }
    }
}

@Composable
fun MediaTab(viewModel: EditItemViewModel, serverUrl: String) {
    if (viewModel.itemMedia.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NesEmptyState(
                title = "No photos",
                message = "No photos available for this item.",
                icon = Icons.Default.PhotoLibrary
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(NesSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            items(viewModel.itemMedia) { photo ->
                val imageUrl = if (photo.path.startsWith("http")) photo.path
                else "$serverUrl/${photo.path.removePrefix("/")}"
                
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
            NesEmptyState(
                title = "No maintenance history",
                message = "No maintenance history for this item.",
                icon = Icons.Default.Build
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(NesSpacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            viewModel.maintenanceTasks.forEach { task ->
                MaintenanceTaskRow(task = task, onToggle = { viewModel.toggleMaintenanceTask(task) })
            }
        }
    }
}
