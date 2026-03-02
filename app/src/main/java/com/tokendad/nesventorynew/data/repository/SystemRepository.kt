package com.tokendad.nesventorynew.data.repository

import com.tokendad.nesventorynew.data.remote.AIStatusResponse
import com.tokendad.nesventorynew.data.remote.AITestConnectionResponse
import com.tokendad.nesventorynew.data.remote.MediaStatsResponse
import com.tokendad.nesventorynew.data.remote.StatusResponse

interface SystemRepository {
    suspend fun getStatus(): StatusResponse
    suspend fun getMediaStats(): MediaStatsResponse
    suspend fun getAIStatus(): AIStatusResponse
    suspend fun testAIConnection(): AITestConnectionResponse
}
