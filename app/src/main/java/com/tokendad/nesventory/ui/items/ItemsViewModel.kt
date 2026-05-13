package com.tokendad.nesventory.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.repository.ItemRepository
import com.tokendad.nesventory.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.tokendad.nesventory.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val locationRepository: LocationRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    var items by mutableStateOf<List<Item>>(emptyList())
    var locationNames by mutableStateOf<Map<UUID, String>>(emptyMap())
    var searchQuery by mutableStateOf("")
    var livingTypeFilter by mutableStateOf<LivingItemType?>(null)
    
    // Default to the known base URL, but update from prefs
    var serverUrl by mutableStateOf(com.tokendad.nesventory.util.Constants.DEFAULT_REMOTE_URL) 
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val filteredItems: List<Item>
        get() {
            val livingFiltered = when (livingTypeFilter) {
                LivingItemType.PERSON -> items.filter {
                    LivingItemType.from(it.is_living, it.relationship_type) == LivingItemType.PERSON
                }
                LivingItemType.PET -> items.filter {
                    LivingItemType.from(it.is_living, it.relationship_type) == LivingItemType.PET
                }
                LivingItemType.PLANT -> items.filter {
                    LivingItemType.from(it.is_living, it.relationship_type) == LivingItemType.PLANT
                }
                LivingItemType.NON_LIVING -> items.filter { !it.is_living }
                null -> items
            }

            return if (searchQuery.isBlank()) {
                livingFiltered
            } else {
                livingFiltered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                (it.brand?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    init {
        fetchData()
        loadServerUrl()
    }
    
    private fun loadServerUrl() {
        viewModelScope.launch {
            val initial = preferencesManager.serverSettings.first()
            if (initial.remoteUrl.isNotBlank()) {
                serverUrl = initial.remoteUrl.trimEnd('/')
            }
            preferencesManager.serverSettings.collect { settings ->
                if (settings.remoteUrl.isNotBlank()) {
                    serverUrl = settings.remoteUrl.trimEnd('/')
                }
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val (isLiving, relationshipType) = when (livingTypeFilter) {
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
                    
                    items = itemsDeferred.await()
                    val locations = locationsDeferred.await()
                    locationNames = locations.associate { it.id to it.name }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load data: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onLivingFilterChange(filter: LivingItemType?) {
        livingTypeFilter = filter
        fetchData()
    }

    fun deleteItem(itemId: UUID) {
        viewModelScope.launch {
            isLoading = true
            try {
                itemRepository.deleteItem(itemId)
                fetchData()
            } catch (e: Exception) {
                errorMessage = "Failed to delete item: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}