package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.HttpHeaders
import io.ktor.serialization.gson.gson

fun createHttpClient(engine: HttpClientEngine, server: String, token: String?, customLogger: Logger?): HttpClient {
    return HttpClient(engine) {
        defaultRequest {
            url(server)
        }

        if (token != null) {
            install(Auth) {
                bearer {
                    loadTokens {
                        // TODO implement real bearer
                        BearerTokens(token, "not used")
                    }
                }
            }
        }

        if (customLogger != null) {
            install(Logging) {
                logger = customLogger
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }

        install(Resources)
        install(ContentNegotiation) {
            // TODO replace with plain json?
            gson()
        }
    }
}
