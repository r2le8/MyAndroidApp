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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val taskList by viewModel.activeTasks.collectAsState()
    val context = LocalContext.current
    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy") }

    var selectedCategory by remember { mutableStateOf("All") }

    // Get unique categories for the dropdown
    val categories = remember(taskList) {
        listOf("All") + taskList.map { it.category }.distinct().sorted()
    }

    // Filter and sort tasks
    val visibleTasks by remember(taskList, selectedCategory) {
        mutableStateOf(
            taskList
                .filter { selectedCategory == "All" || it.category == selectedCategory }
                .sortedBy {
                    try {
                        java.time.LocalDate.parse(it.dueDate, formatter)
                    } catch (e: Exception) {
                        java.time.LocalDate.MAX
                    }
                }
        )
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
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            // Dropdown filter
            CategoryDropdown(
                categories = categories,
                selected = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(visibleTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onComplete = {
                            viewModel.markTaskCompleted(task)
                        },
                        onDelete = {
                            viewModel.deleteTask(task.id)
                        },
                        onEdit = {
                            // Optional: Navigate to edit screen
                        },
                        onShare = { shareTask(context, task) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDropdown(
    categories: List<String>,
    selected: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Category: $selected")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
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

