package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import io.mockk.every
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RecipeMergerTest {

    @Before
    fun mockLog() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun `getDiff should return empty lists when both localRecipes and backendRecipes are empty`() = runTest {
        // Arrange
        val localRecipes = emptyList<Recipe>()
        val backendRecipes = emptyList<BackendRecipe>()

        // Act
        val diff = RecipeMerger.getDiff(localRecipes, backendRecipes)

        // Assert
        assertEquals(emptyList<Recipe>(), diff.localRecipes)
        assertEquals(emptyList<BackendRecipe>(), diff.backendRecipes)
    }

    @Test
    fun `getDiff should return empty lists when localRecipes and backendRecipes have the same recipes`() = runTest {
        // Arrange
        val recipe1 = Recipe("Recipe 1", "Content 1", 100, 1)
        val recipe2 = Recipe("Recipe 2", "Content 2", 200, 2)
        val localRecipes = listOf(recipe1, recipe2)
        val backendRecipes = listOf(
            BackendRecipe(1, "Recipe 1", "Content 1", 100),
            BackendRecipe(2, "Recipe 2", "Content 2", 200)
        )

        // Act
        val diff = RecipeMerger.getDiff(localRecipes, backendRecipes)

        // Assert
        assertEquals(emptyList<Recipe>(), diff.localRecipes)
        assertEquals(emptyList<BackendRecipe>(), diff.backendRecipes)
    }

    @Test
    fun `getDiff should return recipes to upload to backend when localRecipes have new recipes`() {
        // Arrange
        val recipe1 = Recipe("Recipe 1", "Content 1", 100, 1)
        val recipe2 = Recipe("Recipe 2", "Content 2", 200, 2)
        val localRecipes = listOf(recipe1, recipe2)
        val backendRecipes = listOf(BackendRecipe(1, "Recipe 1", "Content 1", 100))

        // Act
        val diff = RecipeMerger.getDiff(localRecipes, backendRecipes)

        // Assert
        assertEquals(emptyList<Recipe>(), diff.localRecipes)
        assertEquals(listOf(recipe2.toBackendRecipe()), diff.backendRecipes)
    }

    @Test
    fun `getDiff should return recipes to upload to backend when localRecipes have updated recipes`() {
        // Arrange
        val recipe1 = Recipe("Recipe 1", "Content 1", 100, 1)
        val localRecipes = listOf(recipe1)
        val backendRecipes = listOf(BackendRecipe(1, "Recipe 1", "Content 1", 50))

        // Act
        val diff = RecipeMerger.getDiff(localRecipes, backendRecipes)

        // Assert
        assertEquals(emptyList<Recipe>(), diff.localRecipes)
        assertEquals(listOf(recipe1.toBackendRecipe()), diff.backendRecipes)
    }

    @Test
    fun `getDiff should return recipes to save to DB when backendRecipes have new or updated recipes`() {
        // Arrange
        val recipe1 = Recipe("Recipe 1", "Content 1", 100, 1)
        val recipe2 = Recipe("Recipe 2", "Content 2", 200, 2)
        val localRecipes = listOf(Recipe("Recipe 1", "Content 1", 50, 1))
        val backendRecipes = listOf(recipe1.toBackendRecipe(), recipe2.toBackendRecipe())

        // Act
        val diff = RecipeMerger.getDiff(localRecipes, backendRecipes)

        // Assert
        assertEquals(listOf(recipe1, recipe2), diff.localRecipes)
        assertTrue(diff.backendRecipes.isEmpty())
    }
}
