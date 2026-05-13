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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesListItemCard
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSearchField
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.ui.theme.PersonAccent
import com.tokendad.nesventory.ui.theme.PetAccent
import com.tokendad.nesventory.ui.theme.PlantAccent
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
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Inventory", style = MaterialTheme.typography.titleMedium) },
                    actions = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                        }
                    }
                )
                NesSearchField(
                    value = viewModel.searchQuery,
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
                        selected = viewModel.livingTypeFilter == null,
                        onClick = { viewModel.onLivingFilterChange(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = viewModel.livingTypeFilter == LivingItemType.PERSON,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.PERSON) },
                        label = { Text("People") }
                    )
                    FilterChip(
                        selected = viewModel.livingTypeFilter == LivingItemType.PET,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.PET) },
                        label = { Text("Pets") }
                    )
                    FilterChip(
                        selected = viewModel.livingTypeFilter == LivingItemType.PLANT,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.PLANT) },
                        label = { Text("Plants") }
                    )
                    FilterChip(
                        selected = viewModel.livingTypeFilter == LivingItemType.NON_LIVING,
                        onClick = { viewModel.onLivingFilterChange(LivingItemType.NON_LIVING) },
                        label = { Text("Non-living") }
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
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    NesLoadingState(message = "Loading items...")
                }
            }
            viewModel.filteredItems.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    NesEmptyState(
                        title = if (viewModel.searchQuery.isNotEmpty())
                            "No items found"
                        else
                            "No items yet",
                        message = if (viewModel.searchQuery.isNotEmpty())
                            "Try adjusting your search query"
                        else
                            "Add your first item to get started",
                        icon = Icons.Outlined.Inventory2,
                        action = if (viewModel.searchQuery.isEmpty()) {
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
                    items(viewModel.filteredItems) { item ->
                        val locationName = item.location_id?.let { viewModel.locationNames[it] }
                        ItemRow(
                            item = item,
                            locationName = locationName,
                            serverUrl = viewModel.serverUrl,
                            onClick = { onItemClick(item.id) },
                            onEdit = { onEditItemClick(item.id) },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
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
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val livingType = LivingItemType.from(item.is_living, item.relationship_type)
    val isLiving = livingType != LivingItemType.NON_LIVING

    NesListItemCard(
        onClick = onClick
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
