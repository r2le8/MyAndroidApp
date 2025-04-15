package com.example.courseworkdemo
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val taskList by viewModel.activeTasks.collectAsState()
    val context = LocalContext.current

    // Use remember to track visible tasks on this screen
    var visibleTasks by remember { mutableStateOf(taskList) }

    // Sync with ViewModel whenever taskList changes
    LaunchedEffect(taskList) {
        visibleTasks = taskList
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(visibleTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onComplete = {
                        viewModel.markTaskCompleted(task)
                        // Immediately remove from visible tasks
                        visibleTasks = visibleTasks.filter { it.id != task.id }
                    },
                    onDelete = {
                        viewModel.deleteTask(task.id)
                        visibleTasks = visibleTasks.filter { it.id != task.id }
                    },
                    onEdit = {
                        // Optional: Navigate to EditTaskScreen
                    },
                    onShare = { shareTask(context, task) }
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit // ✅ New param
) {
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
                IconButton(onClick = onShare) { // ✅ Share icon button
                    Icon(Icons.Default.Share, contentDescription = "Share Task")
                }
            }
        }
    }
}

fun shareTask(context: Context, task: Task) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Task: ${task.name}\n${task.description}")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share task via"))
}

