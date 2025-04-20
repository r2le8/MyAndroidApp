package com.example.courseworkdemo

// Core Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// For dates
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: TaskViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.activeTasks.collectAsState(initial = emptyList())
    val today = remember { LocalDate.now() }
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    val completedCount by viewModel.completedCount.collectAsState()

    // Search query state
    var searchQuery by remember { mutableStateOf("") }

    // Handle task categorization (Due Today and Overdue)
    val dueToday = tasks.filter {
        try {
            LocalDate.parse(it.dueDate, formatter) == today
        } catch (e: Exception) {
            false
        }
    }

    val overdue = tasks.filter {
        try {
            LocalDate.parse(it.dueDate, formatter) < today
        } catch (e: Exception) {
            false
        }
    }

    // Filter tasks based on search query
    val filteredTasks = tasks
        .filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
        .sortedBy {
            try {
                LocalDate.parse(it.dueDate, formatter)
            } catch (e: Exception) {
                LocalDate.MAX // Puts tasks with invalid dates at the end
            }
        }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Quick stats
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            TaskStatCard("Due Today", dueToday.size)
            TaskStatCard("Overdue", overdue.size)
            TaskStatCard("Completed", completedCount)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { navController.navigate("task_creation") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("➕ Add Task")
            }
            Button(
                onClick = { navController.navigate("task_list") },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("📋 Task List")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Tasks") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks List
        Text("Tasks", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(filteredTasks, key = { it.id }) { task ->
                SwipeToDeleteTaskItem(
                    task = task,
                    onDelete = { viewModel.deleteTask(task.id) },
                    onComplete = { viewModel.markTaskCompleted(task) }
                )
            }
        }
    }
}

@Composable
fun TaskStatCard(label: String, count: Int) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(100.dp, 120.dp),  // Set a fixed width and height for the card
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()  // Ensure the Column takes up all available space in the Card
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center  // Vertically center the content
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(count.toString(), style = MaterialTheme.typography.titleLarge)
        }
    }
}





    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SwipeToDeleteTaskItem(
        task: Task,
        onDelete: () -> Unit,
        onComplete: () -> Unit
    ) {
        val dismissState = rememberDismissState(
            confirmValueChange = {
                if (it == DismissValue.DismissedToStart) { // ✅ Only allow EndToStart (left swipe)
                    onDelete()
                    true
                } else {
                    false // ❌ Ignore right swipe
                }
            },
            positionalThreshold = { fullWidth -> fullWidth * 0.5f } // Must swipe >50%
        )

        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart), // ✅ Only allow swipe left
            background = {
                if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            },
            dismissContent = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
        )
    }
