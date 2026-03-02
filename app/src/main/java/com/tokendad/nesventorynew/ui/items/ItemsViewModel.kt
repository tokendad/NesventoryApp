package com.tokendad.nesventorynew.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.repository.ItemRepository
import com.tokendad.nesventorynew.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.tokendad.nesventorynew.data.preferences.PreferencesManager
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
    
    // Default to the known base URL, but update from prefs
    var serverUrl by mutableStateOf(com.tokendad.nesventorynew.util.Constants.DEFAULT_REMOTE_URL) 
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val filteredItems: List<Item>
        get() = if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                (it.brand?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

    init {
        fetchData()
        loadServerUrl()
    }
    
    private fun loadServerUrl() {
        viewModelScope.launch {
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
                coroutineScope {
                    val itemsDeferred = async { itemRepository.getItems() }
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