package com.tokendad.nesventory.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import coil3.compose.AsyncImage
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesSearchField
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.util.PhotoUrlValidator
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onItemClick: (UUID) -> Unit,
    onEditItemClick: (UUID) -> Unit,
    onProfileClick: () -> Unit,
    onServerSettingsClick: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("NesVentory", style = MaterialTheme.typography.titleMedium) },
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                        IconButton(onClick = onServerSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Server Settings")
                        }
                        IconButton(onClick = onExit) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                        }
                    }
                )
                // Search Bar
                NesSearchField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = "Search items...",
                    modifier = Modifier.padding(
                        horizontal = NesSpacing.sm,
                        vertical = NesSpacing.xs
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            Text(
                text = "Newest Items",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = NesSpacing.sm)
            )

            when {
                viewModel.isItemsLoading -> {
                    NesLoadingState(message = "Loading items...")
                }
                else -> {
                    val displayItems = if (viewModel.searchQuery.isBlank()) {
                        viewModel.recentItems
                    } else {
                        viewModel.recentItems.filter {
                            it.name.contains(viewModel.searchQuery, ignoreCase = true)
                        }
                    }

                    if (displayItems.isEmpty()) {
                        NesEmptyState(
                            title = "No items found",
                            message = if (viewModel.searchQuery.isNotBlank()) {
                                "Try a different search term"
                            } else {
                                "Add your first item to get started"
                            },
                            icon = Icons.Outlined.Inventory2
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
                        ) {
                            items(displayItems) { item ->
                                DashboardItemRow(
                                    item = item,
                                    serverUrl = viewModel.remoteUrl.trimEnd('/'),
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
    }
}

@Composable
fun DashboardItemRow(
    item: Item,
    serverUrl: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(NesSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Primary Photo Thumbnail
            val primaryPhoto = item.photos.orEmpty().find { it.is_primary }
            val imageUrl = primaryPhoto?.let { photo ->
                PhotoUrlValidator.buildPhotoUrl(photo.path, serverUrl)
            }

            Card(
                modifier = Modifier.size(NesSize.thumbnailSmall),
                shape = MaterialTheme.shapes.small
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
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(NesSize.iconSmall)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(NesSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                item.estimated_value?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Overflow Menu
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
                        text = {
                            Text(
                                "Delete Item",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
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
