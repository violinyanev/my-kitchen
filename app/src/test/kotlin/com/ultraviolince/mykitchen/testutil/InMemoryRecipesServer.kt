package com.ultraviolince.mykitchen.testutil

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginResult
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginResultData
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Simple in-memory Ktor MockEngine-based fake server to back UI tests.
 * Stores recipes in memory and supports login + basic REST endpoints used by the app.
 */
class InMemoryRecipesServer {
    private val json = Json { ignoreUnknownKeys = true }

    private val idGenerator = AtomicLong(1)
    private val recipes: MutableMap<Long, BackendRecipe> = ConcurrentHashMap()
    private val token: String = "test-token"

    fun reset() {
        recipes.clear()
        idGenerator.set(1)
    }

    fun seed(vararg items: BackendRecipe) {
        items.forEach { item ->
            val id = item.id
            val toStore = if (id <= 0) item.copy(id = idGenerator.getAndIncrement()) else item
            recipes[toStore.id] = toStore
        }
    }

    val engine: HttpClientEngine = MockEngine { request ->
        val path = request.url.encodedPath
        val method = request.method

        fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

        fun isAuthorized(): Boolean {
            val header = request.headers[HttpHeaders.Authorization] ?: return false
            return header == "Bearer $token"
        }

        when {
            method == HttpMethod.Post && path == "/users/login" -> {
                // Always succeed login and return a fixed token
                val body = json.encodeToString(LoginResult(LoginResultData(username = "user", token = token)))
                respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders())
            }

            method == HttpMethod.Get && path == "/recipes" -> {
                if (!isAuthorized()) {
                    respond("Unauthorized", HttpStatusCode.Unauthorized)
                } else {
                    val list = recipes.values.sortedBy { it.id }
                    respond(json.encodeToString(list), HttpStatusCode.OK, jsonHeaders())
                }
            }

            method == HttpMethod.Post && path == "/recipes" -> {
                if (!isAuthorized()) {
                    respond("Unauthorized", HttpStatusCode.Unauthorized)
                } else {
                    // For UI tests we don't require parsing; just echo the last item id
                    val newId = idGenerator.getAndIncrement()
                    val created = BackendRecipe(id = newId, title = "Created $newId", body = "", timestamp = System.currentTimeMillis())
                    recipes[newId] = created
                    val body = "{" + "\"recipe\":" + json.encodeToString(created) + "}"
                    respond(body, HttpStatusCode.OK, jsonHeaders())
                }
            }

            method == HttpMethod.Delete && path.startsWith("/recipes/") -> {
                if (!isAuthorized()) {
                    respond("Unauthorized", HttpStatusCode.Unauthorized)
                } else {
                    val id = path.substringAfterLast("/").toLongOrNull()
                    if (id == null) {
                        respond("Bad Request", HttpStatusCode.BadRequest)
                    } else {
                        recipes.remove(id)
                        respond("{}", HttpStatusCode.OK, jsonHeaders())
                    }
                }
            }

            else -> respond("Not Found", HttpStatusCode.NotFound)
        }
    }
}

