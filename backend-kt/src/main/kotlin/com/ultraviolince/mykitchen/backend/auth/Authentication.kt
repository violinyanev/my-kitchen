package com.ultraviolince.mykitchen.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.ultraviolince.mykitchen.backend.database.UserDatabase
import com.ultraviolince.mykitchen.backend.model.User

class JwtConfig(private val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(username: String): String {
        return JWT.create()
            .withClaim("username", username)
            .sign(algorithm)
    }

    fun verifyToken(token: String): String? {
        return try {
            val verifier = JWT.require(algorithm).build()
            val jwt = verifier.verify(token)
            jwt.getClaim("username").asString()
        } catch (e: JWTVerificationException) {
            null
        }
    }
}

class AuthenticationService(
    private val jwtConfig: JwtConfig,
    private val userDatabase: UserDatabase
) {
    fun authenticate(token: String?): User? {
        if (token == null) return null

        val username = jwtConfig.verifyToken(token) ?: return null
        return userDatabase.getByUsername(username)
    }
}