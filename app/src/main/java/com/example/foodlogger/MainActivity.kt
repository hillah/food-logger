package com.example.foodlogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.foodlogger.ui.screens.HomeScreen
import com.example.foodlogger.ui.theme.FoodLoggerTheme
import com.example.foodlogger.viewmodel.FoodLoggerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FoodLoggerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FoodLoggerTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check health connect permissions on resume
        viewModel.checkHealthConnectPermissions()
    }
}
