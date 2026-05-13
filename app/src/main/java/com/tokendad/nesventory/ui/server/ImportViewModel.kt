package com.tokendad.nesventory.ui.server

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.CsvImportResult
import com.tokendad.nesventory.data.remote.EncirclePreviewResult
import com.tokendad.nesventory.data.remote.NetworkDiscoveredItem
import com.tokendad.nesventory.data.repository.ImportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val importRepository: ImportRepository
) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var csvResult by mutableStateOf<CsvImportResult?>(null)
    var encirclePreview by mutableStateOf<EncirclePreviewResult?>(null)
    var networkItems by mutableStateOf<List<NetworkDiscoveredItem>>(emptyList())
    var selectedNetworkItemIds by mutableStateOf<Set<String>>(emptySet())

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun importCsv(contentResolver: ContentResolver, uri: Uri) {
        uploadFile(
            contentResolver = contentResolver,
            uri = uri,
            defaultFileName = "import.csv",
            action = { filePart -> importRepository.importCsv(filePart) },
            onResult = { result ->
                csvResult = result
                successMessage = "CSV import completed: ${result.imported_count} imported, ${result.failed_count} failed"
            },
            onErrorPrefix = "CSV import failed"
        )
    }

    fun previewEncircle(contentResolver: ContentResolver, uri: Uri) {
        uploadFile(
            contentResolver = contentResolver,
            uri = uri,
            defaultFileName = "encircle.csv",
            action = { filePart -> importRepository.previewEncircleImport(filePart) },
            onResult = { preview ->
                encirclePreview = preview
                successMessage = "Preview loaded: ${preview.item_count} items, ${preview.location_count} locations"
            },
            onErrorPrefix = "Encircle preview failed"
        )
    }

    fun importEncircle(contentResolver: ContentResolver, uri: Uri) {
        uploadFile(
            contentResolver = contentResolver,
            uri = uri,
            defaultFileName = "encircle.csv",
            action = { filePart -> importRepository.importEncircle(filePart) },
            onResult = { result ->
                csvResult = result
                successMessage = "Encircle import completed: ${result.imported_count} imported, ${result.failed_count} failed"
            },
            onErrorPrefix = "Encircle import failed"
        )
    }

    fun scanNetwork() {
        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                val result = importRepository.scanNetwork()
                networkItems = result.discovered_items
                selectedNetworkItemIds = emptySet()
                successMessage = "Network scan completed in ${result.scan_duration_ms}ms"
            } catch (e: Exception) {
                errorMessage = "Network scan failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleNetworkSelection(itemId: String) {
        selectedNetworkItemIds = if (selectedNetworkItemIds.contains(itemId)) {
            selectedNetworkItemIds - itemId
        } else {
            selectedNetworkItemIds + itemId
        }
    }

    fun importSelectedNetworkItems() {
        if (selectedNetworkItemIds.isEmpty()) {
            errorMessage = "Select at least one discovered item"
            return
        }
        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                val result = importRepository.importNetworkItems(selectedNetworkItemIds.toList())
                csvResult = result
                successMessage = "Network import completed: ${result.imported_count} imported, ${result.failed_count} failed"
            } catch (e: Exception) {
                errorMessage = "Network import failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun <T> uploadFile(
        contentResolver: ContentResolver,
        uri: Uri,
        defaultFileName: String,
        action: suspend (MultipartBody.Part) -> T,
        onResult: (T) -> Unit,
        onErrorPrefix: String
    ) {
        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("Could not read selected file")
                val requestFile = bytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", defaultFileName, requestFile)
                val result = action(filePart)
                onResult(result)
            } catch (e: Exception) {
                errorMessage = "$onErrorPrefix: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
