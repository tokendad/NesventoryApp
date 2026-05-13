package com.tokendad.nesventory.ui.edititem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.remote.ContactInfo
import com.tokendad.nesventory.data.remote.MaintenanceTask
import com.tokendad.nesventory.data.remote.ItemUpdate
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.Photo
import com.tokendad.nesventory.data.repository.ItemRepository
import com.tokendad.nesventory.data.repository.LocationRepository
import com.tokendad.nesventory.data.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val locationRepository: LocationRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val preferencesManager: PreferencesManager,
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
    var isLiving by mutableStateOf(false)
    var relationshipType by mutableStateOf("person")
    var birthdate by mutableStateOf("")
    var contactPhone by mutableStateOf("")
    var contactEmail by mutableStateOf("")
    var contactNotes by mutableStateOf("")
    val livingTypeOptions = listOf("person", "pet", "plant")
    
    var availableLocations by mutableStateOf<List<Location>>(emptyList())
    var itemId: UUID? = null

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var serverUrl by mutableStateOf("")

    private val maintenanceDelegate = ItemMaintenanceDelegate(
        maintenanceRepository = maintenanceRepository,
        scope = viewModelScope,
        onError = { errorMessage = it }
    )
    private val photoDelegate = ItemPhotoDelegate(
        itemRepository = itemRepository,
        scope = viewModelScope,
        onError = { errorMessage = it },
        onLoadingChange = { isLoading = it }
    )
    private val enrichmentDelegate = ItemEnrichmentDelegate(
        itemRepository = itemRepository,
        scope = viewModelScope,
        onError = { errorMessage = it },
        onLoadingChange = { isLoading = it }
    )

    val maintenanceTasks: List<MaintenanceTask>
        get() = maintenanceDelegate.tasks

    val itemMedia: List<Photo>
        get() = photoDelegate.itemMedia

    val isReviewingEnrichment: Boolean
        get() = enrichmentDelegate.isReviewingEnrichment

    init {
        val idString: String? = savedStateHandle["itemId"]
        if (idString != null) {
            itemId = UUID.fromString(idString)
            fetchItem(itemId!!)
        }
        fetchLocations()
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

    // ... (existing fetchItem, fetchMaintenanceTasks, fetchLocations, updateItem)

    fun isFieldModified(fieldName: String, currentValue: String): Boolean =
        enrichmentDelegate.isFieldModified(fieldName, currentValue)

    fun acceptEnrichment() = enrichmentDelegate.acceptEnrichment()

    fun discardEnrichment() {
        enrichmentDelegate.discardEnrichment { originalValues ->
            description = originalValues["description"] ?: description
            brand = originalValues["brand"] ?: brand
            modelNumber = originalValues["modelNumber"] ?: modelNumber
            serialNumber = originalValues["serialNumber"] ?: serialNumber
            estimatedValue = originalValues["estimatedValue"] ?: estimatedValue
        }
    }

    private fun fetchItem(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            try {
                val item = itemRepository.getItem(id)
                photoDelegate.replaceItemMedia(item.photos)
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
                isLiving = item.is_living
                relationshipType = item.relationship_type ?: "person"
                birthdate = item.birthdate ?: ""
                contactPhone = item.contact_info?.phone ?: ""
                contactEmail = item.contact_info?.email ?: ""
                contactNotes = item.contact_info?.notes ?: ""
            } catch (e: Exception) {
                errorMessage = "Failed to load item: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
        fetchMaintenanceTasks(id)
    }

    private fun fetchMaintenanceTasks(id: UUID) {
        maintenanceDelegate.fetchTasks(id)
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                availableLocations = locationRepository.getLocations()
                if (isLiving && selectedLocationId != null && !isHomeLocation(selectedLocationId)) {
                    selectedLocationId = homeLocations().firstOrNull()?.id
                }
            } catch (e: Exception) {
                android.util.Log.w("EditItemViewModel", "Failed to fetch locations", e)
            }
        }
    }

    fun homeLocations(): List<Location> =
        availableLocations.filter { it.location_category.equals("Home", ignoreCase = true) }

    private fun isHomeLocation(locationId: UUID?): Boolean =
        homeLocations().any { it.id == locationId }

    fun onLivingChanged(enabled: Boolean) {
        isLiving = enabled
        if (enabled) {
            if (!isHomeLocation(selectedLocationId)) {
                selectedLocationId = homeLocations().firstOrNull()?.id
            }
        } else {
            relationshipType = "person"
            birthdate = ""
            contactPhone = ""
            contactEmail = ""
            contactNotes = ""
        }
    }

    fun updateItem(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            errorMessage = "Name is required"
            return
        }
        if (isLiving && !isHomeLocation(selectedLocationId)) {
            errorMessage = "Living items must be assigned to a Home location"
            return
        }

        val id = itemId ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val updatedItem = ItemUpdate(
                    name = name,
                    description = description.ifBlank { null },
                    brand = brand.ifBlank { null },
                    model_number = modelNumber.ifBlank { null },
                    serial_number = serialNumber.ifBlank { null },
                    purchase_price = purchasePrice.ifBlank { null },
                    purchase_date = purchaseDate.ifBlank { null },
                    estimated_value = estimatedValue.ifBlank { null },
                    retailer = retailer.ifBlank { null },
                    upc = null,
                    is_living = isLiving,
                    relationship_type = relationshipType.takeIf { isLiving },
                    birthdate = birthdate.ifBlank { null }.takeIf { isLiving },
                    contact_info = ContactInfo(
                        phone = contactPhone.ifBlank { null },
                        email = contactEmail.ifBlank { null },
                        notes = contactNotes.ifBlank { null }
                    ).takeIf {
                        isLiving && (contactPhone.isNotBlank() || contactEmail.isNotBlank() || contactNotes.isNotBlank())
                    },
                    location_id = selectedLocationId
                )
                itemRepository.updateItem(id, updatedItem)
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
        errorMessage = null
        enrichmentDelegate.enrichItem(
            itemId = id,
            currentValues = mapOf(
                "description" to description,
                "brand" to brand,
                "modelNumber" to modelNumber,
                "serialNumber" to serialNumber,
                "estimatedValue" to estimatedValue
            )
        ) { enrichedDescription, enrichedBrand, enrichedModel, enrichedSerial, enrichedEstimatedValue ->
            description = enrichedDescription ?: description
            brand = enrichedBrand ?: brand
            modelNumber = enrichedModel ?: modelNumber
            serialNumber = enrichedSerial ?: serialNumber
            estimatedValue = enrichedEstimatedValue ?: estimatedValue
        }
    }

    fun toggleMaintenanceTask(task: MaintenanceTask) {
        val id = itemId ?: return
        maintenanceDelegate.toggleTask(task, id)
    }

    fun deletePhoto(photoId: UUID) {
        val id = itemId ?: return
        photoDelegate.deletePhoto(id, photoId) { fetchItem(id) }
    }
}