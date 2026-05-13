package com.tokendad.nesventory.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.repository.ItemRepository
import com.tokendad.nesventory.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.tokendad.nesventory.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val locationRepository: LocationRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _locationNames = MutableStateFlow<Map<UUID, String>>(emptyMap())
    val locationNames: StateFlow<Map<UUID, String>> = _locationNames.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _livingTypeFilter = MutableStateFlow<LivingItemType?>(null)
    val livingTypeFilter: StateFlow<LivingItemType?> = _livingTypeFilter.asStateFlow()

    private val _serverUrl = MutableStateFlow(com.tokendad.nesventory.util.Constants.DEFAULT_REMOTE_URL)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val filteredItems: StateFlow<List<Item>> = combine(
        _items,
        _searchQuery,
        _livingTypeFilter
    ) { currentItems, query, livingFilter ->
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

        if (query.isBlank()) {
            livingFiltered
        } else {
            livingFiltered.filter {
                it.name.contains(query, ignoreCase = true) ||
                    (it.brand?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        fetchData()
        loadServerUrl()
    }
    
    private fun loadServerUrl() {
        viewModelScope.launch {
            val initial = preferencesManager.serverSettings.first()
            if (initial.remoteUrl.isNotBlank()) {
                _serverUrl.value = initial.remoteUrl.trimEnd('/')
            }
            preferencesManager.serverSettings.collect { settings ->
                if (settings.remoteUrl.isNotBlank()) {
                    _serverUrl.value = settings.remoteUrl.trimEnd('/')
                }
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
                    
                    _items.value = itemsDeferred.await()
                    val locations = locationsDeferred.await()
                    _locationNames.value = locations.associate { it.id to it.name }
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
    }

    fun onLivingFilterChange(filter: LivingItemType?) {
        _livingTypeFilter.value = filter
        fetchData()
    }

    fun deleteItem(itemId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                itemRepository.deleteItem(itemId)
                fetchData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete item: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}