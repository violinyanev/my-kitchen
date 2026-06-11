package com.ultraviolince.mykitchen.server.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.dto.ErrorDto
import com.ultraviolince.mykitchen.server.data.dto.LoginRequestDto
import com.ultraviolince.mykitchen.server.data.dto.LoginResponseDto
import com.ultraviolince.mykitchen.server.data.dto.RegisterRequestDto
import com.ultraviolince.mykitchen.server.data.repository.UserRepository
import com.ultraviolince.mykitchen.server.data.tables.Users
import com.ultraviolince.mykitchen.server.plugins.generateToken
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private const val MIN_PASSWORD_LENGTH = 8

private fun String.isValidEmail(): Boolean = EMAIL_REGEX.matches(this)
private fun String.isValidPassword(): Boolean = length >= MIN_PASSWORD_LENGTH

fun Route.authRoutes(config: AppConfig) {
    post("/users/login") {
        val request = call.receive<LoginRequestDto>()
        if (!request.email.isValidEmail()) {
            call.respond(HttpStatusCode.BadRequest, ErrorDto("Valid email address is required"))
            return@post
        }
        if (!request.password.isValidPassword()) {
            call.respond(HttpStatusCode.BadRequest, ErrorDto("Password must be at least $MIN_PASSWORD_LENGTH characters"))
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

    post("/users/register") {
        val request = call.receive<RegisterRequestDto>()
        if (!request.email.isValidEmail()) {
            call.respond(HttpStatusCode.BadRequest, ErrorDto("Valid email address is required"))
            return@post
        }
        if (!request.password.isValidPassword()) {
            call.respond(HttpStatusCode.BadRequest, ErrorDto("Password must be at least $MIN_PASSWORD_LENGTH characters"))
            return@post
        }
        if (UserRepository.findByEmail(request.email) != null) {
            call.respond(HttpStatusCode.Conflict, ErrorDto("Email already registered"))
            return@post
        }
        val userId = UserRepository.create(request.email, request.password)
        val token = generateToken(
            config = config,
            userId = userId.toString(),
            email = request.email,
        )
        call.respond(HttpStatusCode.Created, LoginResponseDto(token))
    }
}
