package com.example.courseworkdemo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted


class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    // NEW: Only show tasks that are not completed
    val activeTasks: StateFlow<List<Task>> = tasks
        .map { it.filter { task -> !task.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // NEW: Count of completed tasks
    val completedCount: StateFlow<Int> = tasks
        .map { it.count { task -> task.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val allTasks = taskDao.getAllTasks()
            Log.d("TaskViewModel", "📋 Loaded ${allTasks.size} tasks from DB: ${allTasks.map { it.name + " (" + it.dueDate + ")" }}")
            _tasks.value = allTasks
        }
    }


    fun addTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
            Log.d("TaskViewModel", "💾 Inserted task: ${task.name}, due: ${task.dueDate}")
            loadTasks() // Refresh task list
        }
    }


    fun markTaskCompleted(task: Task) {
        viewModelScope.launch {
            taskDao.update(task.copy(isCompleted = true))
            loadTasks() // Refresh task list
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            taskDao.deleteTask(taskId)
            loadTasks() // Refresh task list
        }
    }
}
