package com.example.nesventorynew.ui.serversettings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSettingsScreen(
    viewModel: ServerSettingsViewModel = hiltViewModel(),
    onSettingsSaved: () -> Unit
) {
    val settings by viewModel.serverSettings.collectAsState()

    // Local state for the text fields
    var remoteUrl by remember(settings.remoteUrl) { mutableStateOf(settings.remoteUrl) }
    var localUrl by remember(settings.localUrl) { mutableStateOf(settings.localUrl) }
    var apiToken by remember(settings.apiToken) { mutableStateOf(settings.apiToken) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Server Connection") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = remoteUrl,
                onValueChange = { remoteUrl = it },
                label = { Text("Remote Server URL (HTTPS)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localUrl,
                onValueChange = { localUrl = it },
                label = { Text("Local Server IP (Home Wi-Fi)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apiToken,
                onValueChange = { apiToken = it },
                label = { Text("API Token (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveSettings(settings.copy(
                        remoteUrl = remoteUrl,
                        localUrl = localUrl,
                        apiToken = apiToken
                    ))
                    onSettingsSaved()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save and Continue")
            }
        }
    }
}