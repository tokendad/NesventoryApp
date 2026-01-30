package com.tokendad.nesventorynew.ui.addlocation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventorynew.ui.components.NesDropdown
import com.tokendad.nesventorynew.ui.components.NesPrimaryButton
import com.tokendad.nesventorynew.ui.components.NesTextField
import com.tokendad.nesventorynew.ui.theme.NesSpacing
import com.tokendad.nesventorynew.util.RoomCategories

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
                .padding(horizontal = NesSpacing.sm, vertical = NesSpacing.xs)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            // Name & Friendly Name
            Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                NesTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = "Name *",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                NesTextField(
                    value = viewModel.friendlyName,
                    onValueChange = { viewModel.friendlyName = it },
                    label = "Friendly Name",
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            // Parent Location Selector
            val selectedParentName = viewModel.availableLocations
                .find { it.id == viewModel.selectedParentId }?.name ?: "None (Root)"
            
            val parentOptions = listOf("None (Root)") + viewModel.availableLocations.map { it.name }

            NesDropdown(
                label = "Parent Location",
                options = parentOptions,
                selectedOption = selectedParentName,
                onOptionSelected = { selectedName ->
                    if (selectedName == "None (Root)") {
                        viewModel.selectedParentId = null
                    } else {
                        viewModel.availableLocations.find { it.name == selectedName }?.let {
                            viewModel.selectedParentId = it.id
                        }
                    }
                }
            )

            // Location Category
            LocationCategorySelector(
                selected = viewModel.locationCategory,
                categories = viewModel.locationCategories,
                onSelect = { viewModel.locationCategory = it }
            )

            // Address
            NesTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = "Address",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2,
                textStyle = MaterialTheme.typography.bodySmall
            )

            // Estimated Value
            NesTextField(
                value = viewModel.estimatedPropertyValue,
                onValueChange = { viewModel.estimatedPropertyValue = it },
                label = "Estimated Property Value",
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall
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
                        onCheckedChange = { viewModel.isPrimaryLocation = it }
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Container?", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = viewModel.isContainer,
                        onCheckedChange = { viewModel.isContainer = it }
                    )
                }
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

            viewModel.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            NesPrimaryButton(
                text = "Create Location",
                onClick = { viewModel.createLocation(onSuccess = onLocationCreated) },
                enabled = !viewModel.isLoading,
                loading = viewModel.isLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCategorySelector(
    selected: String?,
    categories: List<String>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected ?: "Select Category",
            onValueChange = {},
            readOnly = true,
            label = { Text("Location Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            textStyle = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    leadingIcon = {
                        Icon(
                            RoomCategories.getIcon(category),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
