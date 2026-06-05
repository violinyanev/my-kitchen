package com.ultraviolince.mykitchen.data.store

import kotlinx.coroutines.flow.Flow

interface CredentialsStore {
    fun observeToken(): Flow<String?>
    fun observeServerUrl(): Flow<String?>
    suspend fun getToken(): String?
    suspend fun getServerUrl(): String?
    suspend fun saveCredentials(token: String, serverUrl: String)
    suspend fun clearCredentials()
}
