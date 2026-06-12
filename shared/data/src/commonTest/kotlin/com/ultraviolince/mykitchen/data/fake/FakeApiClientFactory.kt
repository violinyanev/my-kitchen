package com.ultraviolince.mykitchen.data.fake

import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.remote.dto.RecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Creates a RecipeApiClient backed by a configurable MockEngine. */
fun buildMockApiClient(
    loginToken: String = "test-token",
    remoteRecipes: List<RecipeDto> = emptyList(),
    loginSuccess: Boolean = true,
): RecipeApiClient {
    val json = Json { ignoreUnknownKeys = true }
    val engine = MockEngine { request ->
        val path = request.url.encodedPath
        when {
            path.endsWith("/users/login") -> {
                if (loginSuccess) {
                    respond(
                        content = """{"token":"$loginToken"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else {
                    respond(
                        content = """{"error":"unauthorized"}""",
                        status = HttpStatusCode.Unauthorized,
                    )
                }
            }
            path.endsWith("/recipes") && request.method.value == "GET" -> {
                respond(
                    content = json.encodeToString(remoteRecipes),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            else -> {
                respond(
                    content = """{"id":"new","title":"","content":"","created_at":0,"updated_at":0}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }
    }
    val client = HttpClient(engine) {
        install(ContentNegotiation) { json(json) }
    }
    return RecipeApiClient(client)
}
