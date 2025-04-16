package com.example.courseworkdemo

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager

import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        createNotificationChannel()

        // Get the task's due date from input data
        val dueDateString = inputData.getString("due_date") ?: return Result.failure()
        val dueDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dueDateString)
        val currentDate = Calendar.getInstance().time

        if (dueDate == null || currentDate == null) {
            return Result.failure()
        }

        // Calculate the difference in days
        val diffInMillis = dueDate.time - currentDate.time
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        // Send notification if task is due today or a day before
        if (diffInDays == 0L || diffInDays == 1L) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

                val notification = NotificationCompat.Builder(applicationContext, "task_channel")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Task Reminder")
                    .setContentText("You have a task due soon. Don't forget to check your task list!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(1, notification)
                }
            } else {
                // Log or handle the case where permission was not granted
                return Result.failure()
            }
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminder Channel"
            val descriptionText = "Notifies about upcoming tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
