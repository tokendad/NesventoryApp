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
import com.tokendad.nesventorynew.ui.components.NesDropdown
import com.tokendad.nesventorynew.ui.components.NesEmptyState
import com.tokendad.nesventorynew.ui.components.NesListItemCard
import com.tokendad.nesventorynew.ui.components.NesLoadingState
import com.tokendad.nesventorynew.ui.components.NesPrimaryButton
import com.tokendad.nesventorynew.ui.components.NesTextField
import com.tokendad.nesventorynew.ui.theme.NesSize
import com.tokendad.nesventorynew.ui.theme.NesSpacing

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
                        .padding(horizontal = NesSpacing.lg, vertical = NesSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)
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
                onClick = { viewModel.showCreateDialog = true },
                modifier = Modifier.size(NesSize.minTouchTarget)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> {
                    NesLoadingState(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Loading tasks..."
                    )
                }
                viewModel.filteredTasks.isEmpty() -> {
                    NesEmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        title = when (viewModel.filterState) {
                            "pending" -> "No pending tasks"
                            "completed" -> "No completed tasks"
                            else -> "No maintenance tasks found"
                        },
                        message = "Add a maintenance task to get started",
                        icon = Icons.Default.CheckCircle
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(NesSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
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
                verticalArrangement = Arrangement.spacedBy(NesSpacing.md)
            ) {
                // Title
                NesTextField(
                    value = viewModel.newTaskTitle,
                    onValueChange = { viewModel.newTaskTitle = it },
                    label = "Title *",
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                NesTextField(
                    value = viewModel.newTaskDescription,
                    onValueChange = { viewModel.newTaskDescription = it },
                    label = "Description",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2
                )

                // Item Selector
                val selectedItemName = viewModel.availableItems.find { it.id == viewModel.newTaskItemId }?.name ?: ""
                NesDropdown(
                    label = "Item *",
                    options = viewModel.availableItems.map { it.name },
                    selectedOption = selectedItemName,
                    onOptionSelected = { name ->
                        viewModel.availableItems.find { it.name == name }?.let {
                            viewModel.newTaskItemId = it.id
                        }
                    }
                )

                // Due Date
                NesTextField(
                    value = viewModel.newTaskDueDate,
                    onValueChange = { viewModel.newTaskDueDate = it },
                    label = "Due Date * (YYYY-MM-DD)",
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "2024-12-31"
                )

                // Frequency Selector
                val selectedFrequencyLabel = frequencies.find { it.first == viewModel.newTaskFrequency }?.second ?: "None"
                NesDropdown(
                    label = "Frequency",
                    options = frequencies.map { it.second },
                    selectedOption = selectedFrequencyLabel,
                    onOptionSelected = { label ->
                        frequencies.find { it.second == label }?.let {
                            viewModel.newTaskFrequency = it.first
                        }
                    }
                )

                // Color Picker
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)
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

    NesListItemCard(onClick = onToggle) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(NesSize.thumbnailSmall)
                    .padding(NesSpacing.xs)
            ) {
                Icon(
                    imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (task.completed) Color.Gray else taskColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(NesSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)
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
