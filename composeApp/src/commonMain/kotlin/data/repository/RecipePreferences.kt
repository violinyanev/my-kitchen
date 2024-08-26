package data.repository

import Platform
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking

class RecipePreferences(platform: Platform) {
    private val dataStore = platform.createDataStore()
    private val tokenKey = stringPreferencesKey("token")

    fun getLoginToken(): String? {
        return runBlocking {
            val preferences = dataStore.data.last()
            preferences[tokenKey]
        }
    }

    suspend fun storeLoginToken(token: String) {
        dataStore.edit { dataStore ->
            dataStore[tokenKey] = token
        }
    }
}
