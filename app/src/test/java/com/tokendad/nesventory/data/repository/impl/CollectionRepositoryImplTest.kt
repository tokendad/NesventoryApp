package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.Collection
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response
import java.util.UUID

class CollectionRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = CollectionRepositoryImpl(api)

    @Test
    fun `getCollections delegates to api`() = runTest {
        val expected = listOf(mockk<Collection>(relaxed = true))
        coEvery { api.getCollections() } returns expected

        val actual = repository.getCollections()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getCollections() }
    }

    @Test
    fun `deleteCollection calls api and propagates errors`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.deleteCollection(id) } returns Response.success(Unit)

        repository.deleteCollection(id)

        coVerify(exactly = 1) { api.deleteCollection(id) }
    }

    @Test
    fun `getCollection propagates api exceptions`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.getCollection(id) } throws IllegalStateException("boom")

        try {
            repository.getCollection(id)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }
    }
}
