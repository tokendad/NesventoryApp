package com.tokendad.nesventorynew.ui.maintenance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        viewModel.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Maintenance", style = MaterialTheme.typography.titleMedium) },
                    actions = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                        }
                    }
                )
                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.filterState == "all",
                        onClick = { viewModel.filterState = "all" },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = viewModel.filterState == "pending",
                        onClick = { viewModel.filterState = "pending" },
                        label = { Text("Pending") }
                    )
                    FilterChip(
                        selected = viewModel.filterState == "completed",
                        onClick = { viewModel.filterState = "completed" },
                        label = { Text("Completed") }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.filteredTasks.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        when (viewModel.filterState) {
                            "pending" -> "No pending tasks"
                            "completed" -> "No completed tasks"
                            else -> "No maintenance tasks found"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.filteredTasks) { task ->
                        MaintenanceTaskRow(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onDelete = { viewModel.taskToDelete = task }
                        )
                    }
                }
            }
        }
    }

    // Create Task Dialog
    if (viewModel.showCreateDialog) {
        CreateMaintenanceTaskDialog(viewModel = viewModel)
    }

    // Delete Confirmation Dialog
    viewModel.taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { viewModel.taskToDelete = null },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteTask(task) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.taskToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMaintenanceTaskDialog(viewModel: MaintenanceViewModel) {
    var itemExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }

    val frequencies = listOf(null to "None", "daily" to "Daily", "weekly" to "Weekly", "monthly" to "Monthly", "yearly" to "Yearly")
    val colors = listOf(
        "#4CAF50" to "Green",
        "#2196F3" to "Blue",
        "#FF9800" to "Orange",
        "#F44336" to "Red",
        "#9C27B0" to "Purple",
        "#607D8B" to "Gray"
    )

    AlertDialog(
        onDismissRequest = {
            viewModel.showCreateDialog = false
            viewModel.resetCreateForm()
        },
        title = { Text("Create Maintenance Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = viewModel.newTaskTitle,
                    onValueChange = { viewModel.newTaskTitle = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = viewModel.newTaskDescription,
                    onValueChange = { viewModel.newTaskDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Item Selector
                ExposedDropdownMenuBox(
                    expanded = itemExpanded,
                    onExpandedChange = { itemExpanded = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.availableItems.find { it.id == viewModel.newTaskItemId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Item *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = itemExpanded,
                        onDismissRequest = { itemExpanded = false }
                    ) {
                        viewModel.availableItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    viewModel.newTaskItemId = item.id
                                    itemExpanded = false
                                }
                            )
                        }
                    }
                }

                // Due Date
                OutlinedTextField(
                    value = viewModel.newTaskDueDate,
                    onValueChange = { viewModel.newTaskDueDate = it },
                    label = { Text("Due Date * (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("2024-12-31") }
                )

                // Frequency Selector
                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = frequencies.find { it.first == viewModel.newTaskFrequency }?.second ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        frequencies.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.newTaskFrequency = value
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Color Picker
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { (hex, _) ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        FilterChip(
                            selected = viewModel.newTaskColor == hex,
                            onClick = { viewModel.newTaskColor = if (viewModel.newTaskColor == hex) null else hex },
                            label = { },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.createTask() },
                enabled = !viewModel.isCreating && viewModel.newTaskTitle.isNotBlank()
                    && viewModel.newTaskItemId != null && viewModel.newTaskDueDate.isNotBlank()
            ) {
                if (viewModel.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.showCreateDialog = false
                viewModel.resetCreateForm()
            }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MaintenanceTaskRow(
    task: MaintenanceTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit = {}
) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (task.completed) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    // Frequency badge
                    if (!task.frequency.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = task.frequency.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
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

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
