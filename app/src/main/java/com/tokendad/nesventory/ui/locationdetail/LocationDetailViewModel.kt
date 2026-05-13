package com.tokendad.nesventory.ui.locationdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.repository.LocationRepository
import com.tokendad.nesventory.ui.printer.PrintJobRouter
import com.tokendad.nesventory.ui.printer.PrintResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val preferencesManager: PreferencesManager,
    private val printJobRouter: PrintJobRouter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var location by mutableStateOf<Location?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var serverUrl by mutableStateOf(com.tokendad.nesventory.util.Constants.DEFAULT_REMOTE_URL)

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
        loadSettings()
    }
    
    private fun loadSettings() {
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

    fun printLabel() {
        val currentLocation = location ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val result = printJobRouter.printLocation(currentLocation)) {
                is PrintResult.Success -> successMessage = result.message
                is PrintResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }
    
    fun fetchLocation(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                location = locationRepository.getLocation(id)
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
                locationRepository.deleteLocation(currentLocation.id)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.w("LocationDetailViewModel", "Delete failed", e)
                errorMessage = "Failed to delete location: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
