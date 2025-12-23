package com.tokendad.nesventorynew.ui.locations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tokendad.nesventorynew.data.remote.Location
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
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .height(50.dp), // Compact height
                    placeholder = { Text("Search locations...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLocationClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
            }
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(viewModel.displayedLocations) { location ->
                    LocationRow(
                        location = location,
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

@Composable
fun LocationRow(
    location: Location,
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
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigate() }
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Photo
                val primaryPhoto = location.location_photos.find { it.is_primary }
                val imageUrl = primaryPhoto?.let { photo ->
                    if (photo.path.startsWith("http")) photo.path 
                    else "https://nesdemo.welshrd.com/${photo.path.removePrefix("/")}"
                }

                Card(
                    modifier = Modifier.size(40.dp),
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
                            Text("No Img", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(location.name, style = MaterialTheme.typography.titleSmall)
                    location.friendly_name?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                
                // Info Button for details
                IconButton(
                    onClick = onViewDetails,
                    modifier = Modifier.size(24.dp)
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
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
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