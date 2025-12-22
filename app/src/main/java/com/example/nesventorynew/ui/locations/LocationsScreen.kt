package com.example.nesventorynew.ui.locations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nesventorynew.data.remote.Location
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    onLocationClick: (UUID) -> Unit = {},
    viewModel: LocationsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Locations") }) }
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
                items(viewModel.hierarchicalLocations) { (location, depth) ->
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(location.name, style = MaterialTheme.typography.titleLarge)
                location.friendly_name?.let { Text("($it)", style = MaterialTheme.typography.bodyMedium) }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (location.is_primary_location) {
                        AssistChip(onClick = {}, label = { Text("Primary") })
                    }
                    if (location.is_container) {
                        AssistChip(onClick = {}, label = { Text("Container") })
                    }
                }
            }
        }
    }
}
