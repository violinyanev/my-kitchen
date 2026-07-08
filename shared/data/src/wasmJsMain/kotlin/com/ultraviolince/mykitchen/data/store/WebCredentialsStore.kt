package com.ultraviolince.mykitchen.data.store

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class WebCredentialsStore : CredentialsStore {
    private data class Credentials(val token: String, val serverUrl: String)

    private val state = MutableStateFlow(loadFromStorage())

    private fun loadFromStorage(): Credentials? {
        val token = localStorage.getItem(KEY_TOKEN) ?: return null
        val serverUrl = localStorage.getItem(KEY_SERVER_URL) ?: return null
        return Credentials(token, serverUrl)
    }

    override fun observeToken(): Flow<String?> = state.map { it?.token }
    override fun observeServerUrl(): Flow<String?> = state.map { it?.serverUrl }
    override suspend fun getToken(): String? = state.value?.token
    override suspend fun getServerUrl(): String? = state.value?.serverUrl

    override suspend fun saveCredentials(token: String, serverUrl: String) {
        localStorage.setItem(KEY_TOKEN, token)
        localStorage.setItem(KEY_SERVER_URL, serverUrl)
        state.value = Credentials(token, serverUrl)
    }

    override suspend fun clearCredentials() {
        localStorage.removeItem(KEY_TOKEN)
        localStorage.removeItem(KEY_SERVER_URL)
        state.value = null
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_SERVER_URL = "server_url"
    }
}
