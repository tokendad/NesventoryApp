package com.tokendad.nesventorynew.data.repository

import com.tokendad.nesventorynew.data.remote.PrintLabelResponse
import com.tokendad.nesventorynew.data.remote.PrintJobRequest
import com.tokendad.nesventorynew.data.remote.PrinterConfig
import com.tokendad.nesventorynew.data.remote.PrinterModelsResponse
import com.tokendad.nesventorynew.data.remote.PrinterProfile
import com.tokendad.nesventorynew.data.remote.PrinterProfilesResponse
import com.tokendad.nesventorynew.data.remote.PrinterStatus
import com.tokendad.nesventorynew.data.remote.PrinterTestResult
import com.tokendad.nesventorynew.data.remote.StatusResponse
import com.tokendad.nesventorynew.data.remote.SystemPrintersResponse

interface PrinterRepository {
    suspend fun getPrinterConfig(): PrinterConfig
    suspend fun updatePrinterConfig(config: PrinterConfig): PrinterConfig
    suspend fun getPrinterModels(): PrinterModelsResponse
    suspend fun printLabel(request: PrintJobRequest): PrintLabelResponse
    suspend fun getPrinterStatus(): PrinterStatus
    suspend fun testPrinterConnection(config: PrinterConfig): PrinterTestResult
    suspend fun printTestLabel(): PrintLabelResponse
    suspend fun getPrinterProfiles(): PrinterProfilesResponse
    suspend fun createPrinterProfile(profile: PrinterProfile): PrinterProfile
    suspend fun deletePrinterProfile(profileId: String)
    suspend fun getActivePrinterConfig(): PrinterConfig
    suspend fun activatePrinterProfile(profileId: String): PrinterConfig
    suspend fun getSystemPrinters(): SystemPrintersResponse
    suspend fun setDefaultSystemPrinter(request: Map<String, String>): StatusResponse
}
