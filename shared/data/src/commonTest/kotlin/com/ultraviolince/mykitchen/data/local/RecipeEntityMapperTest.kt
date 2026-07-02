package com.ultraviolince.mykitchen.data.local

import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeEntityMapperTest {

    @Test
    fun `RecipeEntity toDomain maps all fields`() {
        val entity = RecipeEntity("id1", "My Recipe", "Cook well", 12345L, synced = true, deleted = false)
        val recipe = entity.toDomain()
        assertEquals("id1", recipe.id)
        assertEquals("My Recipe", recipe.title)
        assertEquals("Cook well", recipe.content)
        assertEquals(12345L, recipe.timestamp)
        assertEquals(true, recipe.synced)
        assertEquals(false, recipe.deleted)
    }

    @Test
    fun `Recipe toEntity maps all fields`() {
        val recipe = Recipe("id2", "Soup", "Boil water", 99999L, synced = false, deleted = true)
        val entity = recipe.toEntity()
        assertEquals("id2", entity.id)
        assertEquals("Soup", entity.title)
        assertEquals("Boil water", entity.content)
        assertEquals(99999L, entity.timestamp)
        assertEquals(false, entity.synced)
        assertEquals(true, entity.deleted)
    }

    @Test
    fun `toDomain and toEntity round-trip correctly`() {
        val entity = RecipeEntity("id3", "Cake", "Bake", 55555L, synced = false, deleted = false)
        assertEquals(entity, entity.toDomain().toEntity())
    }
}
