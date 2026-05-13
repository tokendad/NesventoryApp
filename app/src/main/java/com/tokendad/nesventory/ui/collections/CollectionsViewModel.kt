package com.tokendad.nesventory.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.CollectionCreate
import com.tokendad.nesventory.data.remote.CollectionUpdate
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.repository.CollectionRepository
import com.tokendad.nesventory.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _collections = MutableStateFlow<List<Collection>>(emptyList())
    val collections: StateFlow<List<Collection>> = _collections.asStateFlow()

    private val _selectedCollectionId = MutableStateFlow<UUID?>(null)
    val selectedCollectionId: StateFlow<UUID?> = _selectedCollectionId.asStateFlow()

    private val _selectedCollection = MutableStateFlow<Collection?>(null)
    val selectedCollection: StateFlow<Collection?> = _selectedCollection.asStateFlow()

    private val _selectedCollectionItems = MutableStateFlow<List<Item>>(emptyList())
    val selectedCollectionItems: StateFlow<List<Item>> = _selectedCollectionItems.asStateFlow()

    private val _availableItems = MutableStateFlow<List<Item>>(emptyList())
    val availableItems: StateFlow<List<Item>> = _availableItems.asStateFlow()

    private val _selectedAssignableItemIds = MutableStateFlow<Set<UUID>>(emptySet())
    val selectedAssignableItemIds: StateFlow<Set<UUID>> = _selectedAssignableItemIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showAssignItemsDialog = MutableStateFlow(false)
    val showAssignItemsDialog: StateFlow<Boolean> = _showAssignItemsDialog.asStateFlow()

    private val _newName = MutableStateFlow("")
    val newName: StateFlow<String> = _newName.asStateFlow()

    private val _newDescription = MutableStateFlow("")
    val newDescription: StateFlow<String> = _newDescription.asStateFlow()

    private val _newIcon = MutableStateFlow("")
    val newIcon: StateFlow<String> = _newIcon.asStateFlow()

    private val _newColor = MutableStateFlow("")
    val newColor: StateFlow<String> = _newColor.asStateFlow()

    init {
        fetchCollections()
    }

    fun fetchCollections() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _collections.value = collectionRepository.getCollections()
                if (_selectedCollectionId.value == null && _collections.value.isNotEmpty()) {
                    _selectedCollectionId.value = _collections.value.first().id
                }
                refreshSelectedCollectionDetails()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load collections: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNewNameChange(value: String) {
        _newName.value = value
    }

    fun onNewDescriptionChange(value: String) {
        _newDescription.value = value
    }

    fun onNewIconChange(value: String) {
        _newIcon.value = value
    }

    fun onNewColorChange(value: String) {
        _newColor.value = value
    }

    fun openCreateDialog() {
        _showCreateDialog.value = true
    }

    fun createCollection() {
        if (_newName.value.isBlank()) {
            _errorMessage.value = "Collection name is required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                collectionRepository.createCollection(
                    CollectionCreate(
                        name = _newName.value.trim(),
                        description = _newDescription.value.ifBlank { null },
                        icon = _newIcon.value.ifBlank { null },
                        color = _newColor.value.ifBlank { null }
                    )
                )
                _showCreateDialog.value = false
                resetFormFields()
                fetchCollections()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create collection: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }

    fun openEditDialog(collection: Collection) {
        _selectedCollectionId.value = collection.id
        _newName.value = collection.name
        _newDescription.value = collection.description ?: ""
        _newIcon.value = collection.icon ?: ""
        _newColor.value = collection.color ?: ""
        _showEditDialog.value = true
    }

    fun updateCollection() {
        val collectionId = _selectedCollectionId.value ?: return
        if (_newName.value.isBlank()) {
            _errorMessage.value = "Collection name is required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                collectionRepository.updateCollection(
                    collectionId,
                    CollectionUpdate(
                        name = _newName.value.trim(),
                        description = _newDescription.value.ifBlank { null },
                        icon = _newIcon.value.ifBlank { null },
                        color = _newColor.value.ifBlank { null }
                    )
                )
                _showEditDialog.value = false
                resetFormFields()
                fetchCollections()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update collection: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }

    fun deleteCollection(collectionId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                collectionRepository.deleteCollection(collectionId)
                if (_selectedCollectionId.value == collectionId) {
                    _selectedCollectionId.value = null
                    _selectedCollection.value = null
                    _selectedCollectionItems.value = emptyList()
                }
                fetchCollections()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete collection: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }

    fun selectCollection(collectionId: UUID) {
        _selectedCollectionId.value = collectionId
        refreshSelectedCollectionDetails()
    }

    private fun refreshSelectedCollectionDetails() {
        val collectionId = _selectedCollectionId.value ?: return
        viewModelScope.launch {
            try {
                _selectedCollection.value = collectionRepository.getCollection(collectionId)
                _selectedCollectionItems.value = collectionRepository.getCollectionItems(collectionId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load collection details: ${e.localizedMessage}"
            }
        }
    }

    fun openAssignItemsDialog() {
        val collectionId = _selectedCollectionId.value ?: return
        viewModelScope.launch {
            try {
                _availableItems.value = itemRepository.getItems()
                val alreadyInCollection = collectionRepository.getCollectionItems(collectionId)
                    .map { it.id }
                    .toSet()
                _selectedAssignableItemIds.value = alreadyInCollection
                _showAssignItemsDialog.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load items: ${e.localizedMessage}"
            }
        }
    }

    fun toggleAssignableItem(itemId: UUID) {
        _selectedAssignableItemIds.value = if (_selectedAssignableItemIds.value.contains(itemId)) {
            _selectedAssignableItemIds.value - itemId
        } else {
            _selectedAssignableItemIds.value + itemId
        }
    }

    fun saveAssignedItems() {
        val collectionId = _selectedCollectionId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentIds = _selectedCollectionItems.value.map { it.id }.toSet()
                val targetIds = _selectedAssignableItemIds.value

                val toAdd = targetIds - currentIds
                val toRemove = currentIds - targetIds

                if (toAdd.isNotEmpty()) {
                    collectionRepository.addItemsToCollection(collectionId, toAdd.toList())
                }
                toRemove.forEach { itemId ->
                    collectionRepository.removeItemFromCollection(collectionId, itemId)
                }

                _showAssignItemsDialog.value = false
                refreshSelectedCollectionDetails()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update collection items: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeItemFromSelectedCollection(itemId: UUID) {
        val collectionId = _selectedCollectionId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                collectionRepository.removeItemFromCollection(collectionId, itemId)
                refreshSelectedCollectionDetails()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove item: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissAssignItemsDialog() {
        _showAssignItemsDialog.value = false
    }

    fun dismissCreateDialog() {
        _showCreateDialog.value = false
        resetFormFields()
    }

    fun dismissEditDialog() {
        _showEditDialog.value = false
        resetFormFields()
    }

    private fun resetFormFields() {
        _newName.value = ""
        _newDescription.value = ""
        _newIcon.value = ""
        _newColor.value = ""
    }
}
