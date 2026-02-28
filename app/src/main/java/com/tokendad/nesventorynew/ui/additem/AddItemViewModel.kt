package com.tokendad.nesventorynew.ui.additem

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.DetectedItem
import com.tokendad.nesventorynew.data.remote.ItemCreate
import com.tokendad.nesventorynew.data.remote.Location
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val api: NesVentoryApi
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
    
    // Barcode Lookup
    var barcodeInput by mutableStateOf("")
    var showBarcodeDialog by mutableStateOf(false)

    var availableLocations by mutableStateOf<List<Location>>(emptyList())
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    private var imageBytes: ByteArray? = null

    // AI Detection states
    var detectedItems by mutableStateOf<List<DetectedItem>>(emptyList())
    var currentDetectionIndex by mutableStateOf(0)
    var showDetectionResults by mutableStateOf(false)
    var showRetryOption by mutableStateOf(false)

    val currentDetectedItem get() = detectedItems.getOrNull(currentDetectionIndex)

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                availableLocations = api.getLocations()
            } catch (e: Exception) {
                android.util.Log.w("AddItemViewModel", "Failed to fetch locations", e)
            }
        }
    }
    
    fun lookupBarcode() {
        if (barcodeInput.isBlank()) return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            showBarcodeDialog = false
            try {
                val request = com.tokendad.nesventorynew.data.remote.BarcodeLookupRequest(barcodeInput)
                val result = api.lookupBarcode(request)
                
                if (result.found) {
                    name = result.name ?: name
                    description = result.description ?: description
                    brand = result.brand ?: brand
                    modelNumber = result.model_number ?: modelNumber
                    estimatedValue = result.estimated_value?.toString() ?: estimatedValue
                } else {
                    errorMessage = "Barcode not found."
                }
            } catch (e: Exception) {
                errorMessage = "Barcode lookup failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun acceptDetection() {
        currentDetectedItem?.let { item ->
            name = item.name
            description = item.description ?: description
            brand = item.brand ?: brand
            estimatedValue = item.estimated_value?.toString() ?: estimatedValue
        }
        showDetectionResults = false
        detectedItems = emptyList()
        currentDetectionIndex = 0
    }

    fun rejectDetection() {
        if (currentDetectionIndex < detectedItems.size - 1) {
            currentDetectionIndex++
        } else {
            // No more items, go back to manual entry
            showDetectionResults = false
            detectedItems = emptyList()
            currentDetectionIndex = 0
        }
    }

    private fun performAnalysis(bytes: ByteArray, usePlugins: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
                showRetryOption = false
            }
            
            try {
                val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)
                val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
                
                val result = api.detectItems(body, usePlugins)
                
                withContext(Dispatchers.Main) {
                    if (result.items.isNotEmpty()) {
                        detectedItems = result.items
                        currentDetectionIndex = 0
                        showDetectionResults = true
                        showRetryOption = false
                    } else {
                        errorMessage = "No items detected in the image."
                        if (usePlugins) {
                            showRetryOption = true
                        }
                    }
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) {
                    errorMessage = "Failed to analyze image: ${e.localizedMessage}"
                 }
            } finally {
                 withContext(Dispatchers.Main) {
                    isLoading = false
                 }
            }
        }
    }

    fun retryWithStandardAi() {
        imageBytes?.let {
            performAnalysis(it, usePlugins = false)
        }
    }

    fun analyzeImage(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    imageBytes = bytes
                    performAnalysis(bytes)
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) {
                    errorMessage = "Failed to read image: ${e.localizedMessage}"
                 }
            }
        }
    }

    fun analyzeBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                
                if (bytes.isNotEmpty()) {
                    imageBytes = bytes
                    performAnalysis(bytes)
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) {
                    errorMessage = "Failed to process bitmap: ${e.localizedMessage}"
                 }
            }
        }
    }

    fun scanBarcodeFromImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
                showBarcodeDialog = false // Close dialog if open
            }
            
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                
                if (bytes.isNotEmpty()) {
                    val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)
                    val body = MultipartBody.Part.createFormData("file", "barcode_scan.jpg", requestFile)
                    
                    val scanResult = api.scanBarcode(body)
                    
                    withContext(Dispatchers.Main) {
                        if (scanResult.found && !scanResult.upc.isNullOrBlank()) {
                            barcodeInput = scanResult.upc
                            // Auto-lookup after scan
                            lookupBarcode()
                        } else {
                            errorMessage = "No barcode found in image."
                        }
                    }
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) {
                    errorMessage = "Failed to scan barcode: ${e.localizedMessage}"
                 }
            } finally {
                 withContext(Dispatchers.Main) {
                    isLoading = false
                 }
            }
        }
    }

    fun createItem(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            errorMessage = "Name is required"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val newItemRequest = ItemCreate(
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
                val createdItem = api.createItem(newItemRequest)
                
                // Upload photo if available
                imageBytes?.let { bytes ->
                    try {
                        val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)
                        val body = MultipartBody.Part.createFormData("file", "item_photo.jpg", requestFile)
                        api.uploadItemPhoto(createdItem.id, body, isPrimary = true)
                    } catch (e: Exception) {
                        android.util.Log.w("AddItemViewModel", "Photo upload failed after item creation", e)
                    }
                }
                
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to create item: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
