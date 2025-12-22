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
                title = { Text("Add New Location") },
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
            // Name (Required)
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Friendly Name
            OutlinedTextField(
                value = viewModel.friendlyName,
                onValueChange = { viewModel.friendlyName = it },
                label = { Text("Friendly Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Parent Location Selector
            var parentExpanded by remember { mutableStateOf(false) }
            val selectedParentName = viewModel.availableLocations
                .find { it.id == viewModel.selectedParentId }?.name ?: ""

            Box {
                OutlinedTextField(
                    value = selectedParentName,
                    onValueChange = {},
                    label = { Text("Parent Location") },
                    placeholder = { Text("Select Parent (Optional)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { parentExpanded = !parentExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Parent")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = parentExpanded,
                    onDismissRequest = { parentExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Root)") },
                        onClick = {
                            viewModel.selectedParentId = null
                            parentExpanded = false
                        }
                    )
                    viewModel.availableLocations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc.name) },
                            onClick = {
                                viewModel.selectedParentId = loc.id
                                parentExpanded = false
                            }
                        )
                    }
                }
            }

            // Address
            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Is Primary Location?")
                Switch(
                    checked = viewModel.isPrimaryLocation,
                    onCheckedChange = { viewModel.isPrimaryLocation = it }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Is Container?")
                Switch(
                    checked = viewModel.isContainer,
                    onCheckedChange = { viewModel.isContainer = it }
                )
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
                onClick = { viewModel.createLocation(onSuccess = onLocationCreated) },
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
                    Text("Create Location")
                }
            }
        }
    }
}
