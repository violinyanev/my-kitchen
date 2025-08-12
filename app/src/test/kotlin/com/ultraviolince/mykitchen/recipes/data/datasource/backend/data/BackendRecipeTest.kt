package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackendRecipeTest {

    @Test
    fun `BackendRecipe holds all properties correctly`() {
        val id = 123L
        val title = "Chocolate Cake"
        val body = "Mix ingredients and bake"
        val timestamp = 1234567890L

        val recipe = BackendRecipe(id, title, body, timestamp)

        assertThat(recipe.id).isEqualTo(id)
        assertThat(recipe.title).isEqualTo(title)
        assertThat(recipe.body).isEqualTo(body)
        assertThat(recipe.timestamp).isEqualTo(timestamp)
    }

    @Test
    fun `BackendRecipe with empty strings`() {
        val recipe = BackendRecipe(0L, "", "", 0L)

        assertThat(recipe.id).isEqualTo(0L)
        assertThat(recipe.title).isEmpty()
        assertThat(recipe.body).isEmpty()
        assertThat(recipe.timestamp).isEqualTo(0L)
    }

    @Test
    fun `BackendRecipe equality works correctly`() {
        val recipe1 = BackendRecipe(1L, "Title", "Body", 123L)
        val recipe2 = BackendRecipe(1L, "Title", "Body", 123L)
        val recipe3 = BackendRecipe(2L, "Title", "Body", 123L)

        assertThat(recipe1).isEqualTo(recipe2)
        assertThat(recipe1).isNotEqualTo(recipe3)
    }

    @Test
    fun `BackendRecipe copy works correctly`() {
        val original = BackendRecipe(1L, "Original Title", "Original Body", 123L)
        val copied = original.copy(title = "Updated Title")

        assertThat(copied.id).isEqualTo(original.id)
        assertThat(copied.title).isEqualTo("Updated Title")
        assertThat(copied.body).isEqualTo(original.body)
        assertThat(copied.timestamp).isEqualTo(original.timestamp)
        assertThat(copied).isNotEqualTo(original)
    }
}
