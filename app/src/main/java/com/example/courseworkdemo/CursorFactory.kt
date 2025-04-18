package com.example.courseworkdemo

import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns

object CursorFactory {

    // Convert a List<Task> to a Cursor
    fun createCursor(tasks: List<Task>): Cursor {
        // Define the columns for the cursor
        val columns = arrayOf(
            BaseColumns._ID, // Standard column name for ID
            TaskContract.TaskEntry.COLUMN_NAME,
            TaskContract.TaskEntry.COLUMN_DESCRIPTION,
            TaskContract.TaskEntry.COLUMN_DUE_DATE,
            TaskContract.TaskEntry.COLUMN_CATEGORY,
            TaskContract.TaskEntry.COLUMN_PRIORITY,
            TaskContract.TaskEntry.COLUMN_IS_COMPLETED
        )

        // Create a MatrixCursor (this is a type of Cursor that can hold data in memory)
        val cursor = MatrixCursor(columns)

        // Populate the cursor with the data from the Task list
        tasks.forEach { task ->
            cursor.addRow(arrayOf(
                task.id, // _ID
                task.name,
                task.description,
                task.dueDate,
                task.category,
                task.priority,
                task.isCompleted
            ))
        }

        return cursor
    }
}
