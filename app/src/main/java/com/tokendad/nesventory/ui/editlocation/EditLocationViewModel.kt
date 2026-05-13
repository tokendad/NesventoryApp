package com.tokendad.nesventory.ui.editlocation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.repository.LocationRepository
import com.tokendad.nesventory.util.RoomCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditLocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var friendlyName by mutableStateOf("")
    var address by mutableStateOf("")
    var estimatedPropertyValue by mutableStateOf("")
    var selectedParentId by mutableStateOf<UUID?>(null)
    var isPrimaryLocation by mutableStateOf(false)
    var isContainer by mutableStateOf(false)
    var locationCategory by mutableStateOf<String?>(null)

    // Insurance fields
    var companyName by mutableStateOf("")
    var companyAddress by mutableStateOf("")
    var companyEmail by mutableStateOf("")
    var companyPhone by mutableStateOf("")
    var agentName by mutableStateOf("")
    var policyNumber by mutableStateOf("")
    var primaryHolderName by mutableStateOf("")
    var primaryHolderPhone by mutableStateOf("")
    var primaryHolderEmail by mutableStateOf("")
    var primaryHolderAddress by mutableStateOf("")
    var insurancePurchaseDate by mutableStateOf("")
    var insurancePurchasePrice by mutableStateOf("")
    var insuranceBuildDate by mutableStateOf("")

    var availableLocations by mutableStateOf<List<Location>>(emptyList())
    var locationCategories by mutableStateOf<List<String>>(RoomCategories.defaultCategories)
    var locationId: UUID? = null

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        val idString: String? = savedStateHandle["locationId"]
        if (idString != null) {
            locationId = UUID.fromString(idString)
            fetchLocation(locationId!!)
        }
        fetchLocations()
        fetchLocationCategories()
    }

    private fun fetchLocationCategories() {
        viewModelScope.launch {
            try {
                locationCategories = locationRepository.getLocationCategories()
            } catch (e: Exception) {
                android.util.Log.w("EditLocationViewModel", "Failed to fetch location categories, using defaults", e)
            }
        }
    }

    private fun fetchLocation(id: UUID) {
        viewModelScope.launch {
            isLoading = true
            try {
                val loc = locationRepository.getLocation(id)
                name = loc.name
                description = loc.description ?: ""
                friendlyName = loc.friendly_name ?: ""
                address = loc.address ?: ""
                estimatedPropertyValue = loc.estimated_property_value ?: ""
                selectedParentId = loc.parent_id
                isPrimaryLocation = loc.is_primary_location
                isContainer = loc.is_container
                locationCategory = loc.location_category

                // Load insurance info
                loc.insurance_info?.let { info ->
                    companyName = info.company_name ?: ""
                    companyAddress = info.company_address ?: ""
                    companyEmail = info.company_email ?: ""
                    companyPhone = info.company_phone ?: ""
                    agentName = info.agent_name ?: ""
                    policyNumber = info.policy_number ?: ""
                    info.primary_holder?.let { holder ->
                        primaryHolderName = holder.name ?: ""
                        primaryHolderPhone = holder.phone ?: ""
                        primaryHolderEmail = holder.email ?: ""
                        primaryHolderAddress = holder.address ?: ""
                    }
                    insurancePurchaseDate = info.purchase_date ?: ""
                    insurancePurchasePrice = info.purchase_price?.toString() ?: ""
                    insuranceBuildDate = info.build_date ?: ""
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load location: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                availableLocations = locationRepository.getLocations()
            } catch (e: Exception) {
                android.util.Log.w("EditLocationViewModel", "Failed to fetch locations", e)
            }
        }
    }

    fun updateLocation(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            errorMessage = "Name is required"
            return
        }

        val id = locationId ?: return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val updatedLocation = com.tokendad.nesventory.data.remote.LocationUpdate(
                    name = name,
                    description = description.ifBlank { null },
                    friendly_name = friendlyName.ifBlank { null },
                    address = address.ifBlank { null },
                    parent_id = selectedParentId,
                    is_primary_location = isPrimaryLocation,
                    is_container = isContainer,
                    estimated_property_value = estimatedPropertyValue.ifBlank { null },
                    location_category = locationCategory,
                    insurance_info = com.tokendad.nesventory.data.remote.InsuranceInfo(
                        company_name = companyName.ifBlank { null },
                        company_address = companyAddress.ifBlank { null },
                        company_email = companyEmail.ifBlank { null },
                        company_phone = companyPhone.ifBlank { null },
                        agent_name = agentName.ifBlank { null },
                        policy_number = policyNumber.ifBlank { null },
                        primary_holder = com.tokendad.nesventory.data.remote.PolicyHolder(
                            name = primaryHolderName.ifBlank { null },
                            phone = primaryHolderPhone.ifBlank { null },
                            email = primaryHolderEmail.ifBlank { null },
                            address = primaryHolderAddress.ifBlank { null }
                        ),
                        purchase_date = insurancePurchaseDate.ifBlank { null },
                        purchase_price = insurancePurchasePrice.toDoubleOrNull(),
                        build_date = insuranceBuildDate.ifBlank { null }
                    )
                )
                locationRepository.updateLocation(id, updatedLocation)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to update location: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
