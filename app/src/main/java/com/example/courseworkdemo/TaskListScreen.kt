package com.example.courseworkdemo
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val taskList by viewModel.activeTasks.collectAsState()
    val context = LocalContext.current
    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy") }

    var selectedCategory by rememberSaveable { mutableStateOf("All") }

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
                title = {
                    Text(
                        text = "Task List",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                modifier = Modifier.height(70.dp), // Standard AppBar height
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            // Dropdown filter
            CategoryDropdown(
                categories = categories,
                selected = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val listState = rememberSaveable(saver = LazyListState.Saver) {
                LazyListState()
            }

            // Wrap LazyColumn in a Box with weight to limit height
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onComplete = { viewModel.markTaskCompleted(task) },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onEdit = { /* Optional: Navigate to edit screen */ },
                            onShare = { shareTask(context, task) }
                        )
                    }
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Category: $selected", style = MaterialTheme.typography.bodyMedium)
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
    onShare: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface // Use theme's onSurface for text color

    // Priority colors based on Material Design 3 colors
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> MaterialTheme.colorScheme.error // Red from MD3 for high priority
        "medium" -> MaterialTheme.colorScheme.tertiary  // Yellow from MD3 for medium priority
        else -> MaterialTheme.colorScheme.primary // Green from MD3 for low priority
    }

    // Card background color based on priority
    val cardBackgroundColor = when (task.priority.lowercase()) {
        "high" -> MaterialTheme.colorScheme.errorContainer // Light red from MD3 for high priority
        "medium" -> MaterialTheme.colorScheme.tertiaryContainer // Light yellow from MD3 for medium priority
        else -> MaterialTheme.colorScheme.primaryContainer // Light green from MD3 for low priority
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title and Due Date
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(task.name, style = MaterialTheme.typography.titleMedium, color = textColor)
                Text(task.dueDate, style = MaterialTheme.typography.bodySmall, color = textColor)
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Description and Category
            Text(task.description, style = MaterialTheme.typography.bodyMedium, color = textColor)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Category: ${task.category}", style = MaterialTheme.typography.bodySmall, color = textColor)
                Text("Priority: ${task.priority}", color = priorityColor)
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Icon buttons aligned to the right
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Complete Task", tint = textColor)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = textColor)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share Task", tint = textColor)
                }
            }
        }
    }
}



fun shareTask(context: Context, task: Task) {
    val shareText = """
        Hi, I want to share a task with you.

        Task: ${task.name}
        Task Detail: ${task.description}
        Due: ${task.dueDate}
    """.trimIndent()

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share task via"))
}

