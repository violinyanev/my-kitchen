package com.ultraviolince.mykitchen.backend.routes

import com.ultraviolince.mykitchen.backend.auth.AuthenticationService
import com.ultraviolince.mykitchen.backend.auth.JwtConfig
import com.ultraviolince.mykitchen.backend.database.UserDatabase
import com.ultraviolince.mykitchen.backend.model.ErrorResponse
import com.ultraviolince.mykitchen.backend.model.LoginRequest
import com.ultraviolince.mykitchen.backend.model.LoginResponse
import com.ultraviolince.mykitchen.backend.model.UserData
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureUserRoutes(
    userDatabase: UserDatabase,
    jwtConfig: JwtConfig,
    authService: AuthenticationService
) {
    routing {
        post("/users/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()
                val (user, error) = userDatabase.validateLoginRequest(loginRequest.email, loginRequest.password)

                if (error != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = error, error = "Bad Request")
                    )
                    return@post
                }

                if (user != null) {
                    val token = jwtConfig.generateToken(user.name)
                    val response = LoginResponse(
                        message = "Successfully created authentication token",
                        data = UserData(
                            email = user.email,
                            username = user.name,
                            token = token
                        )
                    )
                    call.respond(HttpStatusCode.OK, response)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "Invalid request format", error = "Bad Request")
                )
            }
        }

        get("/users") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val user = authService.authenticate(token)

            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        message = "Authentication Token is missing!",
                        data = null,
                        error = "Unauthorized"
                    )
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, userDatabase.getAll())
        }

        get("/user") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val user = authService.authenticate(token)

            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        message = "Authentication Token is missing!",
                        data = null,
                        error = "Unauthorized"
                    )
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, user)
        }
    }
}
