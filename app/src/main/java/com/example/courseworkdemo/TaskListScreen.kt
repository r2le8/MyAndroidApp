package com.example.courseworkdemo
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable

fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val taskList by viewModel.tasks.collectAsState()

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(taskList) { task ->
            TaskCard(
                task = task,
                onComplete = { viewModel.markTaskCompleted(task) },
                onDelete = { viewModel.deleteTask(task.id) }, // assuming you have this
                onEdit = {
                    // Navigate to EditTaskScreen (optional)
                    // navController.navigate("edit/${task.id}")
                }
            )
        }
    }
}

@Composable
fun TaskCard(task: Task, onComplete: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color.Red
        "medium" -> Color.Yellow
        else -> Color.Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(task.name, style = MaterialTheme.typography.titleMedium)
                Text(task.dueDate, style = MaterialTheme.typography.bodySmall)
            }
            Text(task.description, style = MaterialTheme.typography.bodyMedium)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Category: ${task.category}", style = MaterialTheme.typography.bodySmall)
                Text("Priority: ${task.priority}", color = priorityColor)
            }
            Row {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Complete Task")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                }
            }
        }
    }
}
