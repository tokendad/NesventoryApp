package com.tokendad.nesventory.ui.server

import com.tokendad.nesventory.data.remote.StatusResponse
import com.tokendad.nesventory.data.repository.SystemRepository
import com.tokendad.nesventory.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminLogsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val systemRepository = mockk<SystemRepository>(relaxed = true)

    @Test
    fun `loadLogFiles populates files`() = runTest {
        coEvery { systemRepository.listLogFiles() } returns listOf("app.log", "error.log")
        val viewModel = AdminLogsViewModel(systemRepository)
        advanceUntilIdle()

        assertEquals(2, viewModel.logFiles.size)
    }

    @Test
    fun `openLog loads line content`() = runTest {
        coEvery { systemRepository.listLogFiles() } returns emptyList()
        coEvery { systemRepository.readLogFile("app.log") } returns "line1\nline2".toResponseBody(null)
        val viewModel = AdminLogsViewModel(systemRepository)
        advanceUntilIdle()

        viewModel.openLog("app.log")
        advanceUntilIdle()

        assertEquals(2, viewModel.logLines.size)
        assertEquals("line1", viewModel.logLines.first())
    }

    @Test
    fun `rotateLogs delegates to repository`() = runTest {
        coEvery { systemRepository.listLogFiles() } returns emptyList()
        coEvery { systemRepository.rotateLogs() } returns StatusResponse(status = "ok")
        val viewModel = AdminLogsViewModel(systemRepository)
        advanceUntilIdle()

        viewModel.rotateLogs()
        advanceUntilIdle()

        assertEquals("Logs rotated successfully", viewModel.successMessage)
        coVerify(exactly = 1) { systemRepository.rotateLogs() }
    }
}
