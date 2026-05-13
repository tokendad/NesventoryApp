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
import com.tokendad.nesventory.data.repository.PrinterRepository
import com.tokendad.nesventory.ui.printer.PrintJobExecutor
import com.tokendad.nesventory.ui.printer.PrinterModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val printerRepository: PrinterRepository,
    private val preferencesManager: PreferencesManager,
    private val printJobExecutor: PrintJobExecutor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var location by mutableStateOf<Location?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var serverUrl by mutableStateOf(com.tokendad.nesventory.util.Constants.DEFAULT_REMOTE_URL)

    private var printMethod by mutableStateOf("local")
    private var selectedModel by mutableStateOf(PrinterModel.D11_H)
    private var localDensity by mutableStateOf(3)

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
            printMethod = initial.printMethod
            if (initial.remoteUrl.isNotBlank()) {
                serverUrl = initial.remoteUrl.trimEnd('/')
            }
            PrinterModel.fromString(initial.localPrinterModel)?.let {
                selectedModel = it
            }
            localDensity = initial.localPrinterDensity

            preferencesManager.serverSettings.collect { settings ->
                printMethod = settings.printMethod
                if (settings.remoteUrl.isNotBlank()) {
                    serverUrl = settings.remoteUrl.trimEnd('/')
                }
                // Load selected local printer model
                PrinterModel.fromString(settings.localPrinterModel)?.let {
                    selectedModel = it
                }
                localDensity = settings.localPrinterDensity
            }
        }
    }

    fun printLabel() {
        if (printMethod == "server") {
            printLabelOnServer()
        } else {
            printLabelLocally()
        }
    }

    private fun printLabelOnServer() {
        val currentLocation = location ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val request = com.tokendad.nesventory.data.remote.PrintJobRequest(
                    entity_id = currentLocation.id,
                    entity_type = "location",
                    quantity = 1
                )
                printerRepository.printLabel(request)
                successMessage = "Print job sent to server!"
            } catch (e: Exception) {
                errorMessage = "Server print failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun printLabelLocally() {
        val currentLocation = location ?: return

        if (!printJobExecutor.isConnected()) {
            errorMessage = "Printer not connected. Go to Printer Settings."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val qrUrl = "${serverUrl}/api/locations/${currentLocation.id}"
                printJobExecutor.printLabel(
                    labelText = currentLocation.name,
                    labelSubtitle = currentLocation.id.toString().take(8),
                    qrContent = qrUrl,
                    iconType = "location",
                    model = selectedModel,
                    density = localDensity
                )
                withContext(Dispatchers.Main) {
                    successMessage = "Label printed successfully!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Print failed: ${e.localizedMessage}"
                }
            } finally {
                isLoading = false
            }
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
