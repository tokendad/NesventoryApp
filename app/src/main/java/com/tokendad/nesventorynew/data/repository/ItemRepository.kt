package com.tokendad.nesventorynew.data.repository

import com.tokendad.nesventorynew.data.remote.BarcodeLookupRequest
import com.tokendad.nesventorynew.data.remote.BarcodeLookupResult
import com.tokendad.nesventorynew.data.remote.BarcodeScanResult
import com.tokendad.nesventorynew.data.remote.DataTagInfo
import com.tokendad.nesventorynew.data.remote.DetectionResult
import com.tokendad.nesventorynew.data.remote.Document
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.remote.ItemCreate
import com.tokendad.nesventorynew.data.remote.ItemEnrichmentResult
import com.tokendad.nesventorynew.data.remote.Photo
import okhttp3.MultipartBody
import java.util.UUID

@Suppress("unused")
interface ItemRepository {
    suspend fun getItems(): List<Item>
    suspend fun getItem(id: UUID): Item
    suspend fun createItem(item: ItemCreate): Item
    suspend fun updateItem(id: UUID, item: ItemCreate): Item
    suspend fun deleteItem(id: UUID)
    suspend fun enrichItem(id: UUID): ItemEnrichmentResult
    suspend fun detectItems(file: MultipartBody.Part, usePlugins: Boolean = true): DetectionResult
    suspend fun scanBarcode(file: MultipartBody.Part): BarcodeScanResult
    suspend fun lookupBarcode(request: BarcodeLookupRequest): BarcodeLookupResult
    suspend fun parseDataTag(file: MultipartBody.Part, usePlugins: Boolean = true): DataTagInfo
    suspend fun uploadItemPhoto(itemId: UUID, file: MultipartBody.Part, isPrimary: Boolean = false, isDataTag: Boolean = false, photoType: String? = null): Photo
    suspend fun deleteItemPhoto(itemId: UUID, photoId: UUID)
    suspend fun uploadItemDocument(itemId: UUID, file: MultipartBody.Part, documentType: String? = null): Document
    suspend fun deleteItemDocument(itemId: UUID, documentId: UUID)
}
