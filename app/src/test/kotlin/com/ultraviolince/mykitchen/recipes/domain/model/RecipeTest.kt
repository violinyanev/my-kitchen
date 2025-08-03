package com.ultraviolince.mykitchen.recipes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeTest {
    @Test
    fun `has values passed to constructors`() {
        val recipe = Recipe(title = "the recipe", content = "the content", timestamp = 15, id = 123L)
        assertEquals(recipe.title, "the recipe")
        assertEquals(recipe.content, "the content")
        assertEquals(recipe.timestamp, 15)
        assertEquals(recipe.id, 123L)
    }

    @Test
    fun `has default null imagePath when not provided`() {
        val recipe = Recipe(title = "test", content = "content", timestamp = 100)
        assertNull(recipe.imagePath)
        assertNull(recipe.id)
    }

    @Test
    fun `has imagePath when provided`() {
        val imagePath = "/path/to/image.jpg"
        val recipe = Recipe(
            title = "test",
            content = "content", 
            timestamp = 100,
            imagePath = imagePath,
            id = 1L
        )
        assertEquals(imagePath, recipe.imagePath)
        assertEquals(1L, recipe.id)
    }

    @Test
    fun `toString includes id and title`() {
        val recipe = Recipe(title = "My Recipe", content = "content", timestamp = 123456, id = 42L)
        val result = recipe.toString()
        assertEquals("Recipe[42] My Recipe (ts 123456)", result)
    }

    @Test
    fun `toString with null id`() {
        val recipe = Recipe(title = "My Recipe", content = "content", timestamp = 123456)
        val result = recipe.toString()
        assertEquals("Recipe[null] My Recipe (ts 123456)", result)
    }
}
