package com.tokendad.nesventorynew.ui.additem

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onBackClick: () -> Unit,
    onItemCreated: () -> Unit,
    viewModel: AddItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.analyzeImage(context.contentResolver, uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.analyzeBitmap(bitmap)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Item", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.showDetectionResults) {
            val item = viewModel.currentDetectedItem
            if (item != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.showDetectionResults = false },
                    title = { Text("AI Result (${viewModel.currentDetectionIndex + 1}/${viewModel.detectedItems.size})") },
                    text = {
                        Column {
                            Text("Name: ${item.name}", style = MaterialTheme.typography.bodyMedium)
                            if (!item.brand.isNullOrBlank()) Text("Brand: ${item.brand}", style = MaterialTheme.typography.bodySmall)
                            if (!item.description.isNullOrBlank()) Text("Description: ${item.description}", style = MaterialTheme.typography.bodySmall)
                            if (item.estimated_value != null) Text("Value: $${item.estimated_value}", style = MaterialTheme.typography.bodySmall)
                            if (item.confidence != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Confidence: ${(item.confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.acceptDetection() }) {
                            Text("Accept")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.rejectDetection() }) {
                            Text(if (viewModel.currentDetectionIndex < viewModel.detectedItems.size - 1) "Next Result" else "Reject All")
                        }
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Auto-fill Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { 
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery", style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            if (viewModel.isLoading) {
                 LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                 Text("Analyzing...", style = MaterialTheme.typography.labelSmall)
            }

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
                    modifier = Modifier.weight(0.8f)
                )
            }

            // Model & Serial
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(
                    value = viewModel.modelNumber,
                    onValueChange = { viewModel.modelNumber = it },
                    label = "Model",
                    modifier = Modifier.weight(1f)
                )
                CompactTextField(
                    value = viewModel.serialNumber,
                    onValueChange = { viewModel.serialNumber = it },
                    label = "Serial",
                    modifier = Modifier.weight(1f)
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
                    modifier = Modifier.weight(0.8f)
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
                minLines = 2
            )

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.createItem(onSuccess = onItemCreated) },
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
                    Text("Create Item", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        modifier = modifier.height(if (minLines > 1) 80.dp else 56.dp),
        textStyle = MaterialTheme.typography.bodySmall,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = if (singleLine) 1 else 3
    )
}
