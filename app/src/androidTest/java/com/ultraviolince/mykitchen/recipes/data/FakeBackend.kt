package com.ultraviolince.mykitchen.recipes.data

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FakeBackend {
    companion object {
        val server = "http://10.0.2.2:5000"
        val testUser = "test@user.com"
        val testPassword = "TestPassword"
        
        /**
         * Clear all recipes for the test user from the backend.
         * This is used for test isolation.
         */
        fun clearUserRecipes(): Boolean {
            return try {
                val token = loginAndGetToken() ?: return false
                
                val url = URL("$server/recipes/clear")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 5000 // 5 second timeout
                connection.readTimeout = 5000    // 5 second timeout
                
                val responseCode = connection.responseCode
                connection.disconnect()
                responseCode in 200..299
            } catch (e: Exception) {
                // Backend might not be running or reachable - that's ok for some tests
                android.util.Log.w("FakeBackend", "Failed to clear user recipes: ${e.message}")
                false
            }
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
                connection.doOutput = true
                connection.connectTimeout = 5000 // 5 second timeout
                connection.readTimeout = 5000    // 5 second timeout
                
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
                    responseJson.getJSONObject("data").getString("token")
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
