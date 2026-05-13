package com.tokendad.nesventory.ui.server

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tokendad.nesventory.data.remote.GDriveBackup
import com.tokendad.nesventory.ui.components.NesDestructiveButton
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSectionCard
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GDriveBackupScreen(
    onBackClick: () -> Unit,
    viewModel: GDriveBackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var backupToDelete by remember { mutableStateOf<GDriveBackup?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (backupToDelete != null) {
        AlertDialog(
            onDismissRequest = { backupToDelete = null },
            title = { Text("Delete backup") },
            text = { Text("Delete '${backupToDelete?.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        backupToDelete?.id?.let(viewModel::deleteBackup)
                        backupToDelete = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { backupToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Drive Backup") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading && viewModel.status == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center
            ) {
                NesLoadingState(message = "Loading backup status...")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            item {
                NesSectionCard(
                    title = "Connection Status",
                    icon = Icons.Default.CloudDone
                ) {
                    val status = viewModel.status
                    Text(
                        text = if (status?.connected == true) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.titleSmall
                    )
                    status?.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Backups: ${status?.backup_count ?: 0}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    status?.last_backup_at?.let {
                        Text(
                            text = "Last backup: ${DateFormatter.formatDateTime(it)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (status?.connected == true) {
                        NesDestructiveButton(
                            text = "Disconnect Google Drive",
                            onClick = viewModel::disconnect,
                            loading = viewModel.isDisconnecting
                        )
                    } else {
                        NesPrimaryButton(
                            text = "Connect to Google Drive",
                            onClick = {
                                viewModel.connect { authUrl ->
                                    val uri = Uri.parse(authUrl)
                                    if (uri.scheme != "https") {
                                        viewModel.errorMessage = "Invalid authorization URL from server"
                                        return@connect
                                    }
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (_: ActivityNotFoundException) {
                                        viewModel.errorMessage = "No browser available to open authorization link"
                                    }
                                }
                            },
                            loading = viewModel.isConnecting
                        )
                    }
                    NesPrimaryButton(
                        text = "Back Up Now",
                        onClick = viewModel::backupNow,
                        loading = viewModel.isBackingUp,
                        enabled = viewModel.status?.connected == true && !viewModel.isDisconnecting
                    )
                }
            }

            item {
                viewModel.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                viewModel.successMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                NesSectionCard(
                    title = "Backups",
                    icon = Icons.Default.Storage
                ) {
                    if (viewModel.backups.isEmpty()) {
                        Text(
                            text = "No backups found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(viewModel.backups, key = { it.id }) { backup ->
                BackupRow(
                    backup = backup,
                    deleting = viewModel.deletingBackupId == backup.id,
                    onDeleteClick = { backupToDelete = backup }
                )
            }
        }
    }
}

@Composable
private fun BackupRow(
    backup: GDriveBackup,
    deleting: Boolean,
    onDeleteClick: () -> Unit
) {
    NesSectionCard(title = backup.name) {
        Text(
            text = "Size: ${formatBytes(backup.size_bytes)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Created: ${DateFormatter.formatDateTime(backup.created_at)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onDeleteClick,
                enabled = !deleting
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text("Delete")
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.2f GB", gb)
}
