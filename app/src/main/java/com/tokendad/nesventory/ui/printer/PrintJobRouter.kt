package com.tokendad.nesventory.ui.printer

import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.PrintJobRequest
import com.tokendad.nesventory.data.repository.PrinterRepository
import com.tokendad.nesventory.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface PrintResult {
    data class Success(val message: String) : PrintResult
    data class Error(val message: String) : PrintResult
}

class PrintJobRouter @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val printerRepository: PrinterRepository,
    private val printJobExecutor: PrintJobExecutor
) {
    suspend fun printItem(item: Item): PrintResult {
        val settings = loadSettings()
        return if (settings.printMethod == "server") {
            printOnServer(entityId = item.id, entityType = "item")
        } else {
            printLocally(
                labelText = item.name,
                labelSubtitle = item.id.toString().take(8),
                qrContent = "${settings.serverUrl}/api/items/${item.id}",
                iconType = "box",
                settings = settings
            )
        }
    }

    suspend fun printLocation(location: Location): PrintResult {
        val settings = loadSettings()
        return if (settings.printMethod == "server") {
            printOnServer(entityId = location.id, entityType = "location")
        } else {
            printLocally(
                labelText = location.name,
                labelSubtitle = location.id.toString().take(8),
                qrContent = "${settings.serverUrl}/api/locations/${location.id}",
                iconType = "location",
                settings = settings
            )
        }
    }

    private suspend fun printOnServer(
        entityId: java.util.UUID,
        entityType: String
    ): PrintResult {
        return try {
            val response = printerRepository.printLabel(
                PrintJobRequest(
                    entity_id = entityId,
                    entity_type = entityType,
                    quantity = 1
                )
            )
            if (response.success) {
                PrintResult.Success(response.message ?: "Print job sent to server!")
            } else {
                PrintResult.Error(response.message ?: "Server print failed")
            }
        } catch (e: Exception) {
            PrintResult.Error("Server print failed: ${e.localizedMessage}")
        }
    }

    private suspend fun printLocally(
        labelText: String,
        labelSubtitle: String,
        qrContent: String,
        iconType: String,
        settings: PrintSettings
    ): PrintResult {
        if (!printJobExecutor.isConnected()) {
            return PrintResult.Error("Printer not connected. Go to Printer Settings.")
        }

        return withContext(Dispatchers.IO) {
            try {
                printJobExecutor.printLabel(
                    labelText = labelText,
                    labelSubtitle = labelSubtitle,
                    qrContent = qrContent,
                    iconType = iconType,
                    model = settings.localModel,
                    density = settings.localDensity
                )
                PrintResult.Success("Label printed successfully!")
            } catch (e: Exception) {
                PrintResult.Error("Print failed: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun loadSettings(): PrintSettings {
        val settings = preferencesManager.serverSettings.first()
        val normalizedUrl = settings.remoteUrl
            .trim()
            .ifBlank { Constants.DEFAULT_REMOTE_URL }
            .trimEnd('/')

        return PrintSettings(
            printMethod = if (settings.printMethod == "server") "server" else "local",
            serverUrl = normalizedUrl,
            localModel = PrinterModel.fromString(settings.localPrinterModel) ?: PrinterModel.D11_H,
            localDensity = settings.localPrinterDensity.coerceIn(1, 5)
        )
    }
}

private data class PrintSettings(
    val printMethod: String,
    val serverUrl: String,
    val localModel: PrinterModel,
    val localDensity: Int
)
