package com.tokendad.nesventorynew.ui.locationdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventorynew.data.remote.InsuranceInfo
import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.PolicyHolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    onBackClick: () -> Unit,
    viewModel: LocationDetailViewModel = hiltViewModel()
) {
    val location = viewModel.location
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Location") },
            text = { Text("Are you sure you want to delete this location? This will likely cascade delete related items/sub-locations.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLocation(onSuccess = onBackClick)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = location?.name ?: "Location Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (location != null) {
                            IconButton(onClick = { viewModel.printLabel() }) {
                                Icon(Icons.Default.Print, contentDescription = "Print Label")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Location")
                            }
                        }
                    }
                )
                // Show tabs only for primary locations
                if (location?.is_primary_location == true) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Details") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Insurance") }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (location != null) {
                if (location.is_primary_location) {
                    when (selectedTab) {
                        0 -> DetailsTab(location)
                        1 -> InsuranceTab(location.insurance_info)
                    }
                } else {
                    DetailsTab(location)
                }
            }
        }
    }
}

@Composable
fun DetailsTab(location: Location) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primary Photo
        val primaryPhoto = location.location_photos.find { it.is_primary }
        primaryPhoto?.let { photo ->
            val imageUrl = if (photo.path.startsWith("http")) {
                photo.path
            } else {
                 "https://nesdemo.welshrd.com/${photo.path.removePrefix("/")}"
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = "Primary Photo for ${location.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Name
        Text(text = location.name, style = MaterialTheme.typography.headlineMedium)

        // Friendly Name
        location.friendly_name?.let {
            Text(text = "($it)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (location.is_primary_location) {
                AssistChip(onClick = {}, label = { Text("Primary Location") })
            }
            if (location.is_container) {
                AssistChip(onClick = {}, label = { Text("Container") })
            }
        }

        HorizontalDivider()

        // Description
        if (!location.description.isNullOrBlank()) {
            Text(text = "Description", style = MaterialTheme.typography.titleMedium)
            Text(text = location.description, style = MaterialTheme.typography.bodyLarge)
        }

        // Address
        if (!location.address.isNullOrBlank()) {
            Text(text = "Address", style = MaterialTheme.typography.titleMedium)
            Text(text = location.address, style = MaterialTheme.typography.bodyLarge)
        }

        // Values
        if (location.estimated_property_value != null || location.estimated_value_with_items != null) {
            HorizontalDivider()

            location.estimated_property_value?.let {
                Text(text = "Property Value", style = MaterialTheme.typography.titleMedium)
                Text(text = "$$it", style = MaterialTheme.typography.bodyLarge)
            }

            location.estimated_value_with_items?.let {
                Text(text = "Value with Items", style = MaterialTheme.typography.titleMedium)
                Text(text = "$$it", style = MaterialTheme.typography.bodyLarge)
            }
        }

        HorizontalDivider()

        // Timestamps
        Column {
            Text(text = "Created: ${location.created_at}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Updated: ${location.updated_at}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun InsuranceTab(insuranceInfo: InsuranceInfo?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (insuranceInfo == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No insurance information configured",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            // Insurance Company Section
            InsuranceSection(
                title = "Insurance Company",
                icon = Icons.Default.Business
            ) {
                InsuranceField("Company Name", insuranceInfo.company_name)
                InsuranceField("Address", insuranceInfo.company_address)
                InsuranceField("Phone", insuranceInfo.company_phone)
                InsuranceField("Email", insuranceInfo.company_email)
                InsuranceField("Agent Name", insuranceInfo.agent_name)
            }

            // Policy Section
            InsuranceSection(
                title = "Policy Information",
                icon = Icons.Default.Description
            ) {
                InsuranceField("Policy Number", insuranceInfo.policy_number)
            }

            // Primary Holder Section
            insuranceInfo.primary_holder?.let { holder ->
                InsuranceSection(
                    title = "Primary Policy Holder",
                    icon = Icons.Default.Person
                ) {
                    PolicyHolderContent(holder)
                }
            }

            // Additional Holders
            if (insuranceInfo.additional_holders.isNotEmpty()) {
                InsuranceSection(
                    title = "Additional Policy Holders",
                    icon = Icons.Default.People
                ) {
                    insuranceInfo.additional_holders.forEachIndexed { index, holder ->
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        PolicyHolderContent(holder)
                    }
                }
            }

            // Property Details Section
            if (insuranceInfo.purchase_date != null || insuranceInfo.purchase_price != null || insuranceInfo.build_date != null) {
                InsuranceSection(
                    title = "Property Details",
                    icon = Icons.Default.Home
                ) {
                    InsuranceField("Purchase Date", insuranceInfo.purchase_date)
                    insuranceInfo.purchase_price?.let {
                        InsuranceField("Purchase Price", "$${String.format("%,.2f", it)}")
                    }
                    InsuranceField("Build Date", insuranceInfo.build_date)
                }
            }
        }
    }
}

@Composable
private fun InsuranceSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun InsuranceField(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PolicyHolderContent(holder: PolicyHolder) {
    InsuranceField("Name", holder.name)
    InsuranceField("Phone", holder.phone)
    InsuranceField("Email", holder.email)
    InsuranceField("Address", holder.address)
}
