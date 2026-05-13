package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.BarcodeLookupRequest
import com.tokendad.nesventory.data.remote.BarcodeLookupResult
import com.tokendad.nesventory.data.remote.BarcodeScanResult
import com.tokendad.nesventory.data.remote.DataTagInfo
import com.tokendad.nesventory.data.remote.DetectionResult
import com.tokendad.nesventory.data.remote.Document
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.ItemCreate
import com.tokendad.nesventory.data.remote.ItemEnrichmentResult
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.Photo
import com.tokendad.nesventory.data.repository.ItemRepository
import okhttp3.MultipartBody
import java.util.UUID
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : ItemRepository {

    override suspend fun getItems(): List<Item> = api.getItems()

    override suspend fun getItem(id: UUID): Item = api.getItem(id)

    override suspend fun createItem(item: ItemCreate): Item = api.createItem(item)

    override suspend fun updateItem(id: UUID, item: ItemCreate): Item = api.updateItem(id, item)

    override suspend fun deleteItem(id: UUID) = api.deleteItem(id)

    override suspend fun enrichItem(id: UUID): ItemEnrichmentResult = api.enrichItem(id)

    override suspend fun detectItems(file: MultipartBody.Part, usePlugins: Boolean): DetectionResult =
        api.detectItems(file, usePlugins)

    override suspend fun scanBarcode(file: MultipartBody.Part): BarcodeScanResult = api.scanBarcode(file)

    override suspend fun lookupBarcode(request: BarcodeLookupRequest): BarcodeLookupResult =
        api.lookupBarcode(request)

    override suspend fun parseDataTag(file: MultipartBody.Part, usePlugins: Boolean): DataTagInfo =
        api.parseDataTag(file, usePlugins)

    override suspend fun uploadItemPhoto(
        itemId: UUID,
        file: MultipartBody.Part,
        isPrimary: Boolean,
        isDataTag: Boolean,
        photoType: String?
    ): Photo = api.uploadItemPhoto(itemId, file, isPrimary, isDataTag, photoType)

    override suspend fun deleteItemPhoto(itemId: UUID, photoId: UUID) =
        api.deleteItemPhoto(itemId, photoId)

    override suspend fun uploadItemDocument(
        itemId: UUID,
        file: MultipartBody.Part,
        documentType: String?
    ): Document = api.uploadItemDocument(itemId, file, documentType)

    override suspend fun deleteItemDocument(itemId: UUID, documentId: UUID) =
        api.deleteItemDocument(itemId, documentId)
}
