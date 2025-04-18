package com.example.courseworkdemo

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TaskContentProviderTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val contentResolver = context.contentResolver

    @Before
    fun setup() {
        // Clear data before testing
        val taskUri = TaskContract.TaskEntry.CONTENT_URI
        contentResolver.delete(taskUri, null, null)
    }

    @Test
    fun testInsertTask() {
        // Initialize ContentValues outside the lambda expression
        val contentValues = ContentValues()
        contentValues.put(TaskContract.TaskEntry.COLUMN_NAME, "Test Task")
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, "This is a test task.")
        contentValues.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, "2025-04-30")
        contentValues.put(TaskContract.TaskEntry.COLUMN_CATEGORY, "Work")
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, "High")
        contentValues.put(TaskContract.TaskEntry.COLUMN_IS_COMPLETED, false)

        val uri: Uri = contentResolver.insert(TaskContract.TaskEntry.CONTENT_URI, contentValues) ?: return

        // Verify the task was inserted
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.apply {
            moveToFirst()
            val name = getString(getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME))
            assertEquals("Test Task", name)
            close()
        }
    }

    @Test
    fun testUpdateTask() {
        // Initialize ContentValues outside the lambda expression
        val contentValues = ContentValues()
        contentValues.put(TaskContract.TaskEntry.COLUMN_NAME, "Test Task")
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, "This is a test task.")
        contentValues.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, "2025-04-30")
        contentValues.put(TaskContract.TaskEntry.COLUMN_CATEGORY, "Work")
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, "High")
        contentValues.put(TaskContract.TaskEntry.COLUMN_IS_COMPLETED, false)

        val uri = contentResolver.insert(TaskContract.TaskEntry.CONTENT_URI, contentValues) ?: return

        // Update the task
        val updatedValues = ContentValues()
        updatedValues.put(TaskContract.TaskEntry.COLUMN_NAME, "Updated Task")
        val rowsUpdated = contentResolver.update(uri, updatedValues, null, null)
        assertEquals(1, rowsUpdated)

        // Verify the update
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.apply {
            moveToFirst()
            val name = getString(getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME))
            assertEquals("Updated Task", name)
            close()
        }
    }

    @Test
    fun testDeleteTask() {
        // Initialize ContentValues outside the lambda expression
        val contentValues = ContentValues()
        contentValues.put(TaskContract.TaskEntry.COLUMN_NAME, "Test Task")
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, "This is a test task.")
        contentValues.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, "2025-04-30")
        contentValues.put(TaskContract.TaskEntry.COLUMN_CATEGORY, "Work")
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, "High")
        contentValues.put(TaskContract.TaskEntry.COLUMN_IS_COMPLETED, false)

        val uri = contentResolver.insert(TaskContract.TaskEntry.CONTENT_URI, contentValues) ?: return

        // Delete the task
        val rowsDeleted = contentResolver.delete(uri, null, null)
        assertEquals(1, rowsDeleted)

        // Verify the task was deleted
        val cursor = contentResolver.query(uri, null, null, null, null)
        assertEquals(0, cursor?.count)
        cursor?.close()
    }
}
