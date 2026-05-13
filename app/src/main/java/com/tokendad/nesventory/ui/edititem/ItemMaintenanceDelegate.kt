package com.tokendad.nesventory.ui.edititem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tokendad.nesventory.data.remote.MaintenanceTask
import com.tokendad.nesventory.data.remote.MaintenanceTaskUpdate
import com.tokendad.nesventory.data.repository.MaintenanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ItemMaintenanceDelegate(
    private val maintenanceRepository: MaintenanceRepository,
    private val scope: CoroutineScope,
    private val onError: (String) -> Unit
) {
    var tasks by mutableStateOf<List<MaintenanceTask>>(emptyList())
        private set

    fun fetchTasks(itemId: UUID) {
        scope.launch {
            try {
                tasks = maintenanceRepository.getMaintenanceTasksForItem(itemId)
            } catch (e: Exception) {
                onError("Failed to fetch maintenance tasks: ${e.localizedMessage}")
            }
        }
    }

    fun toggleTask(task: MaintenanceTask, itemId: UUID) {
        scope.launch {
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val update = MaintenanceTaskUpdate(
                    completed = !task.completed,
                    completed_date = if (!task.completed) currentDate else null
                )
                maintenanceRepository.updateMaintenanceTask(task.id, update)
                fetchTasks(itemId)
            } catch (e: Exception) {
                onError("Failed to update task: ${e.localizedMessage}")
            }
        }
    }
}
