package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.CsvImportResult
import com.tokendad.nesventory.data.remote.EncirclePreviewResult
import com.tokendad.nesventory.data.remote.NetworkScanResult
import okhttp3.MultipartBody

interface ImportRepository {
    suspend fun importCsv(file: MultipartBody.Part): CsvImportResult
    suspend fun previewEncircleImport(file: MultipartBody.Part): EncirclePreviewResult
    suspend fun importEncircle(file: MultipartBody.Part): CsvImportResult
    suspend fun scanNetwork(): NetworkScanResult
    suspend fun importNetworkItems(itemIds: List<String>): CsvImportResult
}
