package com.ultraviolince.mykitchen.data.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import platform.Foundation.NSUserDefaults

class IosCredentialsStore : CredentialsStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    private data class Credentials(val token: String, val serverUrl: String)

    private val state = MutableStateFlow(loadFromDefaults())

    private fun loadFromDefaults(): Credentials? {
        val token = defaults.stringForKey(KEY_TOKEN) ?: return null
        val serverUrl = defaults.stringForKey(KEY_SERVER_URL) ?: return null
        return Credentials(token, serverUrl)
    }

    override fun observeToken(): Flow<String?> = state.map { it?.token }
    override fun observeServerUrl(): Flow<String?> = state.map { it?.serverUrl }
    override suspend fun getToken(): String? = state.value?.token
    override suspend fun getServerUrl(): String? = state.value?.serverUrl

    override suspend fun saveCredentials(token: String, serverUrl: String) {
        defaults.setObject(token, KEY_TOKEN)
        defaults.setObject(serverUrl, KEY_SERVER_URL)
        state.value = Credentials(token, serverUrl)
    }

    override suspend fun clearCredentials() {
        defaults.removeObjectForKey(KEY_TOKEN)
        defaults.removeObjectForKey(KEY_SERVER_URL)
        state.value = null
    }

    companion object {
        private const val KEY_TOKEN = "credentials_token"
        private const val KEY_SERVER_URL = "credentials_server_url"
    }
}
