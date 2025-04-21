package com.example.courseworkdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.room.Room
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.lightColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Notification permission check (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        val db = Room.databaseBuilder(applicationContext, TaskDatabase::class.java, "task-database").build()
        val taskDao = db.taskDao()
        val viewModel = TaskViewModel(taskDao)


        setContent {
            val navController = rememberNavController()
            var darkModeEnabled by remember { mutableStateOf(false) }

            // Apply dark mode theme dynamically
            // Apply dark mode theme dynamically using colorScheme
            val colors = if (darkModeEnabled) darkColorScheme() else lightColorScheme()

            MaterialTheme(
                colorScheme = colors
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Scaffold(
                        bottomBar = { BottomNavBar(navController) }
                    ) { _ ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 28.dp)
                                .fillMaxSize()
                        ) {
                            composable("home") { HomeScreen(navController, viewModel) }
                            composable("task_creation") { TaskCreationScreen(navController, viewModel) }
                            composable("task_list") { TaskListScreen(navController, viewModel) }
                            composable("settings") {
                                SettingsScreen(
                                    isDarkMode = darkModeEnabled,
                                    onDarkModeChanged = { darkModeEnabled = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Task") },
            label = { Text("Add") },
            selected = false,
            onClick = { navController.navigate("task_creation") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Tasks") },
            label = { Text("Tasks") },
            selected = false,
            onClick = { navController.navigate("task_list") }
        )

    }
}
