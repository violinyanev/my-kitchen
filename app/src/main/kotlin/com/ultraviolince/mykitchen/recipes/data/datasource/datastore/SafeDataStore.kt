package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.InjectedParam

val Context.dataStore by dataStore(
    fileName = "user-preferences",
    serializer = UserPreferencesSerializer
)

class SafeDataStore(@InjectedParam context: Context) {
    private val store = context.dataStore

    val preferences: Flow<UserPreferences> = store.data
        .catch { exception ->
            // Additional safety net for any exceptions that might escape the serializer
            // This includes BadPaddingException, IOException, and other unexpected errors
            Log.w("SafeDataStore", "Error reading preferences, using defaults", exception)
            emit(UserPreferences())
        }

    suspend fun write(server: String, token: String) {
        Log.i("#data", "Storing preferences: server=$server")
        try {
            store.updateData {
                UserPreferences(
                    server = server,
                    token = token
                )
            }
        } catch (exception: Exception) {
            // Handle any write errors gracefully
            Log.e("SafeDataStore", "Error writing preferences", exception)
            // Optionally, you could emit an error state or retry logic here
            throw exception
        }
    }
}
