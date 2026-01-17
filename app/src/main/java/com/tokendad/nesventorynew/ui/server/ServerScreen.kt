package com.tokendad.nesventorynew.ui.server

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.provider.Settings

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
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Connection Config
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Connection Config", style = MaterialTheme.typography.labelLarge)
                    
                    // Remote URL
                    OutlinedTextField(
                        value = remoteUrl,
                        onValueChange = onRemoteUrlChange,
                        label = { Text("Remote URL", style = MaterialTheme.typography.bodySmall) },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = if (remoteStatus != null) 2.dp else 0.dp,
                                color = when (remoteStatus) {
                                    true -> Color.Green
                                    false -> Color.Red
                                    null -> Color.Transparent
                                },
                                shape = RoundedCornerShape(4.dp)
                            ),
                        textStyle = MaterialTheme.typography.bodySmall,
                        supportingText = if (remoteStatus == false) {
                            { Text("Connection failed", color = Color.Red, style = MaterialTheme.typography.labelSmall) }
                        } else null
                    )

                    // Local URL
                    OutlinedTextField(
                        value = localUrl,
                        onValueChange = onLocalUrlChange,
                        label = { Text("Local URL", style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text("http://192.168.1.x:8000") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = if (localStatus != null) 2.dp else 0.dp,
                                color = when (localStatus) {
                                    true -> Color.Green
                                    false -> Color.Red
                                    null -> Color.Transparent
                                },
                                shape = RoundedCornerShape(4.dp)
                            ),
                        textStyle = MaterialTheme.typography.bodySmall,
                        supportingText = if (localStatus == false) {
                            { Text("Connection failed", color = Color.Red, style = MaterialTheme.typography.labelSmall) }
                        } else null
                    )

                    // Connection status summary
                    if (remoteStatus != null || localStatus != null) {
                        val statusText = buildString {
                            if (remoteStatus == true) append("Remote: Connected")
                            else if (remoteStatus == false) append("Remote: Failed")

                            if (remoteStatus != null && localStatus != null) append(" | ")

                            if (localStatus == true) append("Local: Connected")
                            else if (localStatus == false) append("Local: Failed")
                        }
                        if (statusText.isNotEmpty()) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if ((remoteStatus == false && localStatus != true) ||
                                           (localStatus == false && remoteStatus != true))
                                    MaterialTheme.colorScheme.error
                                else if (remoteStatus == true || localStatus == true)
                                    Color.Green
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = onTestConnection,
                        modifier = Modifier.align(Alignment.End).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Test & Save Connection", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // AI Provider Settings
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("AI Provider", style = MaterialTheme.typography.labelLarge)
                    
                    if (!aiStatusMessage.isNullOrBlank()) {
                        Text(
                            text = aiStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (aiStatus == true) Color.Green else MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = onTestAIConnection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (aiStatus != null) 2.dp else 0.dp,
                                color = if (aiStatus == true) Color.Green else if (aiStatus == false) Color.Red else Color.Transparent,
                                shape = RoundedCornerShape(20.dp) // Match button shape usually
                            )
                    ) {
                        Text("Test AI Connection")
                    }
                }
            }
            
             // Printer Settings
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Hardware", style = MaterialTheme.typography.labelLarge)
                    Button(
                        onClick = onPrinterSettingsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Configure Printer")
                    }
                }
            }

            // Local Network Settings
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Local Network", style = MaterialTheme.typography.labelLarge)
                    
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = localSsid,
                            onValueChange = onLocalSsidChange,
                            label = { Text("Local SSID Name", style = MaterialTheme.typography.bodySmall) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryEditable, true)
                                .fillMaxWidth()
                                .height(56.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        
                        if (availableSsids.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableSsids.forEach { ssid ->
                                    DropdownMenuItem(
                                        text = { Text(ssid, style = MaterialTheme.typography.bodySmall) },
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
                            onCheckedChange = onPrioritizeLocalChange,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Prioritize Local Connection", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // Theme Settings
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("App Theme", style = MaterialTheme.typography.labelLarge)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ThemeOption(
                            label = "System", 
                            selected = theme == "system", 
                            onClick = { onThemeChange("system") }
                        )
                        ThemeOption(
                            label = "Light", 
                            selected = theme == "light", 
                            onClick = { onThemeChange("light") }
                        )
                        ThemeOption(
                            label = "Dark", 
                            selected = theme == "dark", 
                            onClick = { onThemeChange("dark") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}
