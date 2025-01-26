package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.dataStore

val Context.dataStore by dataStore(
    fileName = "user-preferences",
    serializer = UserPreferencesSerializer
)

class SafeDataStore(context: Context) {
    private val store = context.dataStore

    val preferences = store.data

    suspend fun write(server: String, token: String) {
        Log.i("#data", "Storing preferences: server=$server")
        store.updateData {
            UserPreferences(
                server = server,
                token = token
            )
        }
    }
}
