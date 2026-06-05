package com.ultraviolince.mykitchen.data.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryCredentialsStore : CredentialsStore {
    private data class Credentials(val token: String, val serverUrl: String)
    private val state = MutableStateFlow<Credentials?>(null)

    override fun observeToken(): Flow<String?> = state.map { it?.token }
    override fun observeServerUrl(): Flow<String?> = state.map { it?.serverUrl }
    override suspend fun getToken(): String? = state.value?.token
    override suspend fun getServerUrl(): String? = state.value?.serverUrl

    override suspend fun saveCredentials(token: String, serverUrl: String) {
        state.value = Credentials(token, serverUrl)
    }

    override suspend fun clearCredentials() {
        state.value = null
    }
}
