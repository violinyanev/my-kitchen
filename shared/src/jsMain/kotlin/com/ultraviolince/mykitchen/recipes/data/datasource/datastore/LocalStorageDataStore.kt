package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

class LocalStorageDataStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val loginStateKey = "my_kitchen_login_state"
    private val serverKey = "my_kitchen_server"
    private val usernameKey = "my_kitchen_username"
    
    fun saveLoginState(state: LoginState) {
        when (state) {
            is LoginState.LoginSuccess -> {
                localStorage.setItem(loginStateKey, "success")
            }
            else -> {
                localStorage.removeItem(loginStateKey)
            }
        }
    }
    
    fun getLoginState(): LoginState {
        return when (localStorage.getItem(loginStateKey)) {
            "success" -> LoginState.LoginSuccess
            else -> LoginState.LoginEmpty
        }
    }
    
    fun saveServer(server: String) {
        localStorage.setItem(serverKey, server)
    }
    
    fun getServer(): String? {
        return localStorage.getItem(serverKey)
    }
    
    fun saveUsername(username: String) {
        localStorage.setItem(usernameKey, username)
    }
    
    fun getUsername(): String? {
        return localStorage.getItem(usernameKey)
    }
    
    fun clear() {
        localStorage.removeItem(loginStateKey)
        localStorage.removeItem(serverKey)
        localStorage.removeItem(usernameKey)
    }
}