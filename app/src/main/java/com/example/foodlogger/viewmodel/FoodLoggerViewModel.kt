package com.example.foodlogger.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodlogger.data.gemini.GeminiNutritionService
import com.example.foodlogger.data.healthconnect.HealthConnectManager
import com.example.foodlogger.data.model.NutritionAnalysisResult
import com.example.foodlogger.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

sealed interface UiState {
    object Idle : UiState
    data class Analyzing(val message: String = "Geminiで栄養素を解析中...") : UiState
    data class Preview(val result: NutritionAnalysisResult) : UiState
    data class Success(val message: String, val recordId: String) : UiState
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

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
            _hasHealthConnectPermission.value = healthConnectManager.hasPermissions()
        }
    }

    fun onImageSelected(bitmap: Bitmap?) {
        _selectedImageBitmap.value = bitmap
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
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

    fun analyzeMeal() {
        val apiKey = geminiApiKey.value
        val model = geminiModel.value
        val bitmap = _selectedImageBitmap.value
        val prompt = _inputText.value

        if (apiKey.isBlank()) {
            _uiState.value = UiState.Error("Gemini APIキーが設定されていません。右上の設定アイコンからAPIキーを入力してください。")
            return
        }

        if (bitmap == null && prompt.isBlank()) {
            _uiState.value = UiState.Error("写真を選択するか、食事内容のテキストを入力してください。")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Analyzing()
            val result = geminiService.analyzeMeal(
                apiKey = apiKey,
                modelName = model,
                promptText = prompt,
                bitmap = bitmap
            )

            result.onSuccess { analysisResult ->
                _uiState.value = UiState.Preview(analysisResult)
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
            val result = healthConnectManager.insertNutritionRecord(analysisResult, Instant.now())
            result.onSuccess { recordId ->
                _uiState.value = UiState.Success("Health Connect に栄養データを正常に記録しました！", recordId)
                // Clear input after successful save
                _selectedImageBitmap.value = null
                _inputText.value = ""
            }.onFailure { error ->
                _uiState.value = UiState.Error("Health Connect への保存に失敗しました: ${error.localizedMessage}")
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = UiState.Idle
    }
}
