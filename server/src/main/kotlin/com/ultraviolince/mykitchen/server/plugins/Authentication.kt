package com.ultraviolince.mykitchen.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ultraviolince.mykitchen.server.config.AppConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

const val JWT_AUTH_NAME = "jwt"

fun Application.configureAuthentication(config: AppConfig) {
    val algorithm = Algorithm.HMAC256(config.jwtSecret)
    val verifier = JWT.require(algorithm)
        .withIssuer(config.jwtIssuer)
        .withAudience(config.jwtAudience)
        .build()

    authentication {
        jwt(JWT_AUTH_NAME) {
            realm = "My Kitchen"
            this.verifier(verifier)
            validate { credentials ->
                if (credentials.payload.audience.contains(config.jwtAudience) &&
                    credentials.payload.subject != null
                ) {
                    JWTPrincipal(credentials.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun generateToken(config: AppConfig, userId: String, email: String): String =
    JWT.create()
        .withIssuer(config.jwtIssuer)
        .withAudience(config.jwtAudience)
        .withSubject(userId)
        .withClaim("email", email)
        .sign(Algorithm.HMAC256(config.jwtSecret))
