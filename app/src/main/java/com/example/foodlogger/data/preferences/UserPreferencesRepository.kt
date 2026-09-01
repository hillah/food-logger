package com.example.foodlogger.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "food_logger_settings")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        private val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        private val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
        const val DEFAULT_MODEL = "gemini-flash-lite-latest"
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_KEY] ?: ""
    }

    val geminiModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_MODEL] ?: DEFAULT_MODEL
    }

    suspend fun saveGeminiApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GEMINI_API_KEY] = apiKey.trim()
        }
    }

    suspend fun saveGeminiModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GEMINI_MODEL] = model
        }
    }
}
