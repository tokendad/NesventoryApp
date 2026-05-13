package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.PrintLabelResponse
import com.tokendad.nesventory.data.remote.PrintJobRequest
import com.tokendad.nesventory.data.remote.PrinterConfig
import com.tokendad.nesventory.data.remote.PrinterModelsResponse
import com.tokendad.nesventory.data.remote.PrinterProfile
import com.tokendad.nesventory.data.remote.PrinterProfilesResponse
import com.tokendad.nesventory.data.remote.PrinterStatus
import com.tokendad.nesventory.data.remote.PrinterTestResult
import com.tokendad.nesventory.data.remote.StatusResponse
import com.tokendad.nesventory.data.remote.SystemPrintersResponse
import com.tokendad.nesventory.data.repository.PrinterRepository
import javax.inject.Inject

class PrinterRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : PrinterRepository {

    override suspend fun getPrinterConfig(): PrinterConfig = api.getPrinterConfig()

    override suspend fun updatePrinterConfig(config: PrinterConfig): PrinterConfig =
        api.updatePrinterConfig(config)

    override suspend fun getPrinterModels(): PrinterModelsResponse = api.getPrinterModels()

    override suspend fun printLabel(request: PrintJobRequest): PrintLabelResponse = api.printLabel(request)

    override suspend fun getPrinterStatus(): PrinterStatus = api.getPrinterStatus()

    override suspend fun testPrinterConnection(config: PrinterConfig): PrinterTestResult =
        api.testPrinterConnection(config)

    override suspend fun printTestLabel(): PrintLabelResponse = api.printTestLabel()

    override suspend fun getPrinterProfiles(): PrinterProfilesResponse = api.getPrinterProfiles()

    override suspend fun createPrinterProfile(profile: PrinterProfile): PrinterProfile =
        api.createPrinterProfile(profile)

    override suspend fun deletePrinterProfile(profileId: String) = api.deletePrinterProfile(profileId)

    override suspend fun getActivePrinterConfig(): PrinterConfig = api.getActivePrinterConfig()

    override suspend fun activatePrinterProfile(profileId: String): PrinterConfig =
        api.activatePrinterProfile(profileId)

    override suspend fun getSystemPrinters(): SystemPrintersResponse = api.getSystemPrinters()

    override suspend fun setDefaultSystemPrinter(request: Map<String, String>): StatusResponse =
        api.setDefaultSystemPrinter(request)
}
