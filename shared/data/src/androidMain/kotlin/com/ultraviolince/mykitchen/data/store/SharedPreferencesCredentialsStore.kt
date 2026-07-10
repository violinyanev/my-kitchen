package com.ultraviolince.mykitchen.data.store

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class SharedPreferencesCredentialsStore(context: Context) : CredentialsStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private data class Credentials(val token: String, val serverUrl: String)

    private val state = MutableStateFlow(loadFromPrefs())

    private fun loadFromPrefs(): Credentials? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val serverUrl = prefs.getString(KEY_SERVER_URL, null) ?: return null
        return Credentials(token, serverUrl)
    }

    override fun observeToken(): Flow<String?> = state.map { it?.token }
    override fun observeServerUrl(): Flow<String?> = state.map { it?.serverUrl }
    override suspend fun getToken(): String? = state.value?.token
    override suspend fun getServerUrl(): String? = state.value?.serverUrl

    override suspend fun saveCredentials(token: String, serverUrl: String) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_SERVER_URL, serverUrl).apply()
        state.value = Credentials(token, serverUrl)
    }

    override suspend fun clearCredentials() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_SERVER_URL).apply()
        state.value = null
    }

    companion object {
        private const val PREFS_NAME = "credentials"
        private const val KEY_TOKEN = "token"
        private const val KEY_SERVER_URL = "server_url"
    }
}
