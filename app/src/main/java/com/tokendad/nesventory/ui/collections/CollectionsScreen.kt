package com.tokendad.nesventory.ui.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.ui.components.NesCard
import com.tokendad.nesventory.ui.components.NesEmptyState
import com.tokendad.nesventory.ui.components.NesErrorState
import com.tokendad.nesventory.ui.components.NesListItemCard
import com.tokendad.nesventory.ui.components.NesLoadingState
import com.tokendad.nesventory.ui.components.NesSecondaryButton
import com.tokendad.nesventory.ui.components.NesTextField
import com.tokendad.nesventory.ui.theme.NesSpacing
import com.tokendad.nesventory.util.ColorUtils

@Composable
fun CollectionsScreen(
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    if (viewModel.showCreateDialog) {
        CollectionEditorDialog(
            title = "New Collection",
            confirmLabel = "Create",
            name = viewModel.newName,
            onNameChange = { viewModel.newName = it },
            description = viewModel.newDescription,
            onDescriptionChange = { viewModel.newDescription = it },
            icon = viewModel.newIcon,
            onIconChange = { viewModel.newIcon = it },
            color = viewModel.newColor,
            onColorChange = { viewModel.newColor = it },
            onConfirm = viewModel::createCollection,
            onDismiss = viewModel::dismissCreateDialog
        )
    }

    if (viewModel.showEditDialog) {
        CollectionEditorDialog(
            title = "Edit Collection",
            confirmLabel = "Save",
            name = viewModel.newName,
            onNameChange = { viewModel.newName = it },
            description = viewModel.newDescription,
            onDescriptionChange = { viewModel.newDescription = it },
            icon = viewModel.newIcon,
            onIconChange = { viewModel.newIcon = it },
            color = viewModel.newColor,
            onColorChange = { viewModel.newColor = it },
            onConfirm = viewModel::updateCollection,
            onDismiss = viewModel::dismissEditDialog
        )
    }

    if (viewModel.showAssignItemsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showAssignItemsDialog = false },
            title = { Text("Assign Items") },
            text = {
                if (viewModel.availableItems.isEmpty()) {
                    Text("No items available.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(NesSpacing.xs)) {
                        items(viewModel.availableItems) { item ->
                            val checked = viewModel.selectedAssignableItemIds.contains(item.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = checked,
                                        onValueChange = { viewModel.toggleAssignableItem(item.id) }
                                    )
                                    .padding(vertical = NesSpacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { viewModel.toggleAssignableItem(item.id) }
                                )
                                Text(item.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveAssignedItems) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showAssignItemsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add collection")
            }
        }
    ) { padding ->
        when {
            viewModel.isLoading && viewModel.collections.isEmpty() -> {
                NesLoadingState(
                    message = "Loading collections...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            viewModel.errorMessage != null && viewModel.collections.isEmpty() -> {
                NesErrorState(
                    title = "Couldn't load collections",
                    message = viewModel.errorMessage ?: "Failed to load collections",
                    onRetry = viewModel::fetchCollections,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            viewModel.collections.isEmpty() -> {
                NesEmptyState(
                    title = "No collections",
                    message = "Create a collection to organize related items.",
                    icon = Icons.Outlined.Collections,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(NesSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)
                ) {
                    item {
                        Text(
                            text = "Collections",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    items(viewModel.collections) { collection ->
                        CollectionRow(
                            collection = collection,
                            selected = viewModel.selectedCollectionId == collection.id,
                            onSelect = { viewModel.selectCollection(collection.id) },
                            onEdit = { viewModel.openEditDialog(collection) },
                            onDelete = { viewModel.deleteCollection(collection.id) }
                        )
                    }

                    viewModel.selectedCollection?.let { selected ->
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = NesSpacing.sm))
                        }
                        item {
                            CollectionDetailsCard(
                                collection = selected,
                                itemCount = viewModel.selectedCollectionItems.size,
                                onAssignItems = viewModel::openAssignItemsDialog
                            )
                        }
                        if (viewModel.selectedCollectionItems.isEmpty()) {
                            item {
                                NesEmptyState(
                                    title = "No items in this collection",
                                    message = "Use Assign Items to add related items.",
                                    icon = Icons.Default.Collections
                                )
                            }
                        } else {
                            items(viewModel.selectedCollectionItems) { item ->
                                NesListItemCard(onClick = {}) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.titleSmall)
                                            item.brand?.takeIf { it.isNotBlank() }?.let {
                                                Spacer(modifier = Modifier.height(NesSpacing.xs))
                                                Text(it, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeItemFromSelectedCollection(item.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RemoveCircle,
                                                contentDescription = "Remove ${item.name}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionRow(
    collection: Collection,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = ColorUtils.parseHexColor(collection.color)
    NesListItemCard(onClick = onSelect) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)
                ) {
                    Text(collection.icon?.takeIf { it.isNotBlank() } ?: "🗂️")
                    Text(collection.name, style = MaterialTheme.typography.titleSmall)
                    if (selected) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Selected") },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = accent?.copy(alpha = 0.18f)
                                    ?: MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
                collection.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(NesSpacing.xs))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(NesSpacing.xs))
                Text(
                    "Items: ${collection.item_count} • Total: ${collection.total_item_count}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit ${collection.name}")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete ${collection.name}")
                }
            }
        }
    }
}

@Composable
private fun CollectionDetailsCard(
    collection: Collection,
    itemCount: Int,
    onAssignItems: () -> Unit
) {
    NesCard(
        title = collection.name,
        subtitle = "Manage membership and shared grouping details.",
        icon = Icons.Default.Collections
    ) {
        collection.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(NesSpacing.sm))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Current items: $itemCount")
            NesSecondaryButton(
                text = "Assign Items",
                onClick = onAssignItems,
                fullWidth = false,
                icon = Icons.Default.Link
            )
        }
    }
}

@Composable
private fun CollectionEditorDialog(
    title: String,
    confirmLabel: String,
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    icon: String,
    onIconChange: (String) -> Unit,
    color: String,
    onColorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                NesTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Name *",
                    modifier = Modifier.fillMaxWidth()
                )
                NesTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = "Description",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(NesSpacing.sm)) {
                    NesTextField(
                        value = icon,
                        onValueChange = onIconChange,
                        label = "Icon (emoji)",
                        modifier = Modifier.weight(1f)
                    )
                    NesTextField(
                        value = color,
                        onValueChange = onColorChange,
                        label = "Color (#RRGGBB)",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
