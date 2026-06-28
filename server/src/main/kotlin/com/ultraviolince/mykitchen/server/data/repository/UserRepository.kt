package com.ultraviolince.mykitchen.server.data.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.ultraviolince.mykitchen.server.data.tables.Users
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

object UserRepository {

    fun findByEmail(email: String): UUID? = transaction {
        Users.selectAll()
            .where { Users.email eq email }
            .firstOrNull()
            ?.get(Users.id)?.value
    }

    fun create(email: String, password: String): UUID = transaction {
        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        Users.insert { row ->
            row[Users.email] = email
            row[Users.passwordHash] = hash
            row[Users.createdAt] = System.currentTimeMillis()
        }[Users.id].value
    }
}
