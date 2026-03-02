package com.tokendad.nesventorynew.data.repository.impl

import com.tokendad.nesventorynew.data.remote.AIStatusResponse
import com.tokendad.nesventorynew.data.remote.AITestConnectionResponse
import com.tokendad.nesventorynew.data.remote.MediaStatsResponse
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.data.remote.StatusResponse
import com.tokendad.nesventorynew.data.repository.SystemRepository
import javax.inject.Inject

class SystemRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : SystemRepository {

    override suspend fun getStatus(): StatusResponse = api.getStatus()

    override suspend fun getMediaStats(): MediaStatsResponse = api.getMediaStats()

    override suspend fun getAIStatus(): AIStatusResponse = api.getAIStatus()

    override suspend fun testAIConnection(): AITestConnectionResponse = api.testAIConnection()
}
