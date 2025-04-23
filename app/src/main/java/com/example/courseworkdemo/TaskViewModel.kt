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
    private var lastDeletedTask: Task? = null

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    val activeTasks: StateFlow<List<Task>> = tasks
        .map { it.filter { task -> !task.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val completedCount: StateFlow<Int> = tasks
        .map { it.count { task -> task.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val allTasks = taskDao.getAllTasks()
            _tasks.value = allTasks
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
            val task = taskDao.getTaskById(taskId) // Fetch the task before deletion
            lastDeletedTask = task // Store the task for undo
            taskDao.deleteTask(taskId)
            loadTasks() // Refresh task list
        }
    }

    // Undo last delete action
    fun undoDelete() {
        lastDeletedTask?.let { task ->
            viewModelScope.launch {
                taskDao.insert(task) // Restore the task
                lastDeletedTask = null // Clear the undo buffer
                loadTasks() // Refresh task list
            }
        }
    }
}

