package com.tokendad.nesventory.ui.server

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tokendad.nesventory.ui.components.ConnectionStatus
import com.tokendad.nesventory.ui.components.NesCompactButton
import com.tokendad.nesventory.ui.components.NesPrimaryButton
import com.tokendad.nesventory.ui.components.NesSecondaryButton
import com.tokendad.nesventory.ui.components.NesSectionCard
import com.tokendad.nesventory.ui.components.NesStatusBorder
import com.tokendad.nesventory.ui.components.NesStatusIndicator
import com.tokendad.nesventory.ui.components.NesTextField
import com.tokendad.nesventory.ui.theme.NesSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    remoteUrl: String,
    onRemoteUrlChange: (String) -> Unit,
    localUrl: String,
    onLocalUrlChange: (String) -> Unit,
    localSsid: String,
    onLocalSsidChange: (String) -> Unit,
    availableSsids: List<String>,
    prioritizeLocal: Boolean,
    onPrioritizeLocalChange: (Boolean) -> Unit,
    remoteStatus: Boolean?,
    localStatus: Boolean?,
    aiStatus: Boolean?,
    aiStatusMessage: String?,
    theme: String,
    onThemeChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onTestAIConnection: () -> Unit,
    showPermissionRationale: Boolean,
    onDismissPermissionRationale: () -> Unit,
    onRequestSsidScan: () -> Unit,
    onPrinterSettingsClick: () -> Unit,
    onExit: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        onRequestSsidScan()
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = onDismissPermissionRationale,
            title = { Text("Permission Required") },
            text = { Text("Location permission is required to scan for available Wi-Fi networks (SSIDs). Please grant this permission in App Settings.") },
            confirmButton = {
                TextButton(onClick = {
                    onDismissPermissionRationale()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionRationale) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Settings", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(NesSpacing.sm)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
        ) {
            // Connection Config Section
            NesSectionCard(
                title = "Connection Config",
                icon = Icons.Default.Cloud
            ) {
                // Remote URL with status border
                NesStatusBorder(
                    status = remoteStatus?.let {
                        if (it) ConnectionStatus.Connected else ConnectionStatus.Error
                    }
                ) {
                    NesTextField(
                        value = remoteUrl,
                        onValueChange = onRemoteUrlChange,
                        label = "Remote URL",
                        isError = remoteStatus == false,
                        errorMessage = if (remoteStatus == false) "Connection failed" else null
                    )
                }

                // Local URL with status border
                NesStatusBorder(
                    status = localStatus?.let {
                        if (it) ConnectionStatus.Connected else ConnectionStatus.Error
                    }
                ) {
                    NesTextField(
                        value = localUrl,
                        onValueChange = onLocalUrlChange,
                        label = "Local URL",
                        placeholder = "http://192.168.1.x:8000",
                        isError = localStatus == false,
                        errorMessage = if (localStatus == false) "Connection failed" else null
                    )
                }

                // Connection status summary
                if (remoteStatus != null || localStatus != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NesSpacing.lg)
                    ) {
                        if (remoteStatus != null) {
                            NesStatusIndicator(
                                status = if (remoteStatus) ConnectionStatus.Connected else ConnectionStatus.Error,
                                label = "Remote"
                            )
                        }
                        if (localStatus != null) {
                            NesStatusIndicator(
                                status = if (localStatus) ConnectionStatus.Connected else ConnectionStatus.Error,
                                label = "Local"
                            )
                        }
                    }
                }

                NesCompactButton(
                    text = "Test & Save Connection",
                    onClick = onTestConnection,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // AI Provider Section
            NesSectionCard(
                title = "AI Provider",
                icon = Icons.Default.Psychology
            ) {
                if (!aiStatusMessage.isNullOrBlank()) {
                    NesStatusIndicator(
                        status = if (aiStatus == true) ConnectionStatus.Connected else ConnectionStatus.Error,
                        label = aiStatusMessage
                    )
                }

                NesSecondaryButton(
                    text = "Test AI Connection",
                    onClick = onTestAIConnection
                )
            }

            // Hardware Section
            NesSectionCard(
                title = "Hardware",
                icon = Icons.Default.Print
            ) {
                NesPrimaryButton(
                    text = "Configure Printer",
                    onClick = onPrinterSettingsClick
                )
            }

            // Local Network Section
            NesSectionCard(
                title = "Local Network",
                icon = Icons.Default.Wifi
            ) {
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = localSsid,
                        onValueChange = onLocalSsidChange,
                        label = { Text("Local SSID Name") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth()
                    )

                    if (availableSsids.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableSsids.forEach { ssid ->
                                DropdownMenuItem(
                                    text = { Text(ssid) },
                                    onClick = {
                                        onLocalSsidChange(ssid)
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = prioritizeLocal,
                        onCheckedChange = onPrioritizeLocalChange
                    )
                    Spacer(Modifier.width(NesSpacing.sm))
                    Text(
                        text = "Prioritize Local Connection",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Theme Section
            NesSectionCard(
                title = "App Theme",
                icon = Icons.Default.Palette
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)
                ) {
                    ThemeOption(
                        label = "System",
                        selected = theme == "system",
                        onClick = { onThemeChange("system") },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        label = "Light",
                        selected = theme == "light",
                        onClick = { onThemeChange("light") },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        label = "Dark",
                        selected = theme == "dark",
                        onClick = { onThemeChange("dark") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier
    )
}
