package com.tokendad.nesventorynew.ui.editlocation

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
import com.tokendad.nesventorynew.ui.addlocation.CompactTextField
import com.tokendad.nesventorynew.ui.addlocation.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationScreen(
    onBackClick: () -> Unit,
    onLocationUpdated: () -> Unit,
    viewModel: EditLocationViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("General", "Media", "Insurance")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Edit Location", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                0 -> GeneralTab(viewModel, onLocationUpdated)
                1 -> MediaTab()
                2 -> InsuranceTab(viewModel)
            }
        }
    }
}

@Composable
fun GeneralTab(viewModel: EditLocationViewModel, onLocationUpdated: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                viewModel.availableLocations
                    .filter { it.id != viewModel.locationId } // Prevent self-parenting
                    .forEach { loc ->
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

        // Estimated Value
        CompactTextField(
            value = viewModel.estimatedPropertyValue,
            onValueChange = { viewModel.estimatedPropertyValue = it },
            label = "Estimated Property Value",
            modifier = Modifier.fillMaxWidth()
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
            onClick = { viewModel.updateLocation(onSuccess = onLocationUpdated) },
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
                Text("Update Location", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MediaTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Media Management", style = MaterialTheme.typography.titleMedium)
            Text("Photos and videos for this location will appear here.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* TODO */ }) {
                Text("Add Media")
            }
        }
    }
}

@Composable
fun InsuranceTab(viewModel: EditLocationViewModel) {
    if (!viewModel.isPrimaryLocation) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Insurance details are only available for primary locations.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(32.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Company Details", style = MaterialTheme.typography.titleMedium)
            CompactTextField(
                value = viewModel.companyName,
                onValueChange = { viewModel.companyName = it },
                label = "Company Name"
            )
            CompactTextField(
                value = viewModel.companyAddress,
                onValueChange = { viewModel.companyAddress = it },
                label = "Company Address"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(
                    value = viewModel.companyEmail,
                    onValueChange = { viewModel.companyEmail = it },
                    label = "Company Email",
                    modifier = Modifier.weight(1f)
                )
                CompactTextField(
                    value = viewModel.companyPhone,
                    onValueChange = { viewModel.companyPhone = it },
                    label = "Company Phone",
                    modifier = Modifier.weight(1f)
                )
            }
            CompactTextField(
                value = viewModel.agentName,
                onValueChange = { viewModel.agentName = it },
                label = "Agent Name"
            )

            HorizontalDivider()
            Text("Policy Details", style = MaterialTheme.typography.titleMedium)
            CompactTextField(
                value = viewModel.policyNumber,
                onValueChange = { viewModel.policyNumber = it },
                label = "Policy Number"
            )

            HorizontalDivider()
            Text("Primary Holder Details", style = MaterialTheme.typography.titleMedium)
            CompactTextField(
                value = viewModel.primaryHolderName,
                onValueChange = { viewModel.primaryHolderName = it },
                label = "Name"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(
                    value = viewModel.primaryHolderEmail,
                    onValueChange = { viewModel.primaryHolderEmail = it },
                    label = "Email",
                    modifier = Modifier.weight(1f)
                )
                CompactTextField(
                    value = viewModel.primaryHolderPhone,
                    onValueChange = { viewModel.primaryHolderPhone = it },
                    label = "Phone",
                    modifier = Modifier.weight(1f)
                )
            }
            CompactTextField(
                value = viewModel.primaryHolderAddress,
                onValueChange = { viewModel.primaryHolderAddress = it },
                label = "Address"
            )

            HorizontalDivider()
            Text("Property Details", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(
                    value = viewModel.insurancePurchaseDate,
                    onValueChange = { viewModel.insurancePurchaseDate = it },
                    label = "Purchase Date",
                    modifier = Modifier.weight(1f)
                )
                CompactTextField(
                    value = viewModel.insurancePurchasePrice,
                    onValueChange = { viewModel.insurancePurchasePrice = it },
                    label = "Purchase Price",
                    modifier = Modifier.weight(1f)
                )
            }
            CompactTextField(
                value = viewModel.insuranceBuildDate,
                onValueChange = { viewModel.insuranceBuildDate = it },
                label = "Build Date"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.updateLocation(onSuccess = {}) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading
            ) {
                Text("Save Insurance Info")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
