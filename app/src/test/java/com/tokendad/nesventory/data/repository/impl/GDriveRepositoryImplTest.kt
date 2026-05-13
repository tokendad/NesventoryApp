package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.GDriveStatus
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response

class GDriveRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = GDriveRepositoryImpl(api)

    @Test
    fun `getStatus delegates to api`() = runTest {
        val expected = GDriveStatus(connected = true, email = "demo@example.com")
        coEvery { api.getGDriveStatus() } returns expected

        val actual = repository.getStatus()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getGDriveStatus() }
    }

    @Test
    fun `disconnect throws when api response is unsuccessful`() = runTest {
        val responseBody = "failed".toResponseBody("text/plain".toMediaType())
        coEvery { api.disconnectGDrive() } returns Response.error(500, responseBody)

        try {
            repository.disconnect()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("Failed to disconnect Google Drive (HTTP 500)", e.message)
        }
    }

    @Test
    fun `deleteBackup throws when api response is unsuccessful`() = runTest {
        val responseBody = "failed".toResponseBody("text/plain".toMediaType())
        coEvery { api.deleteGDriveBackup("backup-1") } returns Response.error(500, responseBody)

        try {
            repository.deleteBackup("backup-1")
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("Failed to delete backup (HTTP 500)", e.message)
        }
    }
}
