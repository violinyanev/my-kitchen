package com.example.myapplication.recipes.domain.model

import org.junit.Assert.assertEquals
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
}
