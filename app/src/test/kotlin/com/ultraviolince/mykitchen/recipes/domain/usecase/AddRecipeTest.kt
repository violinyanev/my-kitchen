package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.data.repository.FakeRecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.InvalidRecipeException
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AddRecipeTest {

    private lateinit var addRecipe: AddRecipe
    private lateinit var fakeRepository: FakeRecipeRepository

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        addRecipe = AddRecipe(fakeRepository)
    }

    @Test
    fun `Add recipe`() = runBlocking {
        val recipe = Recipe(title = "test", content = "content", id = 5L, timestamp = 15L)
        addRecipe(recipe)
        assertThat(recipe).isIn(fakeRepository.getTestRecipes())
    }

    @Test
    fun `add valid recipe with null id`() = runBlocking {
        val recipe = Recipe(title = "New Recipe", content = "content", id = null, timestamp = 123L)
        // Should not throw exception
        addRecipe(recipe)
        assertThat(true).isTrue() // Test passes if no exception thrown
    }

    @Test
    fun `add recipe with empty title throws exception`() {
        val recipe = Recipe(title = "", content = "content", id = null, timestamp = 456L)

        try {
            runBlocking { addRecipe(recipe) }
            assertThat(false).isTrue() // Should not reach here
        } catch (e: InvalidRecipeException) {
            assertThat(true).isTrue() // Expected exception
        }
    }

    @Test
    fun `add recipe with empty content throws exception`() {
        val recipe = Recipe(title = "Title", content = "", id = null, timestamp = 789L)

        try {
            runBlocking { addRecipe(recipe) }
            assertThat(false).isTrue() // Should not reach here
        } catch (e: InvalidRecipeException) {
            assertThat(true).isTrue() // Expected exception
        }
    }

    @Test
    fun `add recipe with valid title and content succeeds`() = runBlocking {
        val recipe = Recipe(title = "Valid Title", content = "Valid content", id = null, timestamp = 100L)
        // Should not throw exception
        addRecipe(recipe)
        assertThat(true).isTrue() // Test passes if no exception thrown
    }
}
