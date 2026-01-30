package com.tokendad.nesventorynew.ui.additem

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventorynew.ui.components.NesCompactButton
import com.tokendad.nesventorynew.ui.components.NesDropdown
import com.tokendad.nesventorynew.ui.components.NesInlineLoading
import com.tokendad.nesventorynew.ui.components.NesPrimaryButton
import com.tokendad.nesventorynew.ui.components.NesSecondaryButton
import com.tokendad.nesventorynew.ui.components.NesTextField
import com.tokendad.nesventorynew.ui.theme.NesSpacing

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

    val barcodeCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.scanBarcodeFromImage(bitmap)
        }
    }

    if (viewModel.showBarcodeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showBarcodeDialog = false },
            title = { Text("Barcode Lookup") },
            text = {
                Column {
                    NesTextField(
                        value = viewModel.barcodeInput,
                        onValueChange = { viewModel.barcodeInput = it },
                        label = "UPC / EAN",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { barcodeCameraLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan with Camera")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.lookupBarcode() }) {
                    Text("Lookup")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showBarcodeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                .padding(horizontal = NesSpacing.sm, vertical = NesSpacing.xs)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            // Auto-fill Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                NesCompactButton(
                    text = "Gallery",
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Create
                )

                NesCompactButton(
                    text = "Camera",
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Face
                )

                NesCompactButton(
                    text = "Barcode",
                    onClick = { viewModel.showBarcodeDialog = true },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Search
                )
            }

            NesInlineLoading(isLoading = viewModel.isLoading)
            if (viewModel.isLoading) {
                Text("Analyzing...", style = MaterialTheme.typography.labelSmall)
            }

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
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            // Model & Serial
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NesTextField(
                    value = viewModel.modelNumber,
                    onValueChange = { viewModel.modelNumber = it },
                    label = "Model",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                NesTextField(
                    value = viewModel.serialNumber,
                    onValueChange = { viewModel.serialNumber = it },
                    label = "Serial",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
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
                    textStyle = MaterialTheme.typography.bodySmall
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
                textStyle = MaterialTheme.typography.bodySmall
            )

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (viewModel.showRetryOption) {
                NesSecondaryButton(
                    text = "Retry with Standard AI",
                    onClick = { viewModel.retryWithStandardAi() },
                    icon = Icons.Default.Refresh
                )
            }

            NesPrimaryButton(
                text = "Create Item",
                onClick = { viewModel.createItem(onSuccess = onItemCreated) },
                enabled = !viewModel.isLoading,
                loading = viewModel.isLoading
            )
        }
    }
}
