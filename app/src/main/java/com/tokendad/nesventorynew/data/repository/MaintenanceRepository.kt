package com.tokendad.nesventorynew.data.repository

import com.tokendad.nesventorynew.data.remote.MaintenanceTask
import com.tokendad.nesventorynew.data.remote.MaintenanceTaskCreate
import com.tokendad.nesventorynew.data.remote.MaintenanceTaskUpdate
import java.util.UUID

interface MaintenanceRepository {
    suspend fun getMaintenanceTasks(): List<MaintenanceTask>
    suspend fun getMaintenanceTask(taskId: UUID): MaintenanceTask
    suspend fun getMaintenanceTasksForItem(itemId: UUID): List<MaintenanceTask>
    suspend fun createMaintenanceTask(task: MaintenanceTaskCreate): MaintenanceTask
    suspend fun updateMaintenanceTask(taskId: UUID, task: MaintenanceTaskUpdate): MaintenanceTask
    suspend fun deleteMaintenanceTask(taskId: UUID)
}
