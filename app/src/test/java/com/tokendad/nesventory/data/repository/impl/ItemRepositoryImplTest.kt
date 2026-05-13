package com.tokendad.nesventory.data.repository.impl

import com.google.gson.Gson
import com.tokendad.nesventory.data.local.ItemDao
import com.tokendad.nesventory.data.local.SyncQueueDao
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.ServerProfileList
import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.BulkDeleteRequest
import com.tokendad.nesventory.data.remote.BulkOperationResponse
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.UUID

class ItemRepositoryImplTest {
    private val api = mockk<NesVentoryApi>(relaxed = true)
    private val itemDao = mockk<ItemDao>(relaxed = true)
    private val syncQueueDao = mockk<SyncQueueDao>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>(relaxed = true)
    private val repository = ItemRepositoryImpl(api, itemDao, syncQueueDao, preferencesManager, Gson())

    init {
        every { preferencesManager.serverProfiles } returns flowOf(ServerProfileList())
    }

    @Test
    fun `getItems delegates with query params`() = runTest {
        val expected = listOf(
            Item(
                id = UUID.randomUUID(),
                name = "Lamp",
                created_at = "2024-01-01T00:00:00Z",
                updated_at = "2024-01-01T00:00:00Z"
            )
        )
        val locationId = UUID.randomUUID()

        coEvery {
            itemDao.getAll()
        } returns emptyList()
        coEvery {
            api.getItems(
                search = "lamp",
                locationId = locationId,
                isLiving = false,
                relationshipType = null,
                collectionId = null,
                collectionIdRecursive = null,
                page = null,
                limit = null
            )
        } returns expected

        val actual = repository.getItems(
            search = "lamp",
            locationId = locationId,
            isLiving = false
        )

        assertEquals(expected, actual)
        coVerify(exactly = 1) {
            api.getItems(
                search = "lamp",
                locationId = locationId,
                isLiving = false,
                relationshipType = null,
                collectionId = null,
                collectionIdRecursive = null,
                page = null,
                limit = null
            )
        }
    }

    @Test
    fun `getItem propagates api exceptions`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.getItem(id) } throws IllegalStateException("boom")
        coEvery { itemDao.getById(id.toString()) } returns null

        try {
            repository.getItem(id)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }
    }

    @Test
    fun `bulkDeleteItems delegates to api`() = runTest {
        val request = BulkDeleteRequest(item_ids = listOf(UUID.randomUUID(), UUID.randomUUID()))
        val expected = BulkOperationResponse(success_count = 2, failed_count = 0, errors = emptyList())
        coEvery { api.bulkDeleteItems(request) } returns expected

        val actual = repository.bulkDeleteItems(request)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.bulkDeleteItems(request) }
    }

    @Test
    fun `deleteItem queues pending delete when api fails`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.deleteItem(id) } throws IOException("offline")

        repository.deleteItem(id)

        coVerify(exactly = 1) { syncQueueDao.insert(any()) }
        coVerify(exactly = 1) { itemDao.deleteById(id.toString()) }
    }

    @Test
    fun `deleteItem propagates non-transient api exceptions`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.deleteItem(id) } throws IllegalStateException("boom")

        try {
            repository.deleteItem(id)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }

        coVerify(exactly = 0) { syncQueueDao.insert(any()) }
    }
}
