package com.example.courseworkdemo

import android.provider.BaseColumns


object TaskContract {
    const val CONTENT_AUTHORITY = "com.example.courseworkdemo.provider"
    const val PATH_TASKS = "tasks"
    val BASE_CONTENT_URI = android.net.Uri.parse("content://$CONTENT_AUTHORITY")

    object TaskEntry : BaseColumns {
        const val TABLE_NAME = "tasks"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DUE_DATE = "dueDate"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_PRIORITY = "priority"
        const val COLUMN_IS_COMPLETED = "isCompleted"

        val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build()
    }
}

