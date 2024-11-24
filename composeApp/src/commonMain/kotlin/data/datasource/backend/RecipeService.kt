package data.datasource.backend

import data.datasource.backend.data.BackendRecipe
import data.datasource.backend.data.BackendRecipeResponse
import data.datasource.backend.data.LoginRequest
import data.datasource.backend.data.LoginResult
import data.datasource.backend.util.NetworkError
import data.datasource.backend.util.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

class RecipeService(private val ktor: HttpClient) {
    suspend fun getRecipes(): NetworkResult<List<BackendRecipe>, NetworkError> {
        val response =
            try {
                ktor.get("/recipes")
            } catch (e: UnresolvedAddressException) {
                return NetworkResult.Error(NetworkError.NO_INTERNET)
            } catch (e: SerializationException) {
                return NetworkResult.Error(NetworkError.SERIALIZATION)
            }

        return when (response.status.value) {
            in 200..299 -> {
                val recipes = response.body<List<BackendRecipe>>()
                NetworkResult.Success(recipes)
            }
            401 -> NetworkResult.Error(NetworkError.UNAUTHORIZED)
            409 -> NetworkResult.Error(NetworkError.CONFLICT)
            408 -> NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> NetworkResult.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> NetworkResult.Error(NetworkError.SERVER_ERROR)
            else -> {
                Log.e("${response.status}, $response")
                NetworkResult.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun createRecipe(recipeRequest: BackendRecipe): NetworkResult<BackendRecipeResponse, NetworkError> {
        val response =
            try {
                ktor.post("/recipes") {
                    contentType(ContentType.Application.Json)
                    setBody(recipeRequest)
                }
            } catch (e: UnresolvedAddressException) {
                return NetworkResult.Error(NetworkError.NO_INTERNET)
            } catch (e: SerializationException) {
                return NetworkResult.Error(NetworkError.SERIALIZATION)
            }

        return when (response.status.value) {
            in 200..299 -> {
                NetworkResult.Success(response.body<BackendRecipeResponse>())
            }
            400 -> NetworkResult.Error(NetworkError.UNKNOWN) // Implementation error in the client
            401 -> NetworkResult.Error(NetworkError.UNAUTHORIZED)
            409 -> NetworkResult.Error(NetworkError.CONFLICT)
            408 -> NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> NetworkResult.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> NetworkResult.Error(NetworkError.SERVER_ERROR)
            else -> {
                Log.e("Unknown error, code: ${response.status.value}")
                NetworkResult.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun deleteRecipe(recipeId: Long): NetworkResult<Unit, NetworkError> {
        val response =
            try {
                ktor.delete("/recipes/$recipeId")
            } catch (e: UnresolvedAddressException) {
                return NetworkResult.Error(NetworkError.NO_INTERNET)
            } catch (e: SerializationException) {
                return NetworkResult.Error(NetworkError.SERIALIZATION)
            }

        return when (response.status.value) {
            in 200..299 -> {
                NetworkResult.Success(Unit)
            }
            401 -> NetworkResult.Error(NetworkError.UNAUTHORIZED)
            409 -> NetworkResult.Error(NetworkError.CONFLICT)
            408 -> NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> NetworkResult.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> NetworkResult.Error(NetworkError.SERVER_ERROR)
            else -> NetworkResult.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun login(loginRequest: LoginRequest): NetworkResult<LoginResult, NetworkError> {
        val response =
            try {
                ktor.post("/users/login") {
                    contentType(ContentType.Application.Json)
                    setBody(loginRequest)
                }
            } catch (e: UnresolvedAddressException) {
                return NetworkResult.Error(NetworkError.NO_INTERNET)
            } catch (e: SerializationException) {
                return NetworkResult.Error(NetworkError.SERIALIZATION)
            } catch (e: Exception) {
                return NetworkResult.Error(NetworkError.SERVER_ERROR) // TODO Probably need different error handling
            }

        return when (response.status.value) {
            in 200..299 -> {
                val result = response.body<LoginResult>()
                NetworkResult.Success(result)
            }
            400 -> NetworkResult.Error(NetworkError.UNAUTHORIZED) // TODO Probably different error handling needed
            401 -> NetworkResult.Error(NetworkError.UNAUTHORIZED)
            409 -> NetworkResult.Error(NetworkError.CONFLICT)
            408 -> NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> NetworkResult.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> NetworkResult.Error(NetworkError.SERVER_ERROR)
            else -> {
                Log.e("${response.status}, $response")
                NetworkResult.Error(NetworkError.UNKNOWN)
            }
        }
    }
}
