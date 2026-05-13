package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.AIStatusResponse
import com.tokendad.nesventory.data.remote.AITestConnectionResponse
import com.tokendad.nesventory.data.remote.MediaStatsResponse
import com.tokendad.nesventory.data.remote.StatusResponse
import okhttp3.ResponseBody

interface SystemRepository {
    suspend fun getStatus(): StatusResponse
    suspend fun getMediaStats(): MediaStatsResponse
    suspend fun getAIStatus(): AIStatusResponse
    suspend fun testAIConnection(): AITestConnectionResponse
    suspend fun listLogFiles(): List<String>
    suspend fun readLogFile(filename: String): ResponseBody
    suspend fun rotateLogs(): StatusResponse
    suspend fun cleanupLogs(): StatusResponse
}
