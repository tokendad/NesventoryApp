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

    var locations by mutableStateOf<List<Location>>(emptyList())
    var hierarchicalLocations by mutableStateOf<List<Pair<Location, Int>>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchLocations()
    }

    fun fetchLocations() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val fetchedLocations = api.getLocations()
                locations = fetchedLocations
                hierarchicalLocations = processLocations(fetchedLocations)
            } catch (e: Exception) {
                errorMessage = "Failed to load locations: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun processLocations(allLocations: List<Location>): List<Pair<Location, Int>> {
        val childrenMap = allLocations.groupBy { it.parent_id }
        val roots = childrenMap[null] ?: emptyList()
        
        val sortedRoots = roots.sortedWith(
            compareByDescending<Location> { it.is_primary_location }
                .thenBy { it.name }
        )

        val result = mutableListOf<Pair<Location, Int>>()

        fun addNode(location: Location, depth: Int) {
            result.add(location to depth)
            val children = childrenMap[location.id] ?: emptyList()
            val sortedChildren = children.sortedBy { it.name }
            sortedChildren.forEach { child ->
                addNode(child, depth + 1)
            }
        }

        sortedRoots.forEach { root ->
            addNode(root, 0)
        }

        return result
    }
}
