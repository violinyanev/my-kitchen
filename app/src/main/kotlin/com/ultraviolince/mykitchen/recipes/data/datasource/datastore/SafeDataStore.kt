package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import android.content.Context
import androidx.datastore.dataStore

val Context.dataStore by dataStore(
    fileName = "user-preferences",
    serializer = UserPreferencesSerializer
)

class SafeDataStore(context: Context) {
    private val store = context.dataStore

    val preferences = store.data

    suspend fun write(server: String, token: String) {
        store.updateData {
            UserPreferences(
                server = server,
                token = token
            )
        }
    }
}
