package com.example.courseworkdemo

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun TaskCreationScreen(navController: NavController, viewModel: TaskViewModel) {
    val context = LocalContext.current
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Medium") }
    var showError by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            dueDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Create New Task",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Task Name Field
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && taskName.isBlank(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Task Description Field
        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Due Date Picker Button
        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = if (dueDate.isEmpty()) "Select Due Date" else "Due: $dueDate")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Category Dropdown Selector
        DropdownSelector(
            label = "Category",
            options = listOf("Personal", "Work", "School", "Fitness"),
            selected = category,
            onSelect = { category = it }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Priority Dropdown Selector
        DropdownSelector(
            label = "Priority",
            options = listOf("High", "Medium", "Low"),
            selected = priority,
            onSelect = { priority = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                if (taskName.isBlank()) {
                    showError = true
                    return@Button
                }

                val newTask = Task(
                    name = taskName,
                    description = taskDescription,
                    dueDate = dueDate,
                    category = category,
                    priority = priority
                )
                viewModel.addTask(newTask)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Parse the due date
                val taskDueDate = sdf.parse(dueDate)
                if (taskDueDate != null) {
                    // Set notifications for the day before and on the due date
                    val taskDueCalendar = Calendar.getInstance().apply { time = taskDueDate }

                    // Schedule notification for a day before the due date
                    val oneDayBefore = taskDueCalendar.apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val workRequest1 = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                        .setInputData(workDataOf("due_date" to dueDate))
                        .setInitialDelay(oneDayBefore.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build()

                    // Schedule notification for the due date
                    val workRequest2 = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                        .setInputData(workDataOf("due_date" to dueDate))
                        .setInitialDelay(taskDueDate.time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build()

                    WorkManager.getInstance(context).enqueue(workRequest1)
                    WorkManager.getInstance(context).enqueue(workRequest2)
                }

                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Save Task")
        }

        // Error message for missing task name
        if (showError && taskName.isBlank()) {
            Text("Task name is required", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun DropdownSelector(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = selected)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}