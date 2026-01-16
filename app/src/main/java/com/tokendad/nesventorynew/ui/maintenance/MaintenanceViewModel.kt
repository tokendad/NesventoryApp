package com.tokendad.nesventorynew.ui.maintenance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.Item
import com.tokendad.nesventorynew.data.remote.MaintenanceTask
import com.tokendad.nesventorynew.data.remote.MaintenanceTaskCreate
import com.tokendad.nesventorynew.data.remote.MaintenanceTaskUpdate
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    var tasks by mutableStateOf<List<MaintenanceTask>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Filter state
    var filterState by mutableStateOf("all") // all, pending, completed

    // Create dialog state
    var showCreateDialog by mutableStateOf(false)
    var availableItems by mutableStateOf<List<Item>>(emptyList())
    var newTaskTitle by mutableStateOf("")
    var newTaskDescription by mutableStateOf("")
    var newTaskItemId by mutableStateOf<UUID?>(null)
    var newTaskDueDate by mutableStateOf("")
    var newTaskFrequency by mutableStateOf<String?>(null)
    var newTaskColor by mutableStateOf<String?>(null)
    var isCreating by mutableStateOf(false)

    // Delete confirmation state
    var taskToDelete by mutableStateOf<MaintenanceTask?>(null)

    val filteredTasks: List<MaintenanceTask>
        get() = when (filterState) {
            "pending" -> tasks.filter { !it.completed }
            "completed" -> tasks.filter { it.completed }
            else -> tasks
        }

    init {
        fetchTasks()
        fetchItems()
    }

    fun fetchTasks() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                tasks = api.getMaintenanceTasks()
            } catch (e: Exception) {
                errorMessage = "Failed to load maintenance tasks: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchItems() {
        viewModelScope.launch {
            try {
                availableItems = api.getItems()
            } catch (e: Exception) {
                // Silently fail - items are just for the dropdown
            }
        }
    }

    fun toggleTaskCompletion(task: MaintenanceTask) {
        viewModelScope.launch {
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val update = MaintenanceTaskUpdate(
                    completed = !task.completed,
                    completed_date = if (!task.completed) {
                        currentDate
                    } else null
                )
                api.updateMaintenanceTask(task.id, update)
                fetchTasks()
            } catch (e: Exception) {
                errorMessage = "Failed to update task: ${e.localizedMessage}"
            }
        }
    }

    fun createTask() {
        val itemId = newTaskItemId ?: return
        if (newTaskTitle.isBlank() || newTaskDueDate.isBlank()) {
            errorMessage = "Title and due date are required"
            return
        }

        viewModelScope.launch {
            isCreating = true
            errorMessage = null
            try {
                val taskCreate = MaintenanceTaskCreate(
                    item_id = itemId,
                    title = newTaskTitle.trim(),
                    description = newTaskDescription.trim().ifBlank { null },
                    due_date = newTaskDueDate.trim(),
                    frequency = newTaskFrequency,
                    color = newTaskColor
                )
                api.createMaintenanceTask(taskCreate)
                successMessage = "Task created successfully"
                resetCreateForm()
                showCreateDialog = false
                fetchTasks()
            } catch (e: Exception) {
                errorMessage = "Failed to create task: ${e.localizedMessage}"
            } finally {
                isCreating = false
            }
        }
    }

    fun deleteTask(task: MaintenanceTask) {
        viewModelScope.launch {
            try {
                api.deleteMaintenanceTask(task.id)
                successMessage = "Task deleted"
                taskToDelete = null
                fetchTasks()
            } catch (e: Exception) {
                errorMessage = "Failed to delete task: ${e.localizedMessage}"
            }
        }
    }

    fun resetCreateForm() {
        newTaskTitle = ""
        newTaskDescription = ""
        newTaskItemId = null
        newTaskDueDate = ""
        newTaskFrequency = null
        newTaskColor = null
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
