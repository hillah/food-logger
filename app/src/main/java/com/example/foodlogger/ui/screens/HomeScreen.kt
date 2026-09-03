package com.example.foodlogger.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary
import com.example.foodlogger.viewmodel.CurrentScreen
import com.example.foodlogger.viewmodel.FoodLoggerViewModel
import com.example.foodlogger.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FoodLoggerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedBitmap by viewModel.selectedImageBitmap.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val hasPermission by viewModel.hasHealthConnectPermission.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val geminiModel by viewModel.geminiModel.collectAsState()

    val currentWeekStart by viewModel.currentWeekStart.collectAsState()
    val weekRecordsMap by viewModel.weekRecordsMap.collectAsState()
    val previousWeekUnregisteredList by viewModel.previousWeekUnregisteredList.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedMealCategory by viewModel.selectedMealCategory.collectAsState()

    val userAgeGroup by viewModel.userAgeGroup.collectAsState()
    val userGender by viewModel.userGender.collectAsState()
    val userActivityLevel by viewModel.userActivityLevel.collectAsState()
    val summaryTargetDate by viewModel.summaryTargetDate.collectAsState()
    val summaryNutrients by viewModel.summaryNutrients.collectAsState()
    val summaryHasCompletedMainMeals by viewModel.summaryHasCompletedMainMeals.collectAsState()
    val summaryDayRecords by viewModel.summaryDayRecords.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Health Connect Permission Launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.checkHealthConnectPermissions()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.errorMessage)
            }
            else -> {}
        }
    }

    // Handle Android system back button/gesture
    androidx.activity.compose.BackHandler(enabled = currentScreen != CurrentScreen.DASHBOARD) {
        viewModel.backToDashboard()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Food Logger",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Health Connect permission banner if not granted
                if (!hasPermission) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Health Connect 権限が必要です",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = "栄養素データの読み書きを行うため、Health Connect権限を許可してください。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = {
                                    requestPermissionLauncher.launch(viewModel.healthConnectManager.permissions)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("権限を許可する")
                            }
                        }
                    }
                }

                // API Key Missing Warning Banner
                if (geminiApiKey.isBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gemini API キー未設定",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "AI解析を利用するにはAPIキーを設定してください",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Button(
                                onClick = { showSettingsDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("設定")
                            }
                        }
                    }
                }

                // Main Content Switching based on currentScreen and uiState
                when {
                    uiState is UiState.Preview -> {
                        val state = uiState as UiState.Preview
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            NutritionPreviewCard(
                                result = state.result,
                                onResultChanged = { updated -> viewModel.updateAnalysisResult(updated) },
                                onSaveToHealthConnect = { record -> viewModel.saveToHealthConnect(record) },
                                onBackToDashboard = { viewModel.backToDashboard() }
                            )
                        }
                    }
                    currentScreen == CurrentScreen.INPUT -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            InputSection(
                                targetDate = selectedDate,
                                selectedCategory = selectedMealCategory,
                                selectedBitmap = selectedBitmap,
                                inputText = inputText,
                                onImageSelected = { bitmap -> viewModel.onImageSelected(bitmap) },
                                onInputTextChanged = { text -> viewModel.onInputTextChanged(text) },
                                onAnalyzeClick = { viewModel.analyzeMealAndAutoSave() },
                                onSkipMealClick = { viewModel.recordSkippedMeal() },
                                onBackClick = { viewModel.backToDashboard() }
                            )
                        }
                    }
                    currentScreen == CurrentScreen.DAILY_SUMMARY -> {
                        DailySummaryScreen(
                            targetDate = summaryTargetDate,
                            dailyTotalNutrients = summaryNutrients,
                            hasCompletedMainMeals = summaryHasCompletedMainMeals,
                            dayRecords = summaryDayRecords,
                            ageGroup = userAgeGroup,
                            gender = userGender,
                            activityLevel = userActivityLevel,
                            onBackClick = { viewModel.backToDashboard() }
                        )
                    }
                    else -> {
                        // DASHBOARD
                        WeeklyDashboardScreen(
                            currentWeekStart = currentWeekStart,
                            weekRecordsMap = weekRecordsMap,
                            previousWeekUnregisteredList = previousWeekUnregisteredList,
                            ageGroup = userAgeGroup,
                            gender = userGender,
                            activityLevel = userActivityLevel,
                            onPreviousWeekClick = { viewModel.goToPreviousWeek() },
                            onNextWeekClick = { viewModel.goToNextWeek() },
                            onCurrentWeekClick = { viewModel.goToCurrentWeek() },
                            onCellClick = { date, category -> viewModel.openMealInput(date, category) },
                            onDailySummaryClick = { date -> viewModel.openDailySummary(date) },
                            onRefresh = { viewModel.loadWeekRecords() }
                        )
                    }
                }
            }

            // Loading overlay
            if (uiState is UiState.Analyzing) {
                val analyzingState = uiState as UiState.Analyzing
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = EmeraldGreenPrimary)
                            Text(
                                text = analyzingState.message,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentApiKey = geminiApiKey,
            currentModel = geminiModel,
            currentAgeGroup = userAgeGroup,
            currentGender = userGender,
            currentActivityLevel = userActivityLevel,
            onSave = { apiKey, model, ageGroup, gender, activityLevel ->
                viewModel.saveSettings(apiKey, model, ageGroup, gender, activityLevel)
                Toast.makeText(context, "設定を保存しました", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
