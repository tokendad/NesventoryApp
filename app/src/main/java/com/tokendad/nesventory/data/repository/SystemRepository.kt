package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.remote.AIStatusResponse
import com.tokendad.nesventory.data.remote.AITestConnectionResponse
import com.tokendad.nesventory.data.remote.MediaStatsResponse
import com.tokendad.nesventory.data.remote.StatusResponse

interface SystemRepository {
    suspend fun getStatus(): StatusResponse
    suspend fun getMediaStats(): MediaStatsResponse
    suspend fun getAIStatus(): AIStatusResponse
    suspend fun testAIConnection(): AITestConnectionResponse
}
