package com.example.foodlogger.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodlogger.data.gemini.GeminiNutritionService
import com.example.foodlogger.data.healthconnect.HealthConnectManager
import com.example.foodlogger.data.model.NutrientDetails
import com.example.foodlogger.data.model.NutritionAnalysisResult
import com.example.foodlogger.data.preferences.UserPreferencesRepository
import com.example.foodlogger.ui.screens.MealCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class CurrentScreen {
    DASHBOARD,
    INPUT,
    PREVIEW,
    DAILY_SUMMARY
}

sealed interface UiState {
    object Idle : UiState
    data class Analyzing(val message: String = "Geminiで栄養素を解析中...") : UiState
    data class Preview(val result: NutritionAnalysisResult) : UiState
    data class Success(val message: String, val recordId: String = "") : UiState
    data class Error(val errorMessage: String) : UiState
}

class FoodLoggerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)
    private val geminiService = GeminiNutritionService()
    val healthConnectManager = HealthConnectManager(application)

    val geminiApiKey: StateFlow<String> = preferencesRepository.geminiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val geminiModel: StateFlow<String> = preferencesRepository.geminiModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesRepository.DEFAULT_MODEL)

    val userAgeGroup: StateFlow<String> = preferencesRepository.userAgeGroup
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesRepository.DEFAULT_AGE_GROUP)

    val userGender: StateFlow<String> = preferencesRepository.userGender
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesRepository.DEFAULT_GENDER)

    val userActivityLevel: StateFlow<String> = preferencesRepository.userActivityLevel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesRepository.DEFAULT_ACTIVITY_LEVEL)

    private val _currentScreen = MutableStateFlow(CurrentScreen.DASHBOARD)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Week navigation & Matrix records
    private val _currentWeekStart = MutableStateFlow(getSundayOfCurrentWeek(LocalDate.now()))
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart.asStateFlow()

    private val _weekRecordsMap = MutableStateFlow<Map<LocalDate, Map<Int, NutritionRecord>>>(emptyMap())
    val weekRecordsMap: StateFlow<Map<LocalDate, Map<Int, NutritionRecord>>> = _weekRecordsMap.asStateFlow()

    private val _previousWeekUnregisteredList = MutableStateFlow<List<Pair<LocalDate, MealCategory>>>(emptyList())
    val previousWeekUnregisteredList: StateFlow<List<Pair<LocalDate, MealCategory>>> = _previousWeekUnregisteredList.asStateFlow()

    // Daily Summary details
    private val _summaryTargetDate = MutableStateFlow(LocalDate.now())
    val summaryTargetDate: StateFlow<LocalDate> = _summaryTargetDate.asStateFlow()

    private val _summaryNutrients = MutableStateFlow(NutrientDetails())
    val summaryNutrients: StateFlow<NutrientDetails> = _summaryNutrients.asStateFlow()

    private val _summaryHasCompletedMainMeals = MutableStateFlow(false)
    val summaryHasCompletedMainMeals: StateFlow<Boolean> = _summaryHasCompletedMainMeals.asStateFlow()

    // Selected Target for Registration
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedMealCategory = MutableStateFlow(determineMealCategoryByTime(LocalTime.now()))
    val selectedMealCategory: StateFlow<MealCategory> = _selectedMealCategory.asStateFlow()

    // Input state
    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap: StateFlow<Bitmap?> = _selectedImageBitmap.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _hasHealthConnectPermission = MutableStateFlow(false)
    val hasHealthConnectPermission: StateFlow<Boolean> = _hasHealthConnectPermission.asStateFlow()

    init {
        checkHealthConnectPermissions()
    }

    fun checkHealthConnectPermissions() {
        viewModelScope.launch {
            val granted = healthConnectManager.hasPermissions()
            _hasHealthConnectPermission.value = granted
            if (granted) {
                loadWeekRecords()
            }
        }
    }

    fun goToPreviousWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
        loadWeekRecords()
    }

    fun goToNextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
        loadWeekRecords()
    }

    fun goToCurrentWeek() {
        _currentWeekStart.value = getSundayOfCurrentWeek(LocalDate.now())
        loadWeekRecords()
    }

    fun openMealInput(date: LocalDate, category: MealCategory) {
        _selectedDate.value = date
        _selectedMealCategory.value = category
        _selectedImageBitmap.value = null
        _inputText.value = ""
        _uiState.value = UiState.Idle
        _currentScreen.value = CurrentScreen.INPUT
    }

    fun openDailySummary(date: LocalDate) {
        _summaryTargetDate.value = date
        val dayRecords = _weekRecordsMap.value[date] ?: emptyMap()

        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalFat = 0.0
        var totalCarbs = 0.0
        var totalFiber = 0.0
        var totalSalt = 0.0
        var totalCalcium = 0.0
        var totalIron = 0.0
        var totalZinc = 0.0
        var totalMagnesium = 0.0
        var totalVitA = 0.0
        var totalVitC = 0.0
        var totalVitD = 0.0
        var totalVitE = 0.0
        var totalFolate = 0.0

        dayRecords.values.forEach { record ->
            totalCalories += record.energy?.inKilocalories ?: 0.0
            totalProtein += record.protein?.inGrams ?: 0.0
            totalFat += record.totalFat?.inGrams ?: 0.0
            totalCarbs += record.totalCarbohydrate?.inGrams ?: 0.0
            totalFiber += record.dietaryFiber?.inGrams ?: 0.0
            totalCalcium += record.calcium?.inGrams?.times(1000.0) ?: 0.0
            totalIron += record.iron?.inGrams?.times(1000.0) ?: 0.0
            totalZinc += record.zinc?.inGrams?.times(1000.0) ?: 0.0
            totalMagnesium += record.magnesium?.inGrams?.times(1000.0) ?: 0.0
            totalVitA += record.vitaminA?.inGrams?.times(1_000_000.0) ?: 0.0
            totalVitC += record.vitaminC?.inGrams?.times(1000.0) ?: 0.0
            totalVitD += record.vitaminD?.inGrams?.times(1_000_000.0) ?: 0.0
            totalVitE += record.vitaminE?.inGrams?.times(1000.0) ?: 0.0
            totalFolate += record.folate?.inGrams?.times(1_000_000.0) ?: 0.0
            // Salt equivalent from sodium
            val sodiumMg = record.sodium?.inGrams?.times(1000.0) ?: 0.0
            totalSalt += (sodiumMg * 2.54) / 1000.0
        }

        _summaryNutrients.value = NutrientDetails(
            caloriesKcal = totalCalories,
            proteinG = totalProtein,
            fatG = totalFat,
            carbohydrateG = totalCarbs,
            fiberG = totalFiber,
            saltEquivalentG = totalSalt,
            calciumMg = totalCalcium,
            ironMg = totalIron,
            zincMg = totalZinc,
            magnesiumMg = totalMagnesium,
            vitaminAMcg = totalVitA,
            vitaminCMg = totalVitC,
            vitaminDMcg = totalVitD,
            vitaminEMg = totalVitE,
            folateMcg = totalFolate
        )

        val hasBreakfast = dayRecords.containsKey(MealCategory.BREAKFAST.mealTypeConstant)
        val hasLunch = dayRecords.containsKey(MealCategory.LUNCH.mealTypeConstant)
        val hasDinner = dayRecords.containsKey(MealCategory.DINNER.mealTypeConstant)
        _summaryHasCompletedMainMeals.value = hasBreakfast && hasLunch && hasDinner

        _currentScreen.value = CurrentScreen.DAILY_SUMMARY
    }

    fun onCategoryChanged(category: MealCategory) {
        _selectedMealCategory.value = category
    }

    fun onDateChanged(date: LocalDate) {
        _selectedDate.value = date
    }

    fun backToDashboard() {
        _currentScreen.value = CurrentScreen.DASHBOARD
        _selectedImageBitmap.value = null
        _inputText.value = ""
        _uiState.value = UiState.Idle
        loadWeekRecords()
    }

    fun onImageSelected(bitmap: Bitmap?) {
        _selectedImageBitmap.value = bitmap
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun saveSettings(apiKey: String, model: String, ageGroup: String, gender: String, activityLevel: String) {
        viewModelScope.launch {
            preferencesRepository.saveGeminiApiKey(apiKey)
            preferencesRepository.saveGeminiModel(model)
            preferencesRepository.saveUserProfile(ageGroup, gender, activityLevel)
        }
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            preferencesRepository.saveGeminiApiKey(apiKey)
        }
    }

    fun saveModel(model: String) {
        viewModelScope.launch {
            preferencesRepository.saveGeminiModel(model)
        }
    }

    /**
     * Load records for the currently displayed week and check for previous week's missing meals.
     */
    fun loadWeekRecords() {
        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()
            val weekStart = _currentWeekStart.value
            val weekEnd = weekStart.plusDays(7)

            val startTime = weekStart.atStartOfDay(zoneId).toInstant()
            val endTime = weekEnd.atStartOfDay(zoneId).toInstant()

            val recordsResult = healthConnectManager.readNutritionRecords(startTime, endTime)
            recordsResult.onSuccess { records ->
                val map = mutableMapOf<LocalDate, MutableMap<Int, NutritionRecord>>()
                records.forEach { record ->
                    val localDate = record.startTime.atZone(zoneId).toLocalDate()
                    val dayMap = map.getOrPut(localDate) { mutableMapOf() }
                    dayMap[record.mealType] = record
                }
                _weekRecordsMap.value = map
            }

            // Check previous week missing meals (Breakfast, Lunch, Dinner only)
            val prevWeekStart = weekStart.minusWeeks(1)
            val prevStartTime = prevWeekStart.atStartOfDay(zoneId).toInstant()
            val prevEndTime = weekStart.atStartOfDay(zoneId).toInstant()

            val prevRecordsResult = healthConnectManager.readNutritionRecords(prevStartTime, prevEndTime)
            prevRecordsResult.onSuccess { prevRecords ->
                val prevMap = mutableMapOf<LocalDate, MutableSet<Int>>()
                prevRecords.forEach { record ->
                    val localDate = record.startTime.atZone(zoneId).toLocalDate()
                    prevMap.getOrPut(localDate) { mutableSetOf() }.add(record.mealType)
                }

                val missingList = mutableListOf<Pair<LocalDate, MealCategory>>()
                val targetCategories = listOf(MealCategory.BREAKFAST, MealCategory.LUNCH, MealCategory.DINNER)

                for (i in 0..6) {
                    val date = prevWeekStart.plusDays(i.toLong())
                    val registeredTypes = prevMap[date] ?: emptySet()
                    targetCategories.forEach { category ->
                        if (!registeredTypes.contains(category.mealTypeConstant)) {
                            missingList.add(date to category)
                        }
                    }
                }
                _previousWeekUnregisteredList.value = missingList
            }
        }
    }

    /**
     * Record skipped meal (fasting / 0 kcal) and return to dashboard.
     */
    fun recordSkippedMeal() {
        viewModelScope.launch {
            _uiState.value = UiState.Analyzing("食事なし（欠食）を記録中...")
            val targetDate = _selectedDate.value
            val category = _selectedMealCategory.value

            val result = healthConnectManager.insertSkippedMealRecord(targetDate, category.key)
            result.onSuccess {
                _uiState.value = UiState.Success("${category.label}を「食事なし」として記録しました。")
                backToDashboard()
            }.onFailure { e ->
                _uiState.value = UiState.Error("記録に失敗しました: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Analyze meal with Gemini and automatically save to Health Connect.
     */
    fun analyzeMealAndAutoSave() {
        val apiKey = geminiApiKey.value
        val model = geminiModel.value
        val bitmap = _selectedImageBitmap.value
        val prompt = _inputText.value
        val targetDate = _selectedDate.value
        val category = _selectedMealCategory.value

        if (apiKey.isBlank()) {
            _uiState.value = UiState.Error("Gemini APIキーが設定されていません。右上の設定アイコン（⚙️）からAPIキーを入力してください。")
            return
        }

        if (bitmap == null && prompt.isBlank()) {
            _uiState.value = UiState.Error("写真を選択するか、食事内容のテキストを入力してください。")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Analyzing("Gemini AI で栄養素を解析中...")
            val result = geminiService.analyzeMeal(
                apiKey = apiKey,
                modelName = model,
                promptText = prompt,
                bitmap = bitmap
            )

            result.onSuccess { analysisResult ->
                _uiState.value = UiState.Analyzing("Health Connect に自動保存中...")
                val finalResult = analysisResult.copy(mealType = category.key)

                // Auto-save to Health Connect
                val upsertResult = healthConnectManager.upsertNutritionRecord(
                    analysisResult = finalResult,
                    targetDate = targetDate,
                    mealTypeString = category.key
                )

                upsertResult.onSuccess {
                    // Stay on Preview screen to allow user to review recognised content & AI notes
                    _uiState.value = UiState.Preview(finalResult)
                }.onFailure { error ->
                    _uiState.value = UiState.Error("Health Connectへの保存に失敗しました: ${error.localizedMessage}")
                }
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.localizedMessage ?: "栄養素の解析に失敗しました。")
            }
        }
    }

    fun updateAnalysisResult(updated: NutritionAnalysisResult) {
        if (_uiState.value is UiState.Preview) {
            _uiState.value = UiState.Preview(updated)
        }
    }

    fun saveToHealthConnect(analysisResult: NutritionAnalysisResult) {
        viewModelScope.launch {
            _uiState.value = UiState.Analyzing("Health Connect に書き込み中...")
            val targetDate = _selectedDate.value
            val category = _selectedMealCategory.value

            val result = healthConnectManager.upsertNutritionRecord(
                analysisResult = analysisResult,
                targetDate = targetDate,
                mealTypeString = category.key
            )
            result.onSuccess { recordId ->
                _uiState.value = UiState.Success("Health Connect に栄養データを記録しました！", recordId)
                backToDashboard()
            }.onFailure { error ->
                _uiState.value = UiState.Error("Health Connect への保存に失敗しました: ${error.localizedMessage}")
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = UiState.Idle
    }

    companion object {
        private fun getSundayOfCurrentWeek(date: LocalDate): LocalDate {
            return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        }

        private fun determineMealCategoryByTime(time: LocalTime): MealCategory {
            return when (time.hour) {
                in 4..10 -> MealCategory.BREAKFAST
                in 11..14 -> MealCategory.LUNCH
                in 15..17 -> MealCategory.SNACK
                in 18..22 -> MealCategory.DINNER
                else -> MealCategory.OTHER
            }
        }
    }
}
