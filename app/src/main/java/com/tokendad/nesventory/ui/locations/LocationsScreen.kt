package com.tokendad.nesventory.ui.locations

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesListItemCard
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSearchField
import com.tokendad.nesventory.ui.theme.NesSize
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.util.PhotoUrlValidator
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    onLocationClick: (UUID) -> Unit = {},
    onAddLocationClick: () -> Unit = {},
    onEditLocationClick: (UUID) -> Unit = {},
    onExit: () -> Unit = {},
    viewModel: LocationsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = viewModel.currentParent?.name ?: "Locations",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        if (viewModel.currentParentId != null) {
                            IconButton(onClick = { viewModel.navigateBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                        }
                    }
                )
                NesSearchField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = "Search locations...",
                    modifier = Modifier.padding(horizontal = NesSpacing.sm, vertical = NesSpacing.xs)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLocationClick,
                modifier = Modifier.size(NesSize.minTouchTarget)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
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
                    NesLoadingState(message = "Loading locations...")
                }
            }
            viewModel.displayedLocations.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    NesEmptyState(
                        title = when {
                            viewModel.searchQuery.isNotEmpty() -> "No locations found"
                            viewModel.currentParentId != null -> "No sub-locations"
                            else -> "No locations yet"
                        },
                        message = when {
                            viewModel.searchQuery.isNotEmpty() -> "Try adjusting your search query"
                            viewModel.currentParentId != null -> "Add a sub-location to organize this area"
                            else -> "Add your first location to get started"
                        },
                        icon = if (viewModel.searchQuery.isNotEmpty())
                            Icons.Outlined.FolderOff
                        else
                            Icons.Outlined.Place,
                        action = if (viewModel.searchQuery.isEmpty()) {
                            {
                                NesPrimaryButton(
                                    text = "Add Location",
                                    onClick = onAddLocationClick,
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
                    items(viewModel.displayedLocations) { location ->
                        LocationRow(
                            location = location,
                            serverUrl = viewModel.serverUrl,
                            onNavigate = { viewModel.navigateTo(location.id) },
                            onViewDetails = { onLocationClick(location.id) },
                            onEdit = { onEditLocationClick(location.id) },
                            onDelete = { viewModel.deleteLocation(location.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationRow(
    location: Location,
    serverUrl: String,
    onNavigate: () -> Unit,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NesListItemCard(
            modifier = Modifier.weight(1f),
            onClick = onNavigate
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Photo
                val primaryPhoto = location.location_photos.find { it.is_primary }
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
                            contentDescription = location.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(NesSize.iconSmall)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(NesSpacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    location.friendly_name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Info Button for details
                IconButton(
                    onClick = onViewDetails,
                    modifier = Modifier.size(NesSize.iconDefault)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.primary
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
                            text = { Text("Edit Location") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Location", color = MaterialTheme.colorScheme.error) },
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
