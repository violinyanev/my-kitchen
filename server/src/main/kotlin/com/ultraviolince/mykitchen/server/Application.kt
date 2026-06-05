package com.ultraviolince.mykitchen.server

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.plugins.configureAuthentication
import com.ultraviolince.mykitchen.server.plugins.configureCors
import com.ultraviolince.mykitchen.server.plugins.configureDatabase
import com.ultraviolince.mykitchen.server.plugins.configureSerialization
import com.ultraviolince.mykitchen.server.plugins.configureStatusPages
import com.ultraviolince.mykitchen.server.routes.authRoutes
import com.ultraviolince.mykitchen.server.routes.healthRoutes
import com.ultraviolince.mykitchen.server.routes.recipeRoutes
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 5000, host = "0.0.0.0") {
        val config = AppConfig.fromEnvironment()
        configureDatabase(config)
        configureSerialization()
        configureAuthentication(config)
        configureStatusPages()
        configureCors()
        configureRouting(config)
    }.start(wait = true)
}

fun Application.configureRouting(config: AppConfig) {
    routing {
        healthRoutes()
        authRoutes(config)
        recipeRoutes()
    }
}
