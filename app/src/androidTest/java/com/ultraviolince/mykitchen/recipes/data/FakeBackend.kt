package com.ultraviolince.mykitchen.recipes.data

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FakeBackend {
    companion object {
        // Try different server addresses for different environments
        // In CI environments, the server is started on localhost, so we try that first
        private val possibleServers = listOf(
            "http://localhost:5000",   // CI environments and local development
            "http://10.0.2.2:5000",   // Standard Android emulator address
            "http://127.0.0.1:5000"   // Fallback local address
        )
        
        val testUser = "test@user.com"
        val testPassword = "TestPassword"
        
        // Determine the correct server address by testing connectivity
        val server: String by lazy {
            findWorkingServer() ?: possibleServers[0] // Default to emulator address
        }
        
        /**
         * Find a working server address by testing connectivity to /health endpoint
         */
        private fun findWorkingServer(): String? {
            // In CI environments, wait a bit longer for server to be ready
            val maxRetries = 3
            val retryDelay = 2000L // 2 seconds between retries
            
            repeat(maxRetries) { retry ->
                for (serverUrl in possibleServers) {
                    try {
                        val url = URL("$serverUrl/health")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 15000 // Increased timeout for CI environments 
                        connection.readTimeout = 15000
                        connection.setRequestProperty("User-Agent", "MyKitchen-Test")
                        
                        val responseCode = connection.responseCode
                        connection.disconnect()
                        
                        if (responseCode == 200) {
                            android.util.Log.i("FakeBackend", "Found working server at: $serverUrl (retry $retry)")
                            return serverUrl
                        } else {
                            android.util.Log.d("FakeBackend", "Server $serverUrl returned code $responseCode (retry $retry)")
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("FakeBackend", "Server $serverUrl not reachable (retry $retry): ${e.message}")
                    }
                }
                
                // Wait before next retry attempt, except on last retry
                if (retry < maxRetries - 1) {
                    android.util.Log.i("FakeBackend", "Waiting ${retryDelay}ms before next retry...")
                    Thread.sleep(retryDelay)
                }
            }
            
            android.util.Log.w("FakeBackend", "No working server found after $maxRetries retries, using default")
            return null
        }
        
        /**
         * Clear all recipes for the test user from the backend.
         * This is used for test isolation.
         */
        fun clearUserRecipes(): Boolean {
            // Retry up to 5 times for CI environment reliability
            repeat(5) { attempt ->
                try {
                    val token = loginAndGetToken() ?: return false
                    
                    val url = URL("$server/recipes/clear")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "DELETE"
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("User-Agent", "MyKitchen-Test")
                    connection.connectTimeout = 15000 // Increased timeout for CI environments
                    connection.readTimeout = 15000    // Increased timeout for CI environments
                    
                    val responseCode = connection.responseCode
                    connection.disconnect()
                    
                    if (responseCode in 200..299) {
                        android.util.Log.i("FakeBackend", "Successfully cleared user recipes (attempt ${attempt + 1})")
                        return true
                    } else {
                        android.util.Log.w("FakeBackend", "Clear recipes failed with response code: $responseCode (attempt ${attempt + 1})")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("FakeBackend", "Attempt ${attempt + 1}: Failed to clear user recipes: ${e.message}")
                    if (attempt == 4) {
                        // Last attempt failed
                        android.util.Log.e("FakeBackend", "All attempts to clear user recipes failed")
                    } else {
                        // Wait before retry with exponential backoff
                        val delay = (attempt + 1) * 1000L // 1s, 2s, 3s, 4s
                        android.util.Log.i("FakeBackend", "Waiting ${delay}ms before retry...")
                        Thread.sleep(delay)
                    }
                }
            }
            return false
        }
        
        private fun loginAndGetToken(): String? {
            return try {
                val json = JSONObject().apply {
                    put("email", testUser)
                    put("password", testPassword)
                }
                
                val url = URL("$server/users/login")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("User-Agent", "MyKitchen-Test")
                connection.doOutput = true
                connection.connectTimeout = 15000 // Increased timeout for CI environments
                connection.readTimeout = 15000    // Increased timeout for CI environments
                
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(json.toString())
                writer.flush()
                writer.close()
                
                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()
                    
                    val responseJson = JSONObject(response)
                    val token = responseJson.getJSONObject("data").getString("token")
                    android.util.Log.i("FakeBackend", "Login successful")
                    token
                } else {
                    android.util.Log.w("FakeBackend", "Login failed with response code: $responseCode")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.w("FakeBackend", "Login failed: ${e.message}")
                null
            }
        }
    }
}
