package com.tokendad.nesventorynew.ui.edititem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.ItemCreate
import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val api: NesVentoryApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var brand by mutableStateOf("")
    var modelNumber by mutableStateOf("")
    var serialNumber by mutableStateOf("")
    var purchasePrice by mutableStateOf("")
    var purchaseDate by mutableStateOf("")
    var estimatedValue by mutableStateOf("")
    var retailer by mutableStateOf("")
    var selectedLocationId by mutableStateOf<UUID?>(null)
    
    var availableLocations by mutableStateOf<List<Location>>(emptyList())
    var itemId: UUID? = null

    var maintenanceTasks by mutableStateOf<List<com.tokendad.nesventorynew.data.remote.MaintenanceTask>>(emptyList())
    var itemMedia by mutableStateOf<List<com.tokendad.nesventorynew.data.remote.Photo>>(emptyList())

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Enrichment Review State
    var isReviewingEnrichment by mutableStateOf(false)
    private var originalValues = mapOf<String, String>()

    init {
        val idString: String? = savedStateHandle["itemId"]
        if (idString != null) {
            itemId = UUID.fromString(idString)
            fetchItem(itemId!!)
        }
        fetchLocations()
    }

    // ... (existing fetchItem, fetchMaintenanceTasks, fetchLocations, updateItem)

    fun isFieldModified(fieldName: String, currentValue: String): Boolean {
        return isReviewingEnrichment && originalValues[fieldName] != currentValue
    }

    fun acceptEnrichment() {
        isReviewingEnrichment = false
        originalValues = emptyMap()
    }

    fun discardEnrichment() {
        if (isReviewingEnrichment) {
            description = originalValues["description"] ?: description
            brand = originalValues["brand"] ?: brand
            modelNumber = originalValues["modelNumber"] ?: modelNumber
            serialNumber = originalValues["serialNumber"] ?: serialNumber
            estimatedValue = originalValues["estimatedValue"] ?: estimatedValue
            isReviewingEnrichment = false
            originalValues = emptyMap()
        }
    }

    private fun fetchItem(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            try {
                val item = api.getItem(id)
                itemMedia = item.photos
                name = item.name
                description = item.description ?: ""
                brand = item.brand ?: ""
                modelNumber = item.model_number ?: ""
                serialNumber = item.serial_number ?: ""
                purchasePrice = item.purchase_price ?: ""
                purchaseDate = item.purchase_date ?: ""
                estimatedValue = item.estimated_value ?: ""
                retailer = item.retailer ?: ""
                selectedLocationId = item.location_id
            } catch (e: Exception) {
                errorMessage = "Failed to load item: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
        fetchMaintenanceTasks(id)
    }

    private fun fetchMaintenanceTasks(id: UUID) {
        viewModelScope.launch {
            try {
                maintenanceTasks = api.getMaintenanceTasksForItem(id)
            } catch (_: Exception) {
                // Fail silently for secondary tabs
            }
        }
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                availableLocations = api.getLocations()
            } catch (_: Exception) {
                // Fail silently
            }
        }
    }

    fun updateItem(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            errorMessage = "Name is required"
            return
        }

        val id = itemId ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val updatedItem = ItemCreate(
                    name = name,
                    description = description.ifBlank { null },
                    brand = brand.ifBlank { null },
                    model_number = modelNumber.ifBlank { null },
                    serial_number = serialNumber.ifBlank { null },
                    purchase_price = purchasePrice.ifBlank { null },
                    purchase_date = purchaseDate.ifBlank { null },
                    estimated_value = estimatedValue.ifBlank { null },
                    retailer = retailer.ifBlank { null },
                    location_id = selectedLocationId
                )
                api.updateItem(id, updatedItem)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to update item: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun enrichData() {
        val id = itemId ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Save current state before enrichment
                originalValues = mapOf(
                    "description" to description,
                    "brand" to brand,
                    "modelNumber" to modelNumber,
                    "serialNumber" to serialNumber,
                    "estimatedValue" to estimatedValue
                )

                val result = api.enrichItem(id)
                val enriched = result.enriched_data.firstOrNull()
                if (enriched != null) {
                    // Update local state with enriched data
                    description = enriched.description ?: description
                    brand = enriched.brand ?: brand
                    modelNumber = enriched.model_number ?: modelNumber
                    serialNumber = enriched.serial_number ?: serialNumber
                    estimatedValue = enriched.estimated_value ?: estimatedValue
                    
                    isReviewingEnrichment = true
                } else {
                    errorMessage = "No enriched data found: ${result.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to enrich data: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleMaintenanceTask(task: com.tokendad.nesventorynew.data.remote.MaintenanceTask) {
        viewModelScope.launch {
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val update = com.tokendad.nesventorynew.data.remote.MaintenanceTaskUpdate(
                    completed = !task.completed,
                    completed_date = if (!task.completed) currentDate else null
                )
                api.updateMaintenanceTask(task.id, update)
                itemId?.let { fetchMaintenanceTasks(it) }
            } catch (e: Exception) {
                errorMessage = "Failed to update task: ${e.localizedMessage}"
            }
        }
    }

    fun deletePhoto(photoId: UUID) {
        val id = itemId ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                api.deleteItemPhoto(id, photoId)
                fetchItem(id)
            } catch (e: Exception) {
                errorMessage = "Failed to delete photo: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}