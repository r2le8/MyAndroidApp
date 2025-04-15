package com.example.courseworkdemo

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
            _tasks.value = taskDao.getAllTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
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
