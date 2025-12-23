package com.example.nesventorynew.ui.addlocation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    onBackClick: () -> Unit,
    onLocationCreated: () -> Unit,
    viewModel: AddLocationViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Location", style = MaterialTheme.typography.titleMedium) },
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
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Name & Friendly Name
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = "Name *",
                    modifier = Modifier.weight(1f)
                )
                CompactTextField(
                    value = viewModel.friendlyName,
                    onValueChange = { viewModel.friendlyName = it },
                    label = "Friendly Name",
                    modifier = Modifier.weight(1f)
                )
            }

            // Parent Location Selector
            var parentExpanded by remember { mutableStateOf(false) }
            val selectedParentName = viewModel.availableLocations
                .find { it.id == viewModel.selectedParentId }?.name ?: ""

            Box {
                OutlinedTextField(
                    value = selectedParentName,
                    onValueChange = {},
                    label = { Text("Parent Location", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("Select Parent (Optional)", style = MaterialTheme.typography.bodySmall) },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    trailingIcon = {
                        IconButton(onClick = { parentExpanded = !parentExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
                DropdownMenu(
                    expanded = parentExpanded,
                    onDismissRequest = { parentExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Root)", style = MaterialTheme.typography.bodySmall) },
                        onClick = {
                            viewModel.selectedParentId = null
                            parentExpanded = false
                        }
                    )
                    viewModel.availableLocations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                viewModel.selectedParentId = loc.id
                                parentExpanded = false
                            }
                        )
                    }
                }
            }

            // Address
            CompactTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = "Address",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )

            // Flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Primary?", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = viewModel.isPrimaryLocation,
                        onCheckedChange = { viewModel.isPrimaryLocation = it },
                        modifier = Modifier.scale(0.8f)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Container?", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = viewModel.isContainer,
                        onCheckedChange = { viewModel.isContainer = it },
                        modifier = Modifier.scale(0.8f)
                    )
                }
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
                onClick = { viewModel.createLocation(onSuccess = onLocationCreated) },
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
                    Text("Create Location", style = MaterialTheme.typography.bodyMedium)
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

// Extension to scale Switch
fun Modifier.scale(scale: Float): Modifier = this.then(Modifier.size(width = 50.dp * scale, height = 30.dp * scale)) // Approximate logic, usually easier with Transform or just smaller size
// Actually, standard Switch size is fixed. Modifier.scale works but affects layout size weirdly sometimes.
// Let's rely on standard Switch but maybe smaller padding around text.
