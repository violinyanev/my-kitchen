package com.ultraviolince.mykitchen.server.data.tables

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = long("created_at")
}
