package com.ultraviolince.mykitchen.data.remote.dto

import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeDtoMapperTest {

    @Test
    fun `RecipeDto toDomain uses updatedAt as timestamp and marks synced`() {
        val dto = RecipeDto(id = "id1", title = "Pasta", content = "Cook it", createdAt = 1000L, updatedAt = 2000L)
        val recipe = dto.toDomain()
        assertEquals("id1", recipe.id)
        assertEquals("Pasta", recipe.title)
        assertEquals("Cook it", recipe.content)
        assertEquals(2000L, recipe.timestamp)
        assertEquals(true, recipe.synced)
        assertEquals(false, recipe.deleted)
    }

    @Test
    fun `Recipe toDto maps timestamp to both createdAt and updatedAt`() {
        val recipe = Recipe("id2", "Soup", "Boil water", 5000L, synced = true, deleted = false)
        val dto = recipe.toDto()
        assertEquals("id2", dto.id)
        assertEquals("Soup", dto.title)
        assertEquals("Boil water", dto.content)
        assertEquals(5000L, dto.createdAt)
        assertEquals(5000L, dto.updatedAt)
    }
}
