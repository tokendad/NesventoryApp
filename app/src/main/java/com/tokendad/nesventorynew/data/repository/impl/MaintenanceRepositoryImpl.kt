package com.tokendad.nesventorynew.data.repository.impl

import com.tokendad.nesventorynew.data.remote.MaintenanceTask
import com.tokendad.nesventorynew.data.remote.MaintenanceTaskCreate
import com.tokendad.nesventorynew.data.remote.MaintenanceTaskUpdate
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.data.repository.MaintenanceRepository
import java.util.UUID
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val api: NesVentoryApi
) : MaintenanceRepository {

    override suspend fun getMaintenanceTasks(): List<MaintenanceTask> = api.getMaintenanceTasks()

    override suspend fun getMaintenanceTask(taskId: UUID): MaintenanceTask = api.getMaintenanceTask(taskId)

    override suspend fun getMaintenanceTasksForItem(itemId: UUID): List<MaintenanceTask> =
        api.getMaintenanceTasksForItem(itemId)

    override suspend fun createMaintenanceTask(task: MaintenanceTaskCreate): MaintenanceTask =
        api.createMaintenanceTask(task)

    override suspend fun updateMaintenanceTask(taskId: UUID, task: MaintenanceTaskUpdate): MaintenanceTask =
        api.updateMaintenanceTask(taskId, task)

    override suspend fun deleteMaintenanceTask(taskId: UUID) = api.deleteMaintenanceTask(taskId)
}
