package com.tokendad.nesventory.data.repository.impl

import com.tokendad.nesventory.data.remote.AIStatusResponse
import com.tokendad.nesventory.data.remote.AITestConnectionResponse
import com.tokendad.nesventory.data.remote.MediaStatsResponse
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.data.remote.StatusResponse
import com.tokendad.nesventory.data.repository.SystemRepository
import okhttp3.ResponseBody
import javax.inject.Inject

class SystemRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : SystemRepository {

    override suspend fun getStatus(): StatusResponse = api.getStatus()

    override suspend fun getMediaStats(): MediaStatsResponse = api.getMediaStats()

    override suspend fun getAIStatus(): AIStatusResponse = api.getAIStatus()

    override suspend fun testAIConnection(): AITestConnectionResponse = api.testAIConnection()

    override suspend fun listLogFiles(): List<String> = api.listLogFiles()

    override suspend fun readLogFile(filename: String): ResponseBody = api.readLogFile(filename)

    override suspend fun rotateLogs(): StatusResponse = api.rotateLogs()

    override suspend fun cleanupLogs(): StatusResponse = api.cleanupLogs()
}
