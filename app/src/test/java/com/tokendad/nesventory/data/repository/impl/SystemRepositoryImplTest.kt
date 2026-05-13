package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.AIStatusResponse
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.StatusResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SystemRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = SystemRepositoryImpl(api)

    @Test
    fun `getAIStatus delegates to api`() = runTest {
        val expected = AIStatusResponse(enabled = true, model = "test-model")
        coEvery { api.getAIStatus() } returns expected

        val actual = repository.getAIStatus()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.getAIStatus() }
    }

    @Test
    fun `testAIConnection propagates api exceptions`() = runTest {
        coEvery { api.testAIConnection() } throws IllegalStateException("boom")

        try {
            repository.testAIConnection()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("boom", e.message)
        }
    }

    @Test
    fun `listLogFiles delegates to api`() = runTest {
        val expected = listOf("app.log", "error.log")
        coEvery { api.listLogFiles() } returns expected

        val actual = repository.listLogFiles()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.listLogFiles() }
    }

    @Test
    fun `readLogFile delegates to api`() = runTest {
        val expected = "line1\nline2".toResponseBody(null)
        coEvery { api.readLogFile("app.log") } returns expected

        val actual = repository.readLogFile("app.log")

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.readLogFile("app.log") }
    }

    @Test
    fun `rotateLogs delegates to api`() = runTest {
        val expected = StatusResponse(status = "ok")
        coEvery { api.rotateLogs() } returns expected

        val actual = repository.rotateLogs()

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.rotateLogs() }
    }
}
