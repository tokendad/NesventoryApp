package com.tokendad.nesventory.data.repository.impl

import com.google.gson.Gson
import com.tokendad.nesventory.data.local.LocationDao
import com.tokendad.nesventory.data.local.SyncQueueDao
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.ServerProfileList
import com.tokendad.nesventory.data.remote.Location
import com.tokendad.nesventory.data.remote.LocationPhoto
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
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class LocationRepositoryImplTest {
    private val api = mockk<NesVentoryApi>(relaxed = true)
    private val locationDao = mockk<LocationDao>(relaxed = true)
    private val syncQueueDao = mockk<SyncQueueDao>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>(relaxed = true)
    private val repository = LocationRepositoryImpl(api, locationDao, syncQueueDao, preferencesManager, Gson())

    init {
        every { preferencesManager.serverProfiles } returns flowOf(ServerProfileList())
    }

    @Test
    fun `getLocations delegates to api`() = runTest {
        val expected = listOf(
            Location(
                id = UUID.randomUUID(),
                name = "Garage",
                created_at = "2024-01-01T00:00:00Z",
                updated_at = "2024-01-01T00:00:00Z"
            )
        )
        coEvery { locationDao.getAll() } returns emptyList()
        coEvery { api.getLocations(null, null) } returns expected

        val actual = repository.getLocations()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getLocations(null, null) }
    }

    @Test
    fun `deleteLocation queues pending delete when api fails`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.deleteLocation(id) } throws IOException("offline")

        repository.deleteLocation(id)

        coVerify(exactly = 1) { syncQueueDao.insert(any()) }
        coVerify(exactly = 1) { locationDao.deleteById(id.toString()) }
    }

    @Test
    fun `deleteLocation propagates non-transient api exceptions`() = runTest {
        val id = UUID.randomUUID()
        coEvery { api.deleteLocation(id) } throws IllegalStateException("boom")

        try {
            repository.deleteLocation(id)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }

        coVerify(exactly = 0) { syncQueueDao.insert(any()) }
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
