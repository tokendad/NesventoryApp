package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.CsvImportResult
import com.tokendad.nesventory.data.remote.NesVentoryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportRepositoryImplTest {
    private val api = mockk<NesVentoryApi>()
    private val repository = ImportRepositoryImpl(api)

    @Test
    fun `importCsv delegates to api`() = runTest {
        val part = mockk<MultipartBody.Part>()
        val expected = CsvImportResult(imported_count = 4, failed_count = 1)
        coEvery { api.importCsv(part) } returns expected

        val actual = repository.importCsv(part)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.importCsv(part) }
    }

    @Test
    fun `importNetworkItems delegates to api`() = runTest {
        val ids = listOf("a", "b")
        val expected = CsvImportResult(imported_count = 2, failed_count = 0)
        coEvery { api.importNetworkItems(ids) } returns expected

        val actual = repository.importNetworkItems(ids)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { api.importNetworkItems(ids) }
    }
}
