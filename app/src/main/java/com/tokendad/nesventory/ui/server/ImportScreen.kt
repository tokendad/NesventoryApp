package com.tokendad.nesventory.ui.server

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSectionCard
import com.tokendad.nesventory.ui.components.NesSecondaryButton
import com.tokendad.nesventory.ui.theme.NesSpacing

private enum class ImportTool { CSV, ENCIRCLE, NETWORK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBackClick: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedTool by remember { mutableStateOf(ImportTool.CSV) }
    var pendingEncircleUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importCsv(context.contentResolver, it) }
    }

    val encirclePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pendingEncircleUri = it
            viewModel.previewEncircle(context.contentResolver, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Tools") },
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
                FilterChip(
                    selected = selectedTool == ImportTool.CSV,
                    onClick = { selectedTool = ImportTool.CSV },
                    label = { Text("CSV") }
                )
                FilterChip(
                    selected = selectedTool == ImportTool.ENCIRCLE,
                    onClick = { selectedTool = ImportTool.ENCIRCLE },
                    label = { Text("Encircle") }
                )
                FilterChip(
                    selected = selectedTool == ImportTool.NETWORK,
                    onClick = { selectedTool = ImportTool.NETWORK },
                    label = { Text("Network") }
                )
            }

            when (selectedTool) {
                ImportTool.CSV -> {
                    NesSectionCard(title = "CSV Import", icon = Icons.Default.Upload) {
                        Text("Select a CSV file to import inventory data.")
                        NesPrimaryButton(
                            text = "Select CSV File",
                            onClick = { csvPickerLauncher.launch("*/*") },
                            loading = viewModel.isLoading
                        )
                    }
                }

                ImportTool.ENCIRCLE -> {
                    NesSectionCard(title = "Encircle Import", icon = Icons.Default.Upload) {
                        Text("Preview Encircle data before final import.")
                        NesPrimaryButton(
                            text = "Select Encircle Export",
                            onClick = { encirclePickerLauncher.launch("*/*") },
                            loading = viewModel.isLoading
                        )
                        viewModel.encirclePreview?.let { preview ->
                            Text(
                                text = "Preview: ${preview.item_count} items, ${preview.location_count} locations",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                            ) {
                                items(preview.items.take(20)) { item ->
                                    Text(
                                        text = item.name ?: item.id ?: "Unnamed item",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            NesSecondaryButton(
                                text = "Import Encircle Data",
                                onClick = {
                                    pendingEncircleUri?.let {
                                        viewModel.importEncircle(context.contentResolver, it)
                                    } ?: run {
                                        viewModel.errorMessage = "Select an Encircle export file first"
                                    }
                                },
                                loading = viewModel.isLoading
                            )
                        }
                    }
                }

                ImportTool.NETWORK -> {
                    NesSectionCard(title = "Network Discovery", icon = Icons.Default.Upload) {
                        NesPrimaryButton(
                            text = "Scan Network",
                            onClick = viewModel::scanNetwork,
                            loading = viewModel.isLoading
                        )
                        if (viewModel.networkItems.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)
                            ) {
                                items(viewModel.networkItems) { item ->
                                    val itemId = item.id
                                    if (itemId != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = viewModel.selectedNetworkItemIds.contains(itemId),
                                                onCheckedChange = { viewModel.toggleNetworkSelection(itemId) }
                                            )
                                            Column {
                                                Text(item.name ?: "Unnamed")
                                                item.ip_address?.let {
                                                    Text(
                                                        text = it,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            NesSecondaryButton(
                                text = "Import Selected",
                                onClick = viewModel::importSelectedNetworkItems,
                                loading = viewModel.isLoading,
                                enabled = viewModel.selectedNetworkItemIds.isNotEmpty()
                            )
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                NesLoadingState(message = "Processing...")
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
            viewModel.csvResult?.errors?.takeIf { it.isNotEmpty() }?.let { errors ->
                NesSectionCard(title = "Import Errors") {
                    errors.forEach { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
