package com.tokendad.nesventorynew.ui.maintenance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventorynew.data.remote.MaintenanceTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    onExit: () -> Unit = {},
    viewModel: MaintenanceViewModel = hiltViewModel()
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (viewModel.tasks.isEmpty()) {
                Text(
                    "No maintenance tasks found.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.tasks) { task ->
                        MaintenanceTaskRow(task, onToggle = { viewModel.toggleTaskCompletion(task) })
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenanceTaskRow(task: MaintenanceTask, onToggle: () -> Unit) {
    val taskColor = remember(task.color) {
        try {
            if (!task.color.isNullOrBlank()) Color(android.graphics.Color.parseColor(task.color))
            else null
        } catch (_: Exception) {
            null
        }
    } ?: MaterialTheme.colorScheme.primary

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (task.completed) Color.Gray else taskColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.completed) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Due: ${task.due_date}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (!task.description.isNullOrBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }

            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
