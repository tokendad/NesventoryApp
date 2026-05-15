package com.tokendad.nesventory.ui.locationdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import coil3.compose.AsyncImage
import com.tokendad.nesventory.data.remote.InsuranceInfo
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.PolicyHolder
import com.tokendad.nesventory.ui.components.NesCard
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesErrorState
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesSectionCard
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.util.RoomCategories
import com.tokendad.nesventory.util.CurrencyFormatter
import com.tokendad.nesventory.util.ColorUtils
import com.tokendad.nesventory.util.DateFormatter
import com.tokendad.nesventory.util.PhotoUrlValidator

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
                    title = { Text(text = location?.name ?: "Location Details", style = MaterialTheme.typography.titleMedium) },
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
            when {
                isLoading -> {
                    NesLoadingState(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Loading location details..."
                    )
                }
                errorMessage != null -> {
                    NesErrorState(
                        title = "Error loading location",
                        message = errorMessage,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                location != null -> {
                    if (location.is_primary_location) {
                        when (selectedTab) {
                            0 -> DetailsTab(location, viewModel.serverUrl)
                            1 -> InsuranceTab(location.insurance_info)
                        }
                    } else {
                        DetailsTab(location, viewModel.serverUrl)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsTab(location: Location, serverUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NesSpacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(NesSpacing.lg)
    ) {
        // Primary Photo
        val primaryPhoto = location.location_photos.orEmpty().find { it.is_primary }
        if (primaryPhoto != null) {
            val imageUrl = PhotoUrlValidator.buildPhotoUrl(primaryPhoto.path, serverUrl)

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Primary Photo for ${location.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (location.location_photos.orEmpty().size > 1) {
            NesSectionCard(title = "Photos", icon = Icons.Default.PhotoLibrary) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                    items(location.location_photos.orEmpty()) { photo ->
                        val imageUrl = PhotoUrlValidator.buildPhotoUrl(photo.path, serverUrl)
                        ElevatedCard(modifier = Modifier.size(width = 120.dp, height = 90.dp)) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = photo.filename,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // Basic Info
        NesCard {
            Text(text = location.name, style = MaterialTheme.typography.headlineSmall)
            
            location.friendly_name?.let {
                Text(text = it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                location.location_category?.let { category ->
                    AssistChip(
                        onClick = {},
                        label = { Text(category) },
                        leadingIcon = {
                            Icon(
                                RoomCategories.getIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                if (location.is_primary_location) {
                    AssistChip(onClick = {}, label = { Text("Primary") })
                }
                if (location.is_container) {
                    AssistChip(onClick = {}, label = { Text("Container") })
                }
            }
        }

        // About
        if (!location.description.isNullOrBlank() || !location.address.isNullOrBlank()) {
            NesSectionCard(title = "About", icon = Icons.Default.Info) {
                if (!location.description.isNullOrBlank()) {
                    Text(text = "Description", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = location.description, style = MaterialTheme.typography.bodyMedium)
                }
                if (!location.address.isNullOrBlank()) {
                    if (!location.description.isNullOrBlank()) Spacer(modifier = Modifier.height(NesSpacing.md))
                    Text(text = "Address", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = location.address, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Values
        if (location.estimated_property_value != null || location.estimated_value_with_items != null) {
            NesSectionCard(title = "Value", icon = Icons.Default.AttachMoney) {
                location.estimated_property_value?.let {
                    DetailRow("Property Value", CurrencyFormatter.format(it))
                }
                location.estimated_value_with_items?.let {
                    DetailRow("Value with Items", CurrencyFormatter.format(it), true)
                }
            }

            if (location.paint_info.orEmpty().isNotEmpty()) {
                NesSectionCard(title = "Paint Info", icon = Icons.Default.Palette) {
                    Column(verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                        location.paint_info.orEmpty().forEach { paint ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(NesSpacing.md),
                                    verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ColorUtils.parseHexColor(paint.hex_color)?.let { swatch ->
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(swatch, shape = MaterialTheme.shapes.small)
                                            )
                                        }
                                        Text(
                                            text = paint.color_name ?: "Unlabeled color",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                    paint.vendor?.takeIf { it.isNotBlank() }?.let {
                                        Text("Vendor: $it", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    paint.color_code?.takeIf { it.isNotBlank() }?.let {
                                        Text("Code: $it", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    paint.finish?.takeIf { it.isNotBlank() }?.let {
                                        Text("Finish: $it", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Timestamps
        Column(modifier = Modifier.padding(horizontal = NesSpacing.sm)) {
            Text(text = "Created: ${DateFormatter.formatDateTime(location.created_at)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Updated: ${DateFormatter.formatDateTime(location.updated_at)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
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
            style = if (isHighlight) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.typography.titleSmall.color else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InsuranceTab(insuranceInfo: InsuranceInfo?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NesSpacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(NesSpacing.lg)
    ) {
        if (insuranceInfo == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NesEmptyState(
                    title = "No insurance information",
                    message = "No insurance information configured",
                    icon = Icons.Default.Shield
                )
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
            if (insuranceInfo.additional_holders.orEmpty().isNotEmpty()) {
                InsuranceSection(
                    title = "Additional Policy Holders",
                    icon = Icons.Default.People
                ) {
                    insuranceInfo.additional_holders.orEmpty().forEachIndexed { index, holder ->
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
    NesSectionCard(
        title = title,
        icon = icon,
        content = content
    )
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
