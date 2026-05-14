package com.tokendad.nesventory.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.BulkDeleteRequest
import com.tokendad.nesventory.data.remote.BulkUpdateLocationRequest
import com.tokendad.nesventory.data.remote.BulkUpdateTagsRequest
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.Tag
import com.tokendad.nesventory.data.network.ConnectivityRepository
import com.tokendad.nesventory.data.repository.ItemRepository
import com.tokendad.nesventory.data.repository.LocationRepository
import com.tokendad.nesventory.data.repository.TagRepository
import com.tokendad.nesventory.data.repository.impl.ItemPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.tokendad.nesventory.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val locationRepository: LocationRepository,
    private val tagRepository: TagRepository,
    private val preferencesManager: PreferencesManager,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _locationNames = MutableStateFlow<Map<UUID, String>>(emptyMap())
    val locationNames: StateFlow<Map<UUID, String>> = _locationNames.asStateFlow()

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _livingTypeFilter = MutableStateFlow<LivingItemType?>(null)
    val livingTypeFilter: StateFlow<LivingItemType?> = _livingTypeFilter.asStateFlow()

    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()

    private val _selectedTagId = MutableStateFlow<UUID?>(null)
    val selectedTagId: StateFlow<UUID?> = _selectedTagId.asStateFlow()

    private val _serverUrl = MutableStateFlow(com.tokendad.nesventory.util.Constants.DEFAULT_REMOTE_URL)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<UUID>>(emptySet())
    val selectedItemIds: StateFlow<Set<UUID>> = _selectedItemIds.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    private val _pagingRefreshSignal = MutableStateFlow(0)

    val filteredItems: StateFlow<List<Item>> = combine(
        _items,
        _searchQuery,
        _livingTypeFilter,
        _selectedTagId
    ) { currentItems, query, livingFilter, selectedTagId ->
        val livingFiltered = when (livingFilter) {
            LivingItemType.PERSON -> currentItems.filter {
                LivingItemType.from(it.is_living, it.relationship_type) == LivingItemType.PERSON
            }
            LivingItemType.PET -> currentItems.filter {
                LivingItemType.from(it.is_living, it.relationship_type) == LivingItemType.PET
            }
            LivingItemType.PLANT -> currentItems.filter {
                LivingItemType.from(it.is_living, it.relationship_type) == LivingItemType.PLANT
            }
            LivingItemType.NON_LIVING -> currentItems.filter { !it.is_living }
            null -> currentItems
        }

        val tagFiltered = if (selectedTagId == null) {
            livingFiltered
        } else {
            livingFiltered.filter { item -> item.tags.any { it.id == selectedTagId } }
        }

        if (query.isBlank()) {
            tagFiltered
        } else {
            tagFiltered.filter {
                it.name.contains(query, ignoreCase = true) ||
                    (it.brand?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pagedItems = combine(_searchQuery, _livingTypeFilter, _pagingRefreshSignal) { query, livingFilter, _ ->
        query to livingFilter
    }.flatMapLatest { (query, livingFilter) ->
        val (isLiving, relationshipType) = when (livingFilter) {
            LivingItemType.PET -> true to "pet"
            LivingItemType.PLANT -> true to "plant"
            LivingItemType.NON_LIVING -> false to null
            LivingItemType.PERSON -> true to null
            null -> null to null
        }
        Pager(PagingConfig(pageSize = 30)) {
            ItemPagingSource(
                repository = itemRepository,
                search = query.takeIf { it.isNotBlank() },
                locationId = null,
                isLiving = isLiving,
                relationshipType = relationshipType,
                collectionId = null,
                collectionIdRecursive = null
            )
        }.flow
    }.cachedIn(viewModelScope)

    init {
        fetchData()
        loadServerUrl()
        observeConnectivity()
    }
    
    private fun loadServerUrl() {
        viewModelScope.launch {
            preferencesManager.serverSettings.collect { settings ->
                if (settings.remoteUrl.isNotBlank()) {
                    _serverUrl.value = settings.remoteUrl.trimEnd('/')
                }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityRepository.isConnected.collect { connected ->
                _isOffline.value = !connected
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val (isLiving, relationshipType) = when (_livingTypeFilter.value) {
                    LivingItemType.PET -> true to "pet"
                    LivingItemType.PLANT -> true to "plant"
                    LivingItemType.NON_LIVING -> false to null
                    LivingItemType.PERSON -> true to null
                    null -> null to null
                }
                coroutineScope {
                    val itemsDeferred = async {
                        itemRepository.getItems(
                            isLiving = isLiving,
                            relationshipType = relationshipType
                        )
                    }
                    val locationsDeferred = async { locationRepository.getLocations() }
                    val tagsDeferred = async {
                        runCatching { tagRepository.getTags() }
                            .getOrElse {
                                android.util.Log.w("ItemsViewModel", "Failed to fetch tags", it)
                                emptyList()
                            }
                    }
                    
                    _items.value = itemsDeferred.await()
                    val locations = locationsDeferred.await()
                    _locations.value = locations
                    _locationNames.value = locations.associate { it.id to it.name }
                    _availableTags.value = tagsDeferred.await()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        clearSelection()
    }

    fun onLivingFilterChange(filter: LivingItemType?) {
        _livingTypeFilter.value = filter
    }

    fun onTagFilterChange(tagId: UUID?) {
        _selectedTagId.value = tagId
    }

    fun enterSelectionMode(itemId: UUID) {
        _isSelectionMode.value = true
        _selectedItemIds.value = _selectedItemIds.value + itemId
    }

    fun toggleSelection(itemId: UUID) {
        val current = _selectedItemIds.value
        _selectedItemIds.value = if (current.contains(itemId)) {
            current - itemId
        } else {
            current + itemId
        }
        if (_selectedItemIds.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun selectAll() {
        _selectedItemIds.value = filteredItems.value.map { it.id }.toSet()
        _isSelectionMode.value = _selectedItemIds.value.isNotEmpty()
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun deleteItem(itemId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                itemRepository.deleteItem(itemId)
                fetchData()
                refreshPagedItems()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete item: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bulkDeleteSelected() {
        if (_selectedItemIds.value.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                itemRepository.bulkDeleteItems(BulkDeleteRequest(item_ids = _selectedItemIds.value.toList()))
                fetchData()
                refreshPagedItems()
                clearSelection()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete selected items: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bulkUpdateLocation(locationId: UUID?) {
        if (_selectedItemIds.value.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                itemRepository.bulkUpdateItemLocation(
                    BulkUpdateLocationRequest(
                        item_ids = _selectedItemIds.value.toList(),
                        location_id = locationId
                    )
                )
                fetchData()
                refreshPagedItems()
                clearSelection()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to move selected items: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bulkUpdateTag(tagId: UUID, isAddAction: Boolean) {
        if (_selectedItemIds.value.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                itemRepository.bulkUpdateItemTags(
                    BulkUpdateTagsRequest(
                        item_ids = _selectedItemIds.value.toList(),
                        tag_ids = listOf(tagId),
                        action = if (isAddAction) "add" else "remove"
                    )
                )
                fetchData()
                refreshPagedItems()
                clearSelection()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update tags for selected items: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun refreshPagedItems() {
        _pagingRefreshSignal.value = _pagingRefreshSignal.value + 1
    }
}