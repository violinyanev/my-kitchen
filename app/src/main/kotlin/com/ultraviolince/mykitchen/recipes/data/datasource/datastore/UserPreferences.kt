package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import android.util.Log
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

@Serializable
data class UserPreferences(
    val server: String? = null,
    val token: String? = null
)

object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        return try {
            val encryptedBytes = withContext(Dispatchers.IO) {
                input.use { it.readBytes() }
            }

            // Handle empty file case
            if (encryptedBytes.isEmpty()) {
                return defaultValue
            }

            val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
            val decryptedBytes = Crypto.decrypt(encryptedBytesDecoded)
            val decodedJsonString = decryptedBytes.decodeToString()
            Json.decodeFromString(decodedJsonString)
        } catch (e: Exception) {
            // Handle any decryption, Base64, or JSON parsing errors
            // This includes BadPaddingException, IllegalArgumentException, etc.
            // Common causes:
            // - Empty or corrupted preferences file
            // - Android Keystore key was regenerated (app reinstall, key rotation, etc.)
            // - Invalid Base64 encoding
            // - Malformed encrypted data
            Log.w("UserPreferencesSerializer", "Failed to decrypt user preferences, using defaults", e)
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }
}
