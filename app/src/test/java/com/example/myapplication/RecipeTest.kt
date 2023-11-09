package com.example.myapplication

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.ui.theme.Purple80
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeTest {
    @Test
    fun recipe_hasDefaultColors() {
        assertEquals(Recipe.recipeColors, listOf(Purple80))
    }

    @Test
    fun recipe_constructor() {
        val recipe = Recipe(title = "recipe", content = "content", timestamp = 15, color = 1, id = 123)
        assertEquals(recipe.title, "recipe")
        assertEquals(recipe.content, "content")
        assertEquals(recipe.timestamp, 15)
        assertEquals(recipe.color, 1)
        assertEquals(recipe.id, 123)
    }
}
