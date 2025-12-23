package com.tokendad.nesventorynew.ui.server

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    localUrl: String,
    onLocalUrlChange: (String) -> Unit,
    localSsid: String,
    onLocalSsidChange: (String) -> Unit,
    prioritizeLocal: Boolean,
    onPrioritizeLocalChange: (Boolean) -> Unit,
    remoteStatus: Boolean?,
    localStatus: Boolean?,
    theme: String,
    onThemeChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onExit: () -> Unit = {}
) {
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
                        value = "https://nesdemo.welshrd.com/",
                        onValueChange = { },
                        label = { Text("Remote URL", style = MaterialTheme.typography.bodySmall) },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = if (remoteStatus == true) 2.dp else 0.dp,
                                color = if (remoteStatus == true) Color.Green else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        textStyle = MaterialTheme.typography.bodySmall
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
                                width = if (localStatus == true) 2.dp else 0.dp,
                                color = if (localStatus == true) Color.Green else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
                    Button(
                        onClick = onTestConnection,
                        modifier = Modifier.align(Alignment.End).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Test Connections", style = MaterialTheme.typography.bodySmall)
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
                    
                    OutlinedTextField(
                        value = localSsid,
                        onValueChange = onLocalSsidChange,
                        label = { Text("Local SSID Name", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
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
