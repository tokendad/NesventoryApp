package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class LocationRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = LocationRepositoryImpl(api)

    @Test
    fun `getLocations delegates to api`() = runTest {
        val expected = listOf(mockk<Location>(relaxed = true))
        coEvery { api.getLocations() } returns expected

        val actual = repository.getLocations()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getLocations() }
    }

    @Test
    fun `deleteLocation propagates api exceptions`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.deleteLocation(id) } throws IllegalStateException("boom")

        try {
            repository.deleteLocation(id)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }
    }
}
