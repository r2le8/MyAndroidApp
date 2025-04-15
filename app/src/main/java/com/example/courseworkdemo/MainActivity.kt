package com.example.courseworkdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔔 Check and request notification permission
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
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController,viewModel)
                    }
                    composable("task_creation") {
                        TaskCreationScreen(navController, viewModel)
                    }
                    composable("task_list") {
                        TaskListScreen(navController, viewModel)
                    }
                }
            }
        }
    }
}
