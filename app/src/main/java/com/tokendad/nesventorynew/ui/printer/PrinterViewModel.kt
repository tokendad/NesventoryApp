package com.tokendad.nesventorynew.ui.printer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.data.remote.PrinterConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    var config by mutableStateOf(PrinterConfig())
        private set
        
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    val supportedModels = listOf("D11", "D110", "B1", "B18", "B21")
    val supportedInterfaces = listOf("bluetooth", "usb", "serial", "tcp")

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            isLoading = true
            try {
                config = api.getPrinterConfig()
            } catch (e: Exception) {
                errorMessage = "Failed to load printer config: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun onModelChange(model: String) {
        config = config.copy(model = model)
    }

    fun onInterfaceChange(interfaceType: String) {
        config = config.copy(interface_type = interfaceType)
    }

    fun onAddressChange(address: String) {
        config = config.copy(address = address)
    }
    
    fun onDensityChange(density: Int) {
        config = config.copy(density = density)
    }

    fun saveConfig() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                config = api.updatePrinterConfig(config)
                successMessage = "Printer configuration saved successfully!"
            } catch (e: Exception) {
                errorMessage = "Failed to save config: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}