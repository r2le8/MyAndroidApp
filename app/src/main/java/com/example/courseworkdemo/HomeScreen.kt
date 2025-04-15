package com.example.courseworkdemo

// Core Compose
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// For dates
import java.time.LocalDate




@Composable
fun HomeScreen(navController: NavController, viewModel: TaskViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.activeTasks.collectAsState(initial = emptyList())
    val today = remember { LocalDate.now() }

    val dueToday = tasks.filter { LocalDate.parse(it.dueDate) == today }
    val overdue = tasks.filter { LocalDate.parse(it.dueDate) < today }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Quick stats
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            TaskStatCard("Due Today", dueToday.size)
            TaskStatCard("Overdue", overdue.size)
            TaskStatCard("Completed", viewModel.completedCount)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("task_creation") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Add Task")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tasks", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(tasks) { task ->
                TaskItem(task = task, onComplete = { viewModel.markTaskCompleted(task) })
            }
        }
    }
}

@Composable
fun TaskStatCard(label: String, count: Int) {
    Card(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(count.toString(), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun TaskItem(task: Task, onComplete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(task.name, style = MaterialTheme.typography.titleMedium)
            Text("Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
            Row {
                Button(onClick = onComplete) {
                    Text("✔ Complete")
                }
            }
        }
    }
}
