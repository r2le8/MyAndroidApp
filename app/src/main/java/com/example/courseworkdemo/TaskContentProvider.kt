package com.example.courseworkdemo

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskContentProvider : ContentProvider() {

    private lateinit var taskDatabase: TaskDatabase

    override fun onCreate(): Boolean {
        context?.let {
            taskDatabase = TaskDatabase.getDatabase(it)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val taskDao = taskDatabase.taskDao()
        var cursor: Cursor?

        // Perform the query on a background thread
        GlobalScope.launch(Dispatchers.IO) {
            when (uri.pathSegments[0]) {
                TaskContract.PATH_TASKS -> {
                    val tasks = taskDao.getAllTasks()
                    cursor = CursorFactory.createCursor(tasks) // Use your own implementation to convert List<Task> to Cursor
                }
                else -> throw IllegalArgumentException("Unknown URI $uri")
            }
        }
        return null // Return null until the background task is done
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val taskDao = taskDatabase.taskDao()

        // Launch a coroutine to run the suspend insert in the background
        GlobalScope.launch(Dispatchers.IO) {
            val task = Task(
                name = values?.getAsString(TaskContract.TaskEntry.COLUMN_NAME) ?: "",
                description = values?.getAsString(TaskContract.TaskEntry.COLUMN_DESCRIPTION) ?: "",
                dueDate = values?.getAsString(TaskContract.TaskEntry.COLUMN_DUE_DATE) ?: "",
                category = values?.getAsString(TaskContract.TaskEntry.COLUMN_CATEGORY) ?: "",
                priority = values?.getAsString(TaskContract.TaskEntry.COLUMN_PRIORITY) ?: "",
                isCompleted = values?.getAsBoolean(TaskContract.TaskEntry.COLUMN_IS_COMPLETED) ?: false
            )

            val taskId = taskDao.insert(task)

            // Post the result back to the main thread using a Handler
            val resultUri = Uri.withAppendedPath(TaskContract.TaskEntry.CONTENT_URI, taskId.toString())
            Handler(Looper.getMainLooper()).post {
                // Return the Uri after the insert is completed
                context?.contentResolver?.notifyChange(resultUri, null)
            }
        }
        return null  // Returning null since the actual Uri will be handled by the Handler
    }


    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val taskDao = taskDatabase.taskDao()
        val task = Task(
            id = selectionArgs?.get(0)?.toInt() ?: 0,
            name = values?.getAsString(TaskContract.TaskEntry.COLUMN_NAME) ?: "",
            description = values?.getAsString(TaskContract.TaskEntry.COLUMN_DESCRIPTION) ?: "",
            dueDate = values?.getAsString(TaskContract.TaskEntry.COLUMN_DUE_DATE) ?: "",
            category = values?.getAsString(TaskContract.TaskEntry.COLUMN_CATEGORY) ?: "",
            priority = values?.getAsString(TaskContract.TaskEntry.COLUMN_PRIORITY) ?: "",
            isCompleted = values?.getAsBoolean(TaskContract.TaskEntry.COLUMN_IS_COMPLETED) ?: false
        )

        // Run the update on a background thread
        GlobalScope.launch(Dispatchers.IO) {
            return@launch taskDao.update(task)
        }
        return 0 // Return value should be updated via coroutine, so this is a placeholder
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val taskDao = taskDatabase.taskDao()

        // Delete task on a background thread
        GlobalScope.launch(Dispatchers.IO) {
            val taskId = selectionArgs?.get(0)?.toInt() ?: 0
            taskDao.deleteTask(taskId)
        }
        return 0 // Return value should be updated via coroutine, so this is a placeholder
    }

    override fun getType(uri: Uri): String? {
        return when (uri.pathSegments[0]) {
            TaskContract.PATH_TASKS -> "vnd.android.cursor.dir/${TaskContract.CONTENT_AUTHORITY}.${TaskContract.PATH_TASKS}"
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }
}
