package com.example.courseworkdemo

// Core Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = { navController.navigate("settings") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⚙️ Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Quick stats row
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            TaskStatCard("Due Today", dueToday.size)
            TaskStatCard("Overdue", overdue.size)
            TaskStatCard("Completed", completedCount)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons for adding task or going to task list
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
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks Header with Legend on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📌 Tasks",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start
            )

            // Add the legend here
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.errorContainer,
                    label = "Overdue"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    label = "Due Soon"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    label = "Upcoming"
                )
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display tasks or "You're all caught up!" message
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🎉 You're all caught up!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
}
@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color = color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
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
            if (it == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { fullWidth -> fullWidth * 0.5f }
    )

    // State for showing the task details dialog
    val showTaskDetailsDialog = remember { mutableStateOf(false) }

    // Show Dialog when double-tapped
    if (showTaskDetailsDialog.value) {
        TaskDetailsDialog(
            task = task,
            onDismiss = { showTaskDetailsDialog.value = false }
        )
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
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
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val dueDate = try {
                LocalDate.parse(task.dueDate, formatter)
            } catch (e: Exception) {
                null
            }
            val today = LocalDate.now()

            val backgroundColor = when {
                dueDate == null -> MaterialTheme.colorScheme.surfaceVariant
                dueDate.isBefore(today) -> MaterialTheme.colorScheme.errorContainer
                dueDate.isBefore(today.plusDays(2)) -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                showTaskDetailsDialog.value = true
                            }
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(task.name, style = MaterialTheme.typography.titleMedium)
                    }

                    Text(
                        text = task.dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )

                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .height(36.dp)
                    ) {
                        Text("✔")
                    }
                }
            }
        }
    )
}

@Composable
fun TaskDetailsDialog(task: Task, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Task Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Task Name
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Due Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Due Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Due: ${task.dueDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                if (task.description.isNotEmpty()) {
                    Text(
                        text = "Description:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Priority
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Priority",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Priority: ${task.priority}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Close", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

