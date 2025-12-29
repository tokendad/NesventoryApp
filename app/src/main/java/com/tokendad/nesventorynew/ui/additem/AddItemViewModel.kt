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
    
    var availableLocations by mutableStateOf<List<Location>>(emptyList())
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    private var imageBytes: ByteArray? = null

    // AI Detection states
    var detectedItems by mutableStateOf<List<DetectedItem>>(emptyList())
    var currentDetectionIndex by mutableStateOf(0)
    var showDetectionResults by mutableStateOf(false)

    val currentDetectedItem get() = detectedItems.getOrNull(currentDetectionIndex)

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                availableLocations = api.getLocations()
            } catch (_: Exception) {
                // Fail silently for dropdown, or maybe show error
            }
        }
    }

    fun acceptDetection() {
        currentDetectedItem?.let { item ->
            name = item.name
            description = item.description ?: ""
            brand = item.brand ?: ""
            estimatedValue = item.estimated_value?.toString() ?: ""
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

    fun analyzeImage(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            // Update loading state on Main thread
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    imageBytes = bytes // Store bytes for later upload
                    val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, bytes.size)
                    val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
                    
                    val result = api.detectItems(body)
                    
                    withContext(Dispatchers.Main) {
                        if (result.items.isNotEmpty()) {
                            detectedItems = result.items
                            currentDetectionIndex = 0
                            showDetectionResults = true
                        } else {
                            errorMessage = "No items detected in the image."
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

    fun analyzeBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                
                if (bytes.isNotEmpty()) {
                    imageBytes = bytes // Store bytes for later upload
                    val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)
                    val body = MultipartBody.Part.createFormData("file", "camera_capture.jpg", requestFile)
                    
                    val result = api.detectItems(body)
                    
                    withContext(Dispatchers.Main) {
                        if (result.items.isNotEmpty()) {
                            detectedItems = result.items
                            currentDetectionIndex = 0
                            showDetectionResults = true
                        } else {
                            errorMessage = "No items detected in the image."
                        }
                    }
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) {
                    errorMessage = "Failed to analyze camera image: ${e.localizedMessage}"
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
                        // Log or handle photo upload failure, but don't fail the item creation
                        // maybe show a toast? For now, we just proceed.
                        e.printStackTrace()
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
