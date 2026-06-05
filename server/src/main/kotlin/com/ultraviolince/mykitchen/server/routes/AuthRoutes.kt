package com.ultraviolince.mykitchen.server.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.dto.ErrorDto
import com.ultraviolince.mykitchen.server.data.dto.LoginRequestDto
import com.ultraviolince.mykitchen.server.data.dto.LoginResponseDto
import com.ultraviolince.mykitchen.server.data.tables.Users
import com.ultraviolince.mykitchen.server.plugins.generateToken
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.authRoutes(config: AppConfig) {
    post("/users/login") {
        val request = call.receive<LoginRequestDto>()
        if (request.email.isBlank() || request.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorDto("Email and password are required"))
            return@post
        }

        val user = transaction {
            Users.selectAll()
                .where { Users.email eq request.email }
                .firstOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorDto("Invalid credentials"))
            return@post
        }

        val passwordMatches = BCrypt.verifyer()
            .verify(request.password.toCharArray(), user[Users.passwordHash])
            .verified

        if (!passwordMatches) {
            call.respond(HttpStatusCode.Unauthorized, ErrorDto("Invalid credentials"))
            return@post
        }

        val token = generateToken(
            config = config,
            userId = user[Users.id].value.toString(),
            email = user[Users.email],
        )
        call.respond(LoginResponseDto(token))
    }
}
