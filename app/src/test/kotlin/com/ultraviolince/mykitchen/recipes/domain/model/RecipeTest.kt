package com.ultraviolince.mykitchen.recipes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import com.google.common.truth.Truth.assertThat

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
    fun `toString includes important information`() {
        val recipe = Recipe(title = "Test Recipe", content = "content", timestamp = 12345L, id = 999L)
        val toString = recipe.toString()

        assertThat(toString).contains("999")
        assertThat(toString).contains("Test Recipe")
        assertThat(toString).contains("12345")
    }

    @Test
    fun `Recipe with null id`() {
        val recipe = Recipe(title = "No ID Recipe", content = "content", timestamp = 123L, id = null)

        assertThat(recipe.id).isNull()
        assertThat(recipe.title).isEqualTo("No ID Recipe")
        assertThat(recipe.toString()).contains("null")
    }

    @Test
    fun `Recipe equality works correctly`() {
        val recipe1 = Recipe("Title", "Content", 123L, 1L)
        val recipe2 = Recipe("Title", "Content", 123L, 1L)
        val recipe3 = Recipe("Different", "Content", 123L, 1L)

        assertThat(recipe1).isEqualTo(recipe2)
        assertThat(recipe1).isNotEqualTo(recipe3)
    }

    @Test
    fun `Recipe copy works correctly`() {
        val original = Recipe("Original", "Content", 123L, 1L)
        val copied = original.copy(title = "Modified")

        assertThat(copied.title).isEqualTo("Modified")
        assertThat(copied.content).isEqualTo(original.content)
        assertThat(copied.timestamp).isEqualTo(original.timestamp)
        assertThat(copied.id).isEqualTo(original.id)
        assertThat(copied).isNotEqualTo(original)
    }
}
