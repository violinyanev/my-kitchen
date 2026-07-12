package com.ultraviolince.mykitchen.server.plugins

import com.ultraviolince.mykitchen.server.config.AppConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import java.net.URL

fun Application.configureCors(config: AppConfig) {
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        val origins = config.corsAllowedOrigins
        if (origins == null) {
            check(config.devMode) {
                "CORS_ALLOWED_ORIGINS must be set in production. " +
                    "Set DEV_MODE=true only for local development to allow any origin."
            }
            anyHost()
        } else {
            origins.forEach { origin ->
                val url = URL(origin)
                val host = if (url.port != -1) "${url.host}:${url.port}" else url.host
                allowHost(host, schemes = listOf(url.protocol))
            }
        }
    }
}
