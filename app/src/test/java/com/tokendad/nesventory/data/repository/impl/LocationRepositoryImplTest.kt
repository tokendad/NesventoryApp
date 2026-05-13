package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.LocationPhoto
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

    @Test
    fun `upload and delete location photo delegate to api`() = runTest {
        val locationId = UUID.randomUUID()
        val photoId = UUID.randomUUID()
        val multipart = mockk<okhttp3.MultipartBody.Part>(relaxed = true)
        val expected = mockk<LocationPhoto>(relaxed = true)
        coEvery { api.uploadLocationPhoto(locationId, multipart, true, "overview") } returns expected
        coEvery { api.deleteLocationPhoto(locationId, photoId) } returns Response.success(Unit)

        val uploaded = repository.uploadLocationPhoto(locationId, multipart, isPrimary = true, photoType = "overview")
        repository.deleteLocationPhoto(locationId, photoId)

        assertEquals(expected, uploaded)
        coVerify(exactly = 1) { api.uploadLocationPhoto(locationId, multipart, true, "overview") }
        coVerify(exactly = 1) { api.deleteLocationPhoto(locationId, photoId) }
    }
}
