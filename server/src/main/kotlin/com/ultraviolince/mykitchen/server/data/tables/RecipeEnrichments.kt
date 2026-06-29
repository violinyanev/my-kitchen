package com.ultraviolince.mykitchen.server.data.tables

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

object RecipeEnrichments : UUIDTable("recipe_enrichments") {
    val recipeId = reference("recipe_id", Recipes)
    val userId = reference("user_id", Users)
    val imageUrl = varchar("image_url", 2000).nullable()
    val imageCredit = varchar("image_credit", 500).nullable()
    val links = text("links")
    val tags = text("tags")
    val summary = text("summary")
    val conversationHistory = text("conversation_history")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}
