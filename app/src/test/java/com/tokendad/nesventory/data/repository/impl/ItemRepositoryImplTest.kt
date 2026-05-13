package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.Item
import com.tokendad.nesventory.data.remote.BulkDeleteRequest
import com.tokendad.nesventory.data.remote.BulkOperationResponse
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class ItemRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = ItemRepositoryImpl(api)

    @Test
    fun `getItems delegates with query params`() = runTest {
        val expected = listOf(mockk<Item>(relaxed = true))
        val locationId = UUID.randomUUID()

        coEvery {
            api.getItems(
                search = "lamp",
                locationId = locationId,
                isLiving = false,
                relationshipType = null,
                collectionId = null,
                collectionIdRecursive = null
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
                collectionIdRecursive = null
            )
        }
    }

    @Test
    fun `getItem propagates api exceptions`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.getItem(id) } throws IllegalStateException("boom")

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
}
