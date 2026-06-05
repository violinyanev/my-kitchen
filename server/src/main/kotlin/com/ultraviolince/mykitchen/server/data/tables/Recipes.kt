package com.ultraviolince.mykitchen.server.data.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object Recipes : UUIDTable("recipes") {
    val userId = reference("user_id", Users)
    val title = varchar("title", 500)
    val content = text("content")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}
