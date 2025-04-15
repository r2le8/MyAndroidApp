package com.example.courseworkdemo


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val dueDate: String,
    val category: String,
    val priority: String,
    val isCompleted: Boolean = false
)
