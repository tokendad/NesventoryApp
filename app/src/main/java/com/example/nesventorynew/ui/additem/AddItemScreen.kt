package com.example.nesventorynew.ui.additem

import android.graphics.Bitmap
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
    
    // Gallery Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.analyzeImage(context.contentResolver, uri)
        }
    }

    // Camera Launcher (Thumbnail)
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
                title = { Text("Add New Item") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Auto-fill Details", style = MaterialTheme.typography.titleMedium)
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Gallery Button
                OutlinedButton(
                    onClick = { 
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
                
                // Camera Button
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Face, contentDescription = null) // Using Face as placeholder for Camera if CameraAlt not found
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            }
            
            if (viewModel.isLoading) {
                 LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                 Text("Analyzing image...", style = MaterialTheme.typography.bodySmall)
            }

            // Name (Required)
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Brand & Retailer
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.brand,
                    onValueChange = { viewModel.brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.retailer,
                    onValueChange = { viewModel.retailer = it },
                    label = { Text("Retailer") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Model & Serial
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.modelNumber,
                    onValueChange = { viewModel.modelNumber = it },
                    label = { Text("Model Num") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.serialNumber,
                    onValueChange = { viewModel.serialNumber = it },
                    label = { Text("Serial Num") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Price & Value
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.purchasePrice,
                    onValueChange = { viewModel.purchasePrice = it },
                    label = { Text("Purch Price") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.estimatedValue,
                    onValueChange = { viewModel.estimatedValue = it },
                    label = { Text("Est Value") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Purchase Date
            OutlinedTextField(
                value = viewModel.purchaseDate,
                onValueChange = { viewModel.purchaseDate = it },
                label = { Text("Purchase Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Location Selector
            var locationExpanded by remember { mutableStateOf(false) }
            val selectedLocationName = viewModel.availableLocations
                .find { it.id == viewModel.selectedLocationId }?.name ?: ""

            Box {
                OutlinedTextField(
                    value = selectedLocationName,
                    onValueChange = {},
                    label = { Text("Location") },
                    placeholder = { Text("Select Location") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { locationExpanded = !locationExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Location")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f) // Adjust width as needed
                ) {
                    viewModel.availableLocations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc.name) },
                            onClick = {
                                viewModel.selectedLocationId = loc.id
                                locationExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Description
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { viewModel.createItem(onSuccess = onItemCreated) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Item")
                }
            }
        }
    }
}
