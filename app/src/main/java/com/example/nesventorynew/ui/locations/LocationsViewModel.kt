package com.example.nesventorynew.ui.locations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nesventorynew.data.remote.Location
import com.example.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    private var allLocations = listOf<Location>()
    var expandedIds by mutableStateOf(setOf<UUID>())
    var searchQuery by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val displayedLocations: List<Pair<Location, Int>>
        get() {
            if (searchQuery.isNotBlank()) {
                // Flat filter when searching
                return allLocations.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.friendly_name?.contains(searchQuery, ignoreCase = true) == true)
                }.map { it to 0 } // Depth 0 for flat search results
            }

            // Hierarchical tree
            val childrenMap = allLocations.groupBy { it.parent_id }
            val result = mutableListOf<Pair<Location, Int>>()

            fun addNodes(parentId: UUID?, depth: Int) {
                val children = childrenMap[parentId] ?: emptyList()
                val sortedChildren = children.sortedWith(
                    compareByDescending<Location> { it.is_primary_location }
                        .thenBy { it.name }
                )

                sortedChildren.forEach { location ->
                    result.add(location to depth)
                    if (expandedIds.contains(location.id)) {
                        addNodes(location.id, depth + 1)
                    }
                }
            }

            addNodes(null, 0)
            return result
        }

    init {
        fetchLocations()
    }

    fun fetchLocations() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                allLocations = api.getLocations()
            } catch (e: Exception) {
                errorMessage = "Failed to load locations: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun toggleExpansion(locationId: UUID) {
        expandedIds = if (expandedIds.contains(locationId)) {
            expandedIds - locationId
        } else {
            expandedIds + locationId
        }
    }
    
    fun hasChildren(locationId: UUID): Boolean {
        return allLocations.any { it.parent_id == locationId }
    }
}
