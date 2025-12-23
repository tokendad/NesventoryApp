package com.tokendad.nesventorynew.ui.locations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    private var allLocations = listOf<Location>()
    var searchQuery by mutableStateOf("")
    
    // Drill-down state
    var currentParentId by mutableStateOf<UUID?>(null)
    private var navigationStack = mutableStateListOf<UUID?>()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val currentParent: Location?
        get() = allLocations.find { it.id == currentParentId }

    val displayedLocations: List<Location>
        get() {
            if (searchQuery.isNotBlank()) {
                return allLocations.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.friendly_name?.contains(searchQuery, ignoreCase = true) == true)
                }
            }

            return allLocations.filter { it.parent_id == currentParentId }
                .sortedWith(
                    compareByDescending<Location> { it.is_primary_location }
                        .thenBy { it.name }
                )
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

    fun navigateTo(parentId: UUID?) {
        if (parentId != currentParentId) {
            navigationStack.add(currentParentId)
            currentParentId = parentId
        }
    }

    fun navigateBack(): Boolean {
        if (navigationStack.isNotEmpty()) {
            currentParentId = navigationStack.removeAt(navigationStack.size - 1)
            return true
        }
        return false
    }
    
    fun deleteLocation(locationId: UUID) {
        viewModelScope.launch {
            isLoading = true
            try {
                api.deleteLocation(locationId)
                fetchLocations()
            } catch (e: Exception) {
                errorMessage = "Failed to delete location: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
