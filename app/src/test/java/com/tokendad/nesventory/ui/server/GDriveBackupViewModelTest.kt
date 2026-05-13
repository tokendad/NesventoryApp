package com.tokendad.nesventory.ui.server

import com.tokendad.nesventory.data.remote.GDriveBackup
import com.tokendad.nesventory.data.remote.GDriveBackupResult
import com.tokendad.nesventory.data.remote.GDriveConnectResponse
import com.tokendad.nesventory.data.remote.GDriveStatus
import com.tokendad.nesventory.data.repository.GDriveRepository
import com.tokendad.nesventory.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GDriveBackupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<GDriveRepository>(relaxed = true)
    private lateinit var viewModel: GDriveBackupViewModel

    @Before
    fun setup() {
        coEvery { repository.getStatus() } returns GDriveStatus(connected = false, backup_count = 0)
        coEvery { repository.listBackups() } returns emptyList()
        viewModel = GDriveBackupViewModel(repository)
    }

    @Test
    fun `init loads status and backups`() = runTest {
        advanceUntilIdle()

        assertEquals(false, viewModel.status?.connected)
        assertEquals(emptyList<GDriveBackup>(), viewModel.backups)
    }

    @Test
    fun `backupNow requires connection`() = runTest {
        advanceUntilIdle()

        viewModel.backupNow()

        assertEquals("Connect Google Drive before running a backup", viewModel.errorMessage)
        coVerify(exactly = 0) { repository.triggerBackup() }
    }

    @Test
    fun `connect emits auth url through callback`() = runTest {
        coEvery { repository.connect() } returns GDriveConnectResponse("https://example.com/auth")

        var openedUrl: String? = null
        viewModel.connect { openedUrl = it }
        advanceUntilIdle()

        assertEquals("https://example.com/auth", openedUrl)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `backupNow triggers backup when connected`() = runTest {
        coEvery { repository.getStatus() } returns GDriveStatus(connected = true, backup_count = 1)
        coEvery { repository.listBackups() } returns emptyList()
        coEvery { repository.triggerBackup() } returns GDriveBackupResult(success = true, message = "Backup complete")

        viewModel = GDriveBackupViewModel(repository)
        advanceUntilIdle()
        viewModel.backupNow()
        advanceUntilIdle()

        assertEquals("Backup complete", viewModel.successMessage)
        coVerify(exactly = 1) { repository.triggerBackup() }
    }
}
