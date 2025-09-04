package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.browser.localStorage

@Serializable
data class UserPreferences(
    val server: String? = null,
    val token: String? = null
)

/**
 * JavaScript/Web implementation of SafeDataStore using localStorage
 */
class LocalStorageDataStore {
    private companion object {
        private const val USER_PREFERENCES_KEY = "my_kitchen_user_preferences"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val _preferencesFlow = MutableStateFlow(UserPreferences())

    val preferences: Flow<UserPreferences> = _preferencesFlow.asStateFlow()

    init {
        loadPreferencesFromStorage()
    }

    suspend fun write(server: String, token: String) {
        console.log("Storing preferences: server=$server")
        val prefs = UserPreferences(
            server = server.takeIf { it.isNotBlank() },
            token = token.takeIf { it.isNotBlank() }
        )
        
        savePreferencesToStorage(prefs)
        _preferencesFlow.value = prefs
    }

    private fun loadPreferencesFromStorage() {
        try {
            val prefsJson = localStorage.getItem(USER_PREFERENCES_KEY)
            if (prefsJson != null) {
                val prefs = json.decodeFromString<UserPreferences>(prefsJson)
                _preferencesFlow.value = prefs
            }
        } catch (e: Exception) {
            console.error("Failed to load user preferences from localStorage:", e)
            _preferencesFlow.value = UserPreferences()
        }
    }

    private fun savePreferencesToStorage(preferences: UserPreferences) {
        try {
            val prefsJson = json.encodeToString(preferences)
            localStorage.setItem(USER_PREFERENCES_KEY, prefsJson)
        } catch (e: Exception) {
            console.error("Failed to save user preferences to localStorage:", e)
        }
    }
}