package com.tokendad.nesventorynew.ui.maintenance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    onExit: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Maintenance Calendar Coming Soon")
        }
    }
}
