package com.tokendad.nesventorynew.ui.locationdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val api: NesVentoryApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var location by mutableStateOf<Location?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        val locationIdString: String? = savedStateHandle["locationId"]
        if (locationIdString != null) {
             try {
                 val id = UUID.fromString(locationIdString)
                 fetchLocation(id)
             } catch (e: IllegalArgumentException) {
                 errorMessage = "Invalid Location ID format"
             }
        }
    }

    fun fetchLocation(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                location = api.getLocation(id)
            } catch (e: Exception) {
                errorMessage = "Failed to load location details: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteLocation(onSuccess: () -> Unit) {
        val currentLocation = location ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                api.deleteLocation(currentLocation.id)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to delete location: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }
}
