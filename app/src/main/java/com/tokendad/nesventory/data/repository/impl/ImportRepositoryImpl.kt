package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.CsvImportResult
import com.tokendad.nesventory.data.remote.EncirclePreviewResult
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.NetworkScanResult
import com.tokendad.nesventory.data.repository.ImportRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class ImportRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : ImportRepository {
    override suspend fun importCsv(file: MultipartBody.Part): CsvImportResult = api.importCsv(file)

    override suspend fun previewEncircleImport(file: MultipartBody.Part): EncirclePreviewResult =
        api.previewEncircleImport(file)

    override suspend fun importEncircle(file: MultipartBody.Part): CsvImportResult = api.importEncircle(file)

    override suspend fun scanNetwork(): NetworkScanResult = api.scanNetwork()

    override suspend fun importNetworkItems(itemIds: List<String>): CsvImportResult =
        api.importNetworkItems(itemIds)
}
