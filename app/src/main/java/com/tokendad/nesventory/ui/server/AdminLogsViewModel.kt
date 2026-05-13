package com.tokendad.nesventory.ui.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventory.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminLogsViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : ViewModel() {
    var logFiles by mutableStateOf<List<String>>(emptyList())
    var selectedLogFile by mutableStateOf<String?>(null)
    var logLines by mutableStateOf<List<String>>(emptyList())
    var isLoading by mutableStateOf(false)
    var isReadingLog by mutableStateOf(false)
    var isMutatingLogs by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    init {
        loadLogFiles()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun loadLogFiles(clearExistingMessages: Boolean = true) {
        viewModelScope.launch {
            isLoading = true
            if (clearExistingMessages) {
                clearMessages()
            }
            try {
                logFiles = systemRepository.listLogFiles()
            } catch (e: Exception) {
                errorMessage = "Failed to list logs: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun openLog(filename: String) {
        viewModelScope.launch {
            isReadingLog = true
            clearMessages()
            try {
                selectedLogFile = filename
                val lines = systemRepository.readLogFile(filename).use { body ->
                    val content = body.string()
                    content.lineSequence().take(MAX_LOG_LINES).toList()
                }
                logLines = lines
            } catch (e: Exception) {
                errorMessage = "Failed to read log file: ${e.localizedMessage}"
                logLines = emptyList()
            } finally {
                isReadingLog = false
            }
        }
    }

    fun rotateLogs() {
        mutateLogs(
            action = { systemRepository.rotateLogs() },
            success = "Logs rotated successfully"
        )
    }

    fun cleanupLogs() {
        mutateLogs(
            action = { systemRepository.cleanupLogs() },
            success = "Old logs cleaned up"
        )
    }

    private fun mutateLogs(
        action: suspend () -> Unit,
        success: String
    ) {
        viewModelScope.launch {
            isMutatingLogs = true
            clearMessages()
            try {
                action()
                successMessage = success
                loadLogFiles(clearExistingMessages = false)
            } catch (e: Exception) {
                errorMessage = "Log operation failed: ${e.localizedMessage}"
            } finally {
                isMutatingLogs = false
            }
        }
    }

    companion object {
        private const val MAX_LOG_LINES = 5000
    }
}
