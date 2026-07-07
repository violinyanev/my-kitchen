package com.ultraviolince.mykitchen.data.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences

class DesktopCredentialsStore : CredentialsStore {
    private val prefs = Preferences.userNodeForPackage(DesktopCredentialsStore::class.java)

    private data class Credentials(val token: String, val serverUrl: String)

    private val state = MutableStateFlow(loadFromPrefs())

    private fun loadFromPrefs(): Credentials? {
        val token = prefs.get(KEY_TOKEN, null) ?: return null
        val serverUrl = prefs.get(KEY_SERVER_URL, null) ?: return null
        return Credentials(token, serverUrl)
    }

    override fun observeToken(): Flow<String?> = state.map { it?.token }
    override fun observeServerUrl(): Flow<String?> = state.map { it?.serverUrl }
    override suspend fun getToken(): String? = state.value?.token
    override suspend fun getServerUrl(): String? = state.value?.serverUrl

    override suspend fun saveCredentials(token: String, serverUrl: String) {
        prefs.put(KEY_TOKEN, token)
        prefs.put(KEY_SERVER_URL, serverUrl)
        state.value = Credentials(token, serverUrl)
    }

    override suspend fun clearCredentials() {
        prefs.remove(KEY_TOKEN)
        prefs.remove(KEY_SERVER_URL)
        state.value = null
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_SERVER_URL = "server_url"
    }
}
