package com.tokendad.nesventory.ui.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tokendad.nesventory.data.preferences.ServerProfile
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesListItemCard
import com.tokendad.nesventory.ui.theme.NesSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerProfilesScreen(
    onBackClick: () -> Unit,
    onAddProfile: () -> Unit,
    onEditProfile: (String) -> Unit,
    viewModel: ServerProfilesViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Profiles") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProfile) {
                Icon(Icons.Default.Add, contentDescription = "Add profile")
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            NesEmptyState(
                title = "No profiles configured",
                message = "Add a server profile to switch between environments.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(NesSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ServerProfileRow(
                        profile = profile,
                        isActive = profile.id == activeProfileId,
                        onSetActive = { viewModel.setActiveProfile(profile.id) },
                        onEdit = { onEditProfile(profile.id) },
                        onDelete = { viewModel.deleteProfile(profile.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerProfileRow(
    profile: ServerProfile,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    NesListItemCard(modifier = Modifier.fillMaxWidth(), onClick = onSetActive) {
        Column(verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)) {
            Text(text = profile.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = profile.remoteUrl.ifBlank { profile.localUrl },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(NesSpacing.xs)
            ) {
                IconButton(onClick = onSetActive) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Set active",
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete, enabled = !isActive) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
