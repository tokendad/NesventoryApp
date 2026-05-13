package com.tokendad.nesventory.ui.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.CollectionCreate
import com.tokendad.nesventory.data.remote.CollectionUpdate
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.repository.CollectionRepository
import com.tokendad.nesventory.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    var collections by mutableStateOf<List<Collection>>(emptyList())
    var selectedCollectionId by mutableStateOf<UUID?>(null)
    var selectedCollection by mutableStateOf<Collection?>(null)
    var selectedCollectionItems by mutableStateOf<List<Item>>(emptyList())
    var availableItems by mutableStateOf<List<Item>>(emptyList())
    var selectedAssignableItemIds by mutableStateOf<Set<UUID>>(emptySet())

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var showCreateDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)
    var showAssignItemsDialog by mutableStateOf(false)

    var newName by mutableStateOf("")
    var newDescription by mutableStateOf("")
    var newIcon by mutableStateOf("")
    var newColor by mutableStateOf("")

    init {
        fetchCollections()
    }

    fun fetchCollections() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                collections = collectionRepository.getCollections()
                if (selectedCollectionId == null && collections.isNotEmpty()) {
                    selectedCollectionId = collections.first().id
                }
                refreshSelectedCollectionDetails()
            } catch (e: Exception) {
                errorMessage = "Failed to load collections: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun createCollection() {
        if (newName.isBlank()) {
            errorMessage = "Collection name is required"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                collectionRepository.createCollection(
                    CollectionCreate(
                        name = newName.trim(),
                        description = newDescription.ifBlank { null },
                        icon = newIcon.ifBlank { null },
                        color = newColor.ifBlank { null }
                    )
                )
                showCreateDialog = false
                resetFormFields()
                fetchCollections()
            } catch (e: Exception) {
                errorMessage = "Failed to create collection: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    fun openEditDialog(collection: Collection) {
        selectedCollectionId = collection.id
        newName = collection.name
        newDescription = collection.description ?: ""
        newIcon = collection.icon ?: ""
        newColor = collection.color ?: ""
        showEditDialog = true
    }

    fun updateCollection() {
        val collectionId = selectedCollectionId ?: return
        if (newName.isBlank()) {
            errorMessage = "Collection name is required"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                collectionRepository.updateCollection(
                    collectionId,
                    CollectionUpdate(
                        name = newName.trim(),
                        description = newDescription.ifBlank { null },
                        icon = newIcon.ifBlank { null },
                        color = newColor.ifBlank { null }
                    )
                )
                showEditDialog = false
                resetFormFields()
                fetchCollections()
            } catch (e: Exception) {
                errorMessage = "Failed to update collection: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    fun deleteCollection(collectionId: UUID) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                collectionRepository.deleteCollection(collectionId)
                if (selectedCollectionId == collectionId) {
                    selectedCollectionId = null
                    selectedCollection = null
                    selectedCollectionItems = emptyList()
                }
                fetchCollections()
            } catch (e: Exception) {
                errorMessage = "Failed to delete collection: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    fun selectCollection(collectionId: UUID) {
        selectedCollectionId = collectionId
        refreshSelectedCollectionDetails()
    }

    private fun refreshSelectedCollectionDetails() {
        val collectionId = selectedCollectionId ?: return
        viewModelScope.launch {
            try {
                selectedCollection = collectionRepository.getCollection(collectionId)
                selectedCollectionItems = collectionRepository.getCollectionItems(collectionId)
            } catch (e: Exception) {
                errorMessage = "Failed to load collection details: ${e.localizedMessage}"
            }
        }
    }

    fun openAssignItemsDialog() {
        val collectionId = selectedCollectionId ?: return
        viewModelScope.launch {
            try {
                availableItems = itemRepository.getItems()
                val alreadyInCollection = collectionRepository.getCollectionItems(collectionId)
                    .map { it.id }
                    .toSet()
                selectedAssignableItemIds = alreadyInCollection
                showAssignItemsDialog = true
            } catch (e: Exception) {
                errorMessage = "Failed to load items: ${e.localizedMessage}"
            }
        }
    }

    fun toggleAssignableItem(itemId: UUID) {
        selectedAssignableItemIds = if (selectedAssignableItemIds.contains(itemId)) {
            selectedAssignableItemIds - itemId
        } else {
            selectedAssignableItemIds + itemId
        }
    }

    fun saveAssignedItems() {
        val collectionId = selectedCollectionId ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val currentIds = selectedCollectionItems.map { it.id }.toSet()
                val targetIds = selectedAssignableItemIds

                val toAdd = targetIds - currentIds
                val toRemove = currentIds - targetIds

                if (toAdd.isNotEmpty()) {
                    collectionRepository.addItemsToCollection(collectionId, toAdd.toList())
                }
                toRemove.forEach { itemId ->
                    collectionRepository.removeItemFromCollection(collectionId, itemId)
                }

                showAssignItemsDialog = false
                refreshSelectedCollectionDetails()
            } catch (e: Exception) {
                errorMessage = "Failed to update collection items: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun removeItemFromSelectedCollection(itemId: UUID) {
        val collectionId = selectedCollectionId ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                collectionRepository.removeItemFromCollection(collectionId, itemId)
                refreshSelectedCollectionDetails()
            } catch (e: Exception) {
                errorMessage = "Failed to remove item: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun dismissCreateDialog() {
        showCreateDialog = false
        resetFormFields()
    }

    fun dismissEditDialog() {
        showEditDialog = false
        resetFormFields()
    }

    private fun resetFormFields() {
        newName = ""
        newDescription = ""
        newIcon = ""
        newColor = ""
    }
}
