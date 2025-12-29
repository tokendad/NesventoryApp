package com.tokendad.nesventorynew.ui.printer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Printer Settings", style = MaterialTheme.typography.titleMedium) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (viewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Model Selector
            DropdownSelector(
                label = "Printer Model",
                options = viewModel.supportedModels,
                selectedOption = viewModel.config.model,
                onOptionSelected = viewModel::onModelChange
            )

            // Interface Selector
            DropdownSelector(
                label = "Interface Type",
                options = viewModel.supportedInterfaces,
                selectedOption = viewModel.config.interface_type,
                onOptionSelected = viewModel::onInterfaceChange
            )

            // Address Field
            OutlinedTextField(
                value = viewModel.config.address ?: "",
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address (MAC or Port)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Density Slider
            Text("Print Density: ${viewModel.config.density}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = viewModel.config.density.toFloat(),
                onValueChange = { viewModel.onDensityChange(it.roundToInt()) },
                valueRange = 1f..5f,
                steps = 3 // creates 5 positions: 1, 2, 3, 4, 5
            )

            Spacer(modifier = Modifier.weight(1f))

            if (viewModel.errorMessage != null) {
                Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            if (viewModel.successMessage != null) {
                Text(viewModel.successMessage!!, color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = { viewModel.saveConfig() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !viewModel.isLoading
            ) {
                Text("Save Configuration")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}