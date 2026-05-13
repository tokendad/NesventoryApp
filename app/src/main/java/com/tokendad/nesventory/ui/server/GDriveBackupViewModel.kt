package com.tokendad.nesventory.ui.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.remote.GDriveBackup
import com.tokendad.nesventory.data.remote.GDriveStatus
import com.tokendad.nesventory.data.repository.GDriveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GDriveBackupViewModel @Inject constructor(
    private val gDriveRepository: GDriveRepository
) : ViewModel() {
    var status by mutableStateOf<GDriveStatus?>(null)
    var backups by mutableStateOf<List<GDriveBackup>>(emptyList())
    var isLoading by mutableStateOf(false)
    var isConnecting by mutableStateOf(false)
    var isDisconnecting by mutableStateOf(false)
    var isBackingUp by mutableStateOf(false)
    var deletingBackupId by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    init {
        loadData()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val loadedStatus = gDriveRepository.getStatus()
                val loadedBackups = gDriveRepository.listBackups()
                status = loadedStatus
                backups = loadedBackups
            } catch (e: Exception) {
                errorMessage = "Failed to load Google Drive backup data: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun connect(onAuthUrlReady: (String) -> Unit) {
        viewModelScope.launch {
            isConnecting = true
            clearMessages()
            try {
                val response = gDriveRepository.connect()
                onAuthUrlReady(response.auth_url)
                successMessage = "Complete authorization in your browser, then return to refresh."
            } catch (e: Exception) {
                errorMessage = "Failed to start Google Drive connection: ${e.localizedMessage}"
            } finally {
                isConnecting = false
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            isDisconnecting = true
            clearMessages()
            try {
                gDriveRepository.disconnect()
                successMessage = "Google Drive disconnected"
                loadData()
            } catch (e: Exception) {
                errorMessage = "Failed to disconnect Google Drive: ${e.localizedMessage}"
            } finally {
                isDisconnecting = false
            }
        }
    }

    fun backupNow() {
        if (status?.connected != true) {
            errorMessage = "Connect Google Drive before running a backup"
            return
        }
        viewModelScope.launch {
            isBackingUp = true
            clearMessages()
            try {
                val result = gDriveRepository.triggerBackup()
                successMessage = result.message ?: if (result.success) "Backup completed" else "Backup request failed"
                loadData()
            } catch (e: Exception) {
                errorMessage = "Failed to create backup: ${e.localizedMessage}"
            } finally {
                isBackingUp = false
            }
        }
    }

    fun deleteBackup(backupId: String) {
        viewModelScope.launch {
            deletingBackupId = backupId
            clearMessages()
            try {
                gDriveRepository.deleteBackup(backupId)
                successMessage = "Backup deleted"
                backups = backups.filterNot { it.id == backupId }
            } catch (e: Exception) {
                errorMessage = "Failed to delete backup: ${e.localizedMessage}"
            } finally {
                deletingBackupId = null
            }
        }
    }
}
