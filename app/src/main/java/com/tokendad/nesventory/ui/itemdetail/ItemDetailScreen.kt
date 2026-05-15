package com.tokendad.nesventory.ui.itemdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
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
import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.Photo
import com.tokendad.nesventory.ui.components.NesCard
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesErrorState
import com.tokendad.nesventory.ui.components.NesListItemCard
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesSectionCard
import com.tokendad.nesventory.ui.components.NesTagChip
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.util.CurrencyFormatter
import com.tokendad.nesventory.util.DateFormatter
import com.tokendad.nesventory.util.ColorUtils
import com.tokendad.nesventory.util.PhotoUrlValidator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val item = viewModel.item
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

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
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(onSuccess = onBackClick)
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
                    title = { Text(text = item?.name ?: "Item Details", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (item != null) {
                            IconButton(onClick = { viewModel.printLabel() }) {
                                Icon(Icons.Default.Print, contentDescription = "Print Label")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Item")
                            }
                        }
                    }
                )
                // Tabs
                if (item != null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Details") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Media") }
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
                isLoading && item == null -> {
                    NesLoadingState(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Loading item details..."
                    )
                }
                errorMessage != null && item == null -> {
                    NesErrorState(
                        title = "Error loading item",
                        message = errorMessage,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                item != null -> {
                    when (selectedTab) {
                        0 -> DetailsTab(item, viewModel.serverUrl, viewModel.itemCollections)
                        1 -> MediaTab(item.photos, viewModel.serverUrl)
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun DetailsTab(item: Item, serverUrl: String, itemCollections: List<Collection>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = NesSpacing.lg, vertical = NesSpacing.md)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(NesSpacing.lg)
    ) {
        // Primary Photo
        val primaryPhoto = item.photos.orEmpty().find { it.is_primary }
        if (primaryPhoto != null) {
            val imageUrl = PhotoUrlValidator.buildPhotoUrl(primaryPhoto.path, serverUrl)

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Primary Photo for ${item.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Basic Info
        NesCard {
            Text(text = item.name, style = MaterialTheme.typography.headlineSmall)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
            ) {
                if (item.is_living) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                when (item.relationship_type?.lowercase()) {
                                    "pet" -> "Living: Pet"
                                    "plant" -> "Living: Plant"
                                    else -> "Living: Person"
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, Modifier.size(16.dp)) }
                    )
                }
                item.brand?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text("Brand: $it") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, Modifier.size(16.dp)) }
                    )
                }
                item.model_number?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text("Model: $it") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, Modifier.size(16.dp)) }
                    )
                }
            }
        }

        if (item.is_living) {
            NesSectionCard(title = "Living Details", icon = Icons.Default.Person) {
                item.relationship_type?.takeIf { it.isNotBlank() }?.let {
                    DetailRow("Type", it.replaceFirstChar { c -> c.uppercase() })
                }
                item.birthdate?.takeIf { it.isNotBlank() }?.let {
                    DetailRow("Birthdate", DateFormatter.formatDate(it))
                }
                item.contact_info?.phone?.takeIf { it.isNotBlank() }?.let {
                    DetailRow("Phone", it)
                }
                item.contact_info?.email?.takeIf { it.isNotBlank() }?.let {
                    DetailRow("Email", it)
                }
                item.contact_info?.notes?.takeIf { it.isNotBlank() }?.let {
                    DetailRow("Notes", it)
                }
            }
        }

        // Description & Serial
        if (!item.description.isNullOrBlank() || !item.serial_number.isNullOrBlank() || !item.upc.isNullOrBlank()) {
            NesSectionCard(title = "About", icon = Icons.Default.Description) {
                if (!item.description.isNullOrBlank()) {
                    Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                }
                if (!item.serial_number.isNullOrBlank()) {
                    if (!item.description.isNullOrBlank()) Spacer(modifier = Modifier.height(NesSpacing.sm))
                    Row {
                        Text("Serial Number: ", style = MaterialTheme.typography.labelMedium)
                        Text(item.serial_number, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item.upc?.takeIf { it.isNotBlank() }?.let {
                    if (!item.description.isNullOrBlank() || !item.serial_number.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(NesSpacing.sm))
                    }
                    Row {
                        Text("UPC: ", style = MaterialTheme.typography.labelMedium)
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Purchase & Value Info
        if (item.purchase_price != null || item.purchase_date != null || item.retailer != null || item.estimated_value != null) {
            NesSectionCard(title = "Value & Purchase", icon = Icons.Default.AttachMoney) {
                item.estimated_value?.let {
                    DetailRow("Estimated Value", CurrencyFormatter.format(it), true)
                }
                item.purchase_price?.let {
                    DetailRow("Purchase Price", CurrencyFormatter.format(it))
                }
                item.purchase_date?.let {
                    DetailRow("Purchase Date", DateFormatter.formatDate(it))
                }
                item.retailer?.let {
                    DetailRow("Retailer", it)
                }
            }
        }

        if (item.warranties.orEmpty().isNotEmpty()) {
            NesSectionCard(title = "Warranties", icon = Icons.Default.VerifiedUser) {
                Column(verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                    item.warranties.orEmpty().forEach { warranty ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(NesSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                            ) {
                                Text(
                                    text = formatWarrantyType(warranty.type),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                warranty.provider?.takeIf { it.isNotBlank() }?.let {
                                    Text("Provider: $it", style = MaterialTheme.typography.bodyMedium)
                                }
                                warranty.expiration_date?.takeIf { it.isNotBlank() }?.let {
                                    Text("Expires: ${DateFormatter.formatDate(it)}", style = MaterialTheme.typography.bodyMedium)
                                }
                                warranty.notes?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (item.tags.orEmpty().isNotEmpty()) {
            NesSectionCard(title = "Tags", icon = Icons.Default.LocalOffer) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                ) {
                    item.tags.orEmpty().forEach { tag ->
                        NesTagChip(tag = tag)
                    }
                }
            }
        }

        if (itemCollections.isNotEmpty()) {
            NesSectionCard(title = "Collections", icon = Icons.Default.Collections) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                ) {
                    itemCollections.forEach { collection ->
                        val baseColor = ColorUtils.parseHexColor(collection.color)
                        AssistChip(
                            onClick = {},
                            label = { Text(collection.name) },
                            leadingIcon = {
                                if (!collection.icon.isNullOrBlank()) {
                                    Text(collection.icon, style = MaterialTheme.typography.labelMedium)
                                } else {
                                    Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = baseColor?.copy(alpha = 0.18f)
                                    ?: MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Custom Fields
        item.custom_fields?.let { fields ->
            if (fields.isNotEmpty()) {
                CustomFieldsSection(fields)
            }
        }

        // Timestamps
        Column(modifier = Modifier.padding(horizontal = NesSpacing.sm)) {
            Text(text = "Created: ${DateFormatter.formatDateTime(item.created_at)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Updated: ${DateFormatter.formatDateTime(item.updated_at)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

private fun formatWarrantyType(type: String): String =
    type.replace('_', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

@Composable
fun MediaTab(photos: List<Photo>, serverUrl: String) {
    if (photos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NesEmptyState(
                title = "No photos",
                message = "No photos available for this item",
                icon = Icons.Default.PhotoLibrary
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(NesSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            items(photos) { photo ->
                val imageUrl = PhotoUrlValidator.buildPhotoUrl(photo.path, serverUrl)

                ElevatedCard {
                    Box {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = photo.filename,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Primary badge
                        if (photo.is_primary) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Primary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomFieldsSection(customFields: Map<String, Any>?) {
    if (customFields.isNullOrEmpty()) return

    NesSectionCard(title = "Custom Fields", icon = Icons.Default.Extension) {
        customFields.forEach { (key, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = key.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
