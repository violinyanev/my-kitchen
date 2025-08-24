package com.ultraviolince.mykitchen.backend

import com.ultraviolince.mykitchen.backend.auth.AuthenticationService
import com.ultraviolince.mykitchen.backend.auth.JwtConfig
import com.ultraviolince.mykitchen.backend.database.RecipeDatabase
import com.ultraviolince.mykitchen.backend.database.UserDatabase
import com.ultraviolince.mykitchen.backend.model.ApiVersion
import com.ultraviolince.mykitchen.backend.routes.configureRecipeRoutes
import com.ultraviolince.mykitchen.backend.routes.configureUserRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

fun getApiVersion() = ApiVersion(
    apiVersionMajor = 0,
    apiVersionMinor = 5,
    apiVersionPatch = 2
)

fun main(args: Array<String>) {
    println("API version: ${getApiVersion()}")

    // Get secret key
    val secretKey = System.getenv("RECIPES_SECRET_KEY") ?: "Test"

    // Get data folder
    val defaultDataFolder = System.getProperty("java.io.tmpdir") + "/my_kitchen_data"
    val dataFolder = if (args.isNotEmpty()) {
        args[0]
    } else {
        defaultDataFolder
    }

    val folder = Paths.get(dataFolder)
    if (!folder.exists()) {
        folder.createDirectories()
    }

    println("Using data in $folder")

    // Initialize databases
    val userDatabase = UserDatabase(folder.resolve("users.yaml"), createBackup = true)
    val recipeDatabase = RecipeDatabase(folder.resolve("recipes.yaml"), createBackup = true)

    // Initialize authentication
    val jwtConfig = JwtConfig(secretKey)
    val authService = AuthenticationService(jwtConfig, userDatabase)

    // Configure host and port
    val host = System.getenv("FLASK_HOST") ?: "127.0.0.1"
    val port = System.getenv("FLASK_PORT")?.toIntOrNull() ?: 5000

    embeddedServer(Netty, port = port, host = host) {
        configureKtor(userDatabase, recipeDatabase, jwtConfig, authService)
    }.start(wait = true)
}

fun Application.configureKtor(
    userDatabase: UserDatabase,
    recipeDatabase: RecipeDatabase,
    jwtConfig: JwtConfig,
    authService: AuthenticationService
) {
    // Install plugins
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            }
        )
    }

    install(CORS) {
        anyHost() // Allow any host for development
    }

    // Install call logging
    install(io.ktor.server.plugins.calllogging.CallLogging)

    // Configure routes
    routing {
        get("/health") {
            call.respondText("OK", status = HttpStatusCode.OK)
        }

        get("/version") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val user = authService.authenticate(token)

            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "message" to "Authentication Token is missing!",
                        "data" to null,
                        "error" to "Unauthorized"
                    )
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, mapOf("current_user" to user))
        }
    }

    // Configure logging (simplified version)
    monitor.subscribe(io.ktor.server.application.ApplicationStarted) {
        println("Application started")
    }

    // Configure user and recipe routes
    configureUserRoutes(userDatabase, jwtConfig, authService)
    configureRecipeRoutes(recipeDatabase, authService)
}
