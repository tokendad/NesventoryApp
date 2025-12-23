package com.tokendad.nesventorynew.ui.addlocation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.LocationCreate
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddLocationViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var friendlyName by mutableStateOf("")
    var address by mutableStateOf("")
    var selectedParentId by mutableStateOf<UUID?>(null)
    var isPrimaryLocation by mutableStateOf(false)
    var isContainer by mutableStateOf(false)
    
    var availableLocations by mutableStateOf<List<Location>>(emptyList())
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                availableLocations = api.getLocations()
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    fun createLocation(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            errorMessage = "Name is required"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val newLocation = LocationCreate(
                    name = name,
                    description = description.ifBlank { null },
                    friendly_name = friendlyName.ifBlank { null },
                    address = address.ifBlank { null },
                    parent_id = selectedParentId,
                    is_primary_location = isPrimaryLocation,
                    is_container = isContainer
                )
                api.createLocation(newLocation)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to create location: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
