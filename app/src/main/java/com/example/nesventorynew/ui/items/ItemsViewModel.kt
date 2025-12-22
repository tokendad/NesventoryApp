package com.example.nesventorynew.ui.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.remote.Item
import com.example.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    var items by mutableStateOf<List<Item>>(emptyList())
    var locationNames by mutableStateOf<Map<UUID, String>>(emptyMap())
    var searchQuery by mutableStateOf("")
    
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
    }

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                coroutineScope {
                    val itemsDeferred = async { api.getItems() }
                    val locationsDeferred = async { api.getLocations() }
                    
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
}