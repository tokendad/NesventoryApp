package com.tokendad.nesventorynew.ui.maintenance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokendad.nesventorynew.data.remote.MaintenanceTask
import com.tokendad.nesventorynew.data.remote.MaintenanceTaskUpdate
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val api: NesVentoryApi
) : ViewModel() {

    var tasks by mutableStateOf<List<MaintenanceTask>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchTasks()
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
}