package com.tokendad.nesventory.ui.server

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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.RotateRight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSecondaryButton
import com.tokendad.nesventory.ui.components.NesSectionCard
import com.tokendad.nesventory.ui.theme.NesSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLogsScreen(
    onBackClick: () -> Unit,
    viewModel: AdminLogsViewModel = hiltViewModel()
) {
    var showRotateConfirm by remember { mutableStateOf(false) }
    var showCleanupConfirm by remember { mutableStateOf(false) }

    if (showRotateConfirm) {
        AlertDialog(
            onDismissRequest = { showRotateConfirm = false },
            title = { Text("Rotate logs") },
            text = { Text("Rotate the active log file now?") },
            confirmButton = {
                TextButton(onClick = {
                    showRotateConfirm = false
                    viewModel.rotateLogs()
                }) { Text("Rotate") }
            },
            dismissButton = {
                TextButton(onClick = { showRotateConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showCleanupConfirm) {
        AlertDialog(
            onDismissRequest = { showCleanupConfirm = false },
            title = { Text("Cleanup logs") },
            text = { Text("Delete old log files? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showCleanupConfirm = false
                    viewModel.cleanupLogs()
                }) { Text("Cleanup") }
            },
            dismissButton = {
                TextButton(onClick = { showCleanupConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Logs") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(NesSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)
            ) {
                NesSecondaryButton(
                    text = "Rotate",
                    onClick = { showRotateConfirm = true },
                    icon = Icons.Default.RotateRight,
                    enabled = !viewModel.isMutatingLogs,
                    fullWidth = false
                )
                NesSecondaryButton(
                    text = "Cleanup",
                    onClick = { showCleanupConfirm = true },
                    icon = Icons.Default.CleaningServices,
                    enabled = !viewModel.isMutatingLogs,
                    fullWidth = false
                )
                NesPrimaryButton(
                    text = "Refresh",
                    onClick = viewModel::loadLogFiles,
                    enabled = !viewModel.isLoading,
                    fullWidth = false
                )
            }

            viewModel.successMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            viewModel.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            NesSectionCard(title = "Log Files") {
                if (viewModel.isLoading) {
                    NesLoadingState(message = "Loading logs...")
                } else if (viewModel.logFiles.isEmpty()) {
                    Text(
                        text = "No log files found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                    ) {
                        items(viewModel.logFiles) { filename ->
                            TextButton(
                                onClick = { viewModel.openLog(filename) }
                            ) {
                                Text(filename)
                            }
                        }
                    }
                }
            }

            NesSectionCard(title = viewModel.selectedLogFile ?: "Log Viewer") {
                if (viewModel.isReadingLog) {
                    NesLoadingState(message = "Reading log file...")
                } else if (viewModel.logLines.isEmpty()) {
                    Text(
                        text = "Select a log file to view content.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                    ) {
                        items(viewModel.logLines) { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
