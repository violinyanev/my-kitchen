package com.example.myapplication

import com.example.myapplication.recipes.domain.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeTest {
    @Test
    fun recipe_constructor() {
        val recipe = Recipe(title = "recipe", content = "content", timestamp = 15, id = 123)
        assertEquals(recipe.title, "recipe")
        assertEquals(recipe.content, "content")
        assertEquals(recipe.timestamp, 15)
        assertEquals(recipe.id, 123)
    }
}
