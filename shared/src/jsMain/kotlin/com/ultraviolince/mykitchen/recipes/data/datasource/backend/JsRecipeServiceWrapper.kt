package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class JsRecipeServiceWrapper {
    private var client: HttpClient? = null
    private var baseUrl: String? = null
    
    fun configure(server: String) {
        baseUrl = server
        client = HttpClient(Js) {
            // Basic configuration for JS client
        }
    }
    
    suspend fun login(username: String, password: String): Boolean {
        return try {
            val client = this.client ?: return false
            val response = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"username": "$username", "password": "$password"}""")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            console.error("Login error:", e)
            false
        }
    }
    
    suspend fun getRecipes(): List<Map<String, Any>>? {
        return try {
            val client = this.client ?: return null
            val response = client.get("$baseUrl/recipes")
            if (response.status.isSuccess()) {
                // For now, return empty list - would need proper JSON parsing
                emptyList()
            } else null
        } catch (e: Exception) {
            console.error("Get recipes error:", e)
            null
        }
    }
    
    fun close() {
        client?.close()
        client = null
    }
}