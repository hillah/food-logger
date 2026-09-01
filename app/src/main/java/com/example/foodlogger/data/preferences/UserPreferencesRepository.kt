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
        private val KEY_USER_AGE_GROUP = stringPreferencesKey("user_age_group")
        private val KEY_USER_GENDER = stringPreferencesKey("user_gender")
        private val KEY_USER_ACTIVITY_LEVEL = stringPreferencesKey("user_activity_level")

        const val DEFAULT_MODEL = "gemini-flash-lite-latest"
        const val DEFAULT_AGE_GROUP = "40s"
        const val DEFAULT_GENDER = "male"
        const val DEFAULT_ACTIVITY_LEVEL = "low"
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_KEY] ?: ""
    }

    val geminiModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_MODEL] ?: DEFAULT_MODEL
    }

    val userAgeGroup: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_AGE_GROUP] ?: DEFAULT_AGE_GROUP
    }

    val userGender: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_GENDER] ?: DEFAULT_GENDER
    }

    val userActivityLevel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ACTIVITY_LEVEL] ?: DEFAULT_ACTIVITY_LEVEL
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

    suspend fun saveUserProfile(ageGroup: String, gender: String, activityLevel: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_AGE_GROUP] = ageGroup
            preferences[KEY_USER_GENDER] = gender
            preferences[KEY_USER_ACTIVITY_LEVEL] = activityLevel
        }
    }
}
