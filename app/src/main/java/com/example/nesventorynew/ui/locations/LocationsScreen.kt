package com.example.nesventorynew.ui.locations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nesventorynew.data.remote.Location
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    onLocationClick: (UUID) -> Unit = {},
    onAddLocationClick: () -> Unit = {},
    viewModel: LocationsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("Locations") })
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search locations...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLocationClick) {
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.filteredHierarchicalLocations) { (location, depth) ->
                    LocationRow(location, depth, onClick = { onLocationClick(location.id) })
                }
            }
        }
    }
}

@Composable
fun LocationRow(location: Location, depth: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (depth > 0) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp).padding(end = 4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Photo
                val primaryPhoto = location.location_photos.find { it.is_primary }
                val imageUrl = primaryPhoto?.let { photo ->
                    if (photo.path.startsWith("http")) photo.path 
                    else "https://nesdemo.welshrd.com/${photo.path.removePrefix("/")}"
                }

                Card(
                    modifier = Modifier.size(48.dp),
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(location.name, style = MaterialTheme.typography.titleMedium)
                    location.friendly_name?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    
                    if (location.is_primary_location || location.is_container) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (location.is_primary_location) {
                                Text("Primary", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            if (location.is_container) {
                                Text("Container", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}
