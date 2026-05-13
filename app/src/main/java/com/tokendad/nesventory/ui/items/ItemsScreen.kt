package com.tokendad.nesventory.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesListItemCard
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesOfflineBanner
import com.tokendad.nesventory.ui.components.NesDropdown
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSearchField
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.ui.theme.PersonAccent
import com.tokendad.nesventory.ui.theme.PetAccent
import com.tokendad.nesventory.ui.theme.PlantAccent
import com.tokendad.nesventory.util.ColorUtils
import com.tokendad.nesventory.util.PhotoUrlValidator
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    onItemClick: (UUID) -> Unit = {},
    onAddItemClick: () -> Unit = {},
    onEditItemClick: (UUID) -> Unit = {},
    onExit: () -> Unit = {},
    viewModel: ItemsViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val livingTypeFilter by viewModel.livingTypeFilter.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val locationNames by viewModel.locationNames.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val selectedTagId by viewModel.selectedTagId.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedItemIds.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val pagedItems = viewModel.pagedItems.collectAsLazyPagingItems()
    val usePagedList = selectedTagId == null && !isSelectionMode

    var showMoveDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var selectedMoveLocationId by remember(locations) {
        mutableStateOf<UUID?>(locations.firstOrNull()?.id)
    }
    var selectedBulkTagId by remember(availableTags) {
        mutableStateOf<UUID?>(availableTags.firstOrNull()?.id)
    }
    var isAddTagAction by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    if (showMoveDialog) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Move selected items") },
            text = {
                if (locations.isEmpty()) {
                    Text("No locations available.")
                } else {
                    NesDropdown(
                        label = "Location",
                        options = locations.map { it.name },
                        selectedOption = locations.firstOrNull { it.id == selectedMoveLocationId }?.name ?: "",
                        onOptionSelected = { selected ->
                            selectedMoveLocationId = locations.firstOrNull { it.name == selected }?.id
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.bulkUpdateLocation(selectedMoveLocationId)
                        showMoveDialog = false
                    },
                    enabled = locations.isNotEmpty()
                ) {
                    Text("Move")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Update tags for selected items") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                    NesDropdown(
                        label = "Action",
                        options = listOf("Add", "Remove"),
                        selectedOption = if (isAddTagAction) "Add" else "Remove",
                        onOptionSelected = { selected -> isAddTagAction = selected == "Add" }
                    )
                    if (availableTags.isEmpty()) {
                        Text("No tags available.")
                    } else {
                        NesDropdown(
                            label = "Tag",
                            options = availableTags.map { it.name },
                            selectedOption = availableTags.firstOrNull { it.id == selectedBulkTagId }?.name ?: "",
                            onOptionSelected = { selected ->
                                selectedBulkTagId = availableTags.firstOrNull { it.name == selected }?.id
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedBulkTagId?.let { viewModel.bulkUpdateTag(it, isAddTagAction) }
                        showTagDialog = false
                    },
                    enabled = selectedBulkTagId != null
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
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
                    title = {
                        Text(
                            if (isSelectionMode) "${selectedItemIds.size} selected" else "My Inventory",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        if (isSelectionMode) {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Default.Close, contentDescription = "Exit selection mode")
                            }
                        }
                    },
                    actions = {
                        if (isSelectionMode) {
                            IconButton(onClick = { viewModel.selectAll() }) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Select all")
                            }
                            IconButton(onClick = { viewModel.bulkDeleteSelected() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                            }
                            IconButton(onClick = { showMoveDialog = true }) {
                                Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Move selected")
                            }
                            IconButton(onClick = { showTagDialog = true }) {
                                Icon(Icons.Default.LocalOffer, contentDescription = "Tag selected")
                            }
                        } else {
                            IconButton(onClick = onExit) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                            }
                        }
                    }
                )
                NesSearchField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = "Search items...",
                    modifier = Modifier.padding(horizontal = NesSpacing.sm, vertical = NesSpacing.xs)
                )
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = NesSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                ) {
                    FilterChip(
                        selected = livingTypeFilter == null,
                        onClick = { viewModel.onLivingFilterChange(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = livingTypeFilter == LivingItemType.PERSON,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.PERSON) },
                        label = { Text("People") }
                    )
                    FilterChip(
                        selected = livingTypeFilter == LivingItemType.PET,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.PET) },
                        label = { Text("Pets") }
                    )
                    FilterChip(
                        selected = livingTypeFilter == LivingItemType.PLANT,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.PLANT) },
                        label = { Text("Plants") }
                    )
                    FilterChip(
                        selected = livingTypeFilter == LivingItemType.NON_LIVING,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.NON_LIVING) },
                        label = { Text("Non-living") }
                    )
                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = NesSpacing.sm, vertical = NesSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                ) {
                    FilterChip(
                        selected = selectedTagId == null,
                        onClick = { viewModel.onTagFilterChange(null) },
                        label = { Text("All tags") }
                    )
                    availableTags.forEach { tag ->
                        val tagColor = ColorUtils.parseHexColor(tag.color)
                        FilterChip(
                            selected = selectedTagId == tag.id,
                            onClick = { viewModel.onTagFilterChange(tag.id) },
                            label = { Text(tag.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tagColor?.copy(alpha = 0.25f)
                                    ?: MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
                if (isOffline) {
                    NesOfflineBanner(
                        modifier = Modifier.padding(horizontal = NesSpacing.sm, vertical = NesSpacing.xs)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItemClick,
                modifier = Modifier.size(NesSize.minTouchTarget)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    NesLoadingState(message = "Loading items...")
                }
            }
            (!usePagedList && filteredItems.isEmpty()) || (usePagedList && pagedItems.itemCount == 0) -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    NesEmptyState(
                        title = if (searchQuery.isNotEmpty())
                            "No items found"
                        else
                            "No items yet",
                        message = if (searchQuery.isNotEmpty())
                            "Try adjusting your search query"
                        else
                            "Add your first item to get started",
                        icon = Icons.Outlined.Inventory2,
                        action = if (searchQuery.isEmpty()) {
                            {
                                NesPrimaryButton(
                                    text = "Add Item",
                                    onClick = onAddItemClick,
                                    fullWidth = false
                                )
                            }
                        } else null
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(NesSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                ) {
                    if (usePagedList) {
                        items(pagedItems.itemCount) { index ->
                            val item = pagedItems[index] ?: return@items
                            val locationName = item.location_id?.let { locationNames[it] }
                            ItemRow(
                                item = item,
                                locationName = locationName,
                                serverUrl = serverUrl,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedItemIds.contains(item.id),
                                onClick = { onItemClick(item.id) },
                                onLongPress = { viewModel.enterSelectionMode(item.id) },
                                onToggleSelection = { viewModel.toggleSelection(item.id) },
                                onEdit = { onEditItemClick(item.id) },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                        }
                    } else {
                        items(filteredItems) { item ->
                            val locationName = item.location_id?.let { locationNames[it] }
                            ItemRow(
                                item = item,
                                locationName = locationName,
                                serverUrl = serverUrl,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedItemIds.contains(item.id),
                                onClick = { onItemClick(item.id) },
                                onLongPress = { viewModel.enterSelectionMode(item.id) },
                                onToggleSelection = { viewModel.toggleSelection(item.id) },
                                onEdit = { onEditItemClick(item.id) },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemRow(
    item: Item,
    locationName: String?,
    serverUrl: String,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val livingType = LivingItemType.from(item.is_living, item.relationship_type)
    val isLiving = livingType != LivingItemType.NON_LIVING

    NesListItemCard(
        onClick = {
            if (isSelectionMode) onToggleSelection() else onClick()
        },
        onLongClick = onLongPress,
        isSelected = isSelected
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Primary Photo
            val primaryPhoto = item.photos.find { it.is_primary }
            val imageUrl = primaryPhoto?.let { photo ->
                PhotoUrlValidator.buildPhotoUrl(photo.path, serverUrl)
            }

            Card(
                modifier = Modifier.size(NesSize.thumbnailSmall),
                shape = if (isLiving) CircleShape else MaterialTheme.shapes.small
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (livingType) {
                                LivingItemType.PERSON -> Icons.Default.Person
                                LivingItemType.PET -> Icons.Default.Pets
                                LivingItemType.PLANT -> Icons.Default.Eco
                                LivingItemType.NON_LIVING -> Icons.Outlined.Inventory2
                            },
                            contentDescription = null,
                            tint = when (livingType) {
                                LivingItemType.PERSON -> PersonAccent
                                LivingItemType.PET -> PetAccent
                                LivingItemType.PLANT -> PlantAccent
                                LivingItemType.NON_LIVING -> MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.size(NesSize.iconSmall)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(NesSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall
                )
                if (isLiving) {
                    val label = when (livingType) {
                        LivingItemType.PERSON -> "Person"
                        LivingItemType.PET -> "Pet"
                        LivingItemType.PLANT -> "Plant"
                        LivingItemType.NON_LIVING -> "Item"
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (livingType) {
                            LivingItemType.PERSON -> PersonAccent
                            LivingItemType.PET -> PetAccent
                            LivingItemType.PLANT -> PlantAccent
                            LivingItemType.NON_LIVING -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Text(
                    text = locationName ?: "No Location",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (locationName != null)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Ellipsis Menu
            if (!isSelectionMode) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(NesSize.iconDefault)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Details") },
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Item") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Item", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
