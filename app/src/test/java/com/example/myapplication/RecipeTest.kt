package com.example.myapplication

import com.example.myapplication.recipes.domain.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeTest {
    @Test
    fun `has values passed to constructors`() {
        val recipe = Recipe(title = "the recipe", content = "the content", timestamp = 15, id = 123)
        assertEquals(recipe.title, "the recipe")
        assertEquals(recipe.content, "the content")
        assertEquals(recipe.timestamp, 15)
        assertEquals(recipe.id, 123)
    }
}
