package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackendRecipeResponseTest {

    @Test
    fun `BackendRecipeResponse holds recipe correctly`() {
        val recipe = BackendRecipe(
            id = 123L,
            title = "Test Recipe",
            body = "Test Body",
            timestamp = 456L
        )
        val response = BackendRecipeResponse(recipe)

        assertThat(response.recipe).isEqualTo(recipe)
        assertThat(response.recipe.id).isEqualTo(123L)
        assertThat(response.recipe.title).isEqualTo("Test Recipe")
    }

    @Test
    fun `BackendRecipeResponse equality works correctly`() {
        val recipe = BackendRecipe(1L, "Title", "Body", 123L)
        val response1 = BackendRecipeResponse(recipe)
        val response2 = BackendRecipeResponse(recipe.copy())

        assertThat(response1).isEqualTo(response2)
    }

    @Test
    fun `BackendRecipeResponse copy works correctly`() {
        val recipe = BackendRecipe(1L, "Original", "Body", 123L)
        val original = BackendRecipeResponse(recipe)
        val newRecipe = recipe.copy(title = "Updated")
        val copied = original.copy(recipe = newRecipe)

        assertThat(copied.recipe.title).isEqualTo("Updated")
        assertThat(copied).isNotEqualTo(original)
    }
}
