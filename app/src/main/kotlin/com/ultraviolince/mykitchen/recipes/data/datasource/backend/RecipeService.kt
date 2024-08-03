package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipeResponse
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginResult
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result as Result

@SuppressWarnings("MagicNumber")
class RecipeService(private val ktor: HttpClient) {

    suspend fun getRecipes(): Result<List<BackendRecipe>, NetworkError> {
        val response = try {
            ktor.get("/recipes")
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                val recipes = response.body<List<BackendRecipe>>()
                Result.Success(recipes)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Log.e("#network", "${response.status}, $response")
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun createRecipe(recipeRequest: BackendRecipe): Result<BackendRecipeResponse, NetworkError> {
        val response = try {
            ktor.post("/recipes") {
                contentType(ContentType.Application.Json)
                setBody(recipeRequest)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        Log.e("#network", "DEBUG " + response.bodyAsText())

        return when (response.status.value) {
            in 200..299 -> {
                Result.Success(response.body<BackendRecipeResponse>())
            }
            400 -> Result.Error(NetworkError.UNKNOWN)  // Implementation error in the client
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Log.e("#network", "Unknown error, code: ${response.status.value}")
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun deleteRecipe(recipeId: Long): Result<Unit, NetworkError> {
        val response = try {
            ktor.delete("/recipes/$recipeId")
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                Result.Success(Unit)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun login(loginRequest: LoginRequest): Result<LoginResult, NetworkError> {
        val response = try {
            ktor.post("/users/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        } catch (e: ConnectException) {
            return Result.Error(NetworkError.SERVER_ERROR) // TODO Probably need different error handling
        }

        return when (response.status.value) {
            in 200..299 -> {
                val result = response.body<LoginResult>()
                Result.Success(result)
            }
            400 -> Result.Error(NetworkError.UNAUTHORIZED) // TODO Probably different error handling needed
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Log.e("#network", "${response.status}, $response")
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }
}
