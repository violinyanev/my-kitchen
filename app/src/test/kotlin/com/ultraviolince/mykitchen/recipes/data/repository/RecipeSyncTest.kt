package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Tests for the new sync functionality in RecipeRepositoryImpl
 */
class RecipeSyncTest {

    private lateinit var dao: RecipeDao
    private lateinit var recipeService: RecipeServiceWrapper
    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        recipeService = mockk(relaxed = true)
        repository = RecipeRepositoryImpl(dao, recipeService)
    }

    @Test
    fun `insertRecipe marks recipe as syncing then synced on success`() = runBlocking {
        val recipe = Recipe("Test Recipe", "Content", 123L)
        val recipeId = 5L

        coEvery { dao.insertRecipe(recipe) } returns recipeId
        coEvery { recipeService.insertRecipe(recipeId, recipe) } returns true

        val result = repository.insertRecipe(recipe)

        assertThat(result).isEqualTo(recipeId)

        // Verify the dao was called to insert and update sync status
        coVerify { dao.insertRecipe(recipe) }
        coVerify { recipeService.insertRecipe(recipeId, recipe) }
    }

    @Test
    fun `insertRecipe marks recipe as sync error on service failure`() = runBlocking {
        val recipe = Recipe("Test Recipe", "Content", 123L)
        val recipeId = 5L

        coEvery { dao.insertRecipe(recipe) } returns recipeId
        coEvery { recipeService.insertRecipe(recipeId, recipe) } returns false

        val result = repository.insertRecipe(recipe)

        assertThat(result).isEqualTo(recipeId)

        // Verify calls were made
        coVerify { dao.insertRecipe(recipe) }
        coVerify { recipeService.insertRecipe(recipeId, recipe) }
    }

    @Test
    fun `syncRecipe updates status correctly on success`() = runBlocking {
        val recipeId = 1L
        val recipe = Recipe("Test Recipe", "Content", 123L, id = recipeId)

        coEvery { dao.getRecipeById(recipeId) } returns recipe
        coEvery { recipeService.insertRecipe(recipeId, recipe) } returns true

        val result = repository.syncRecipe(recipeId)

        assertThat(result).isTrue()
        coVerify { dao.getRecipeById(recipeId) }
        coVerify { recipeService.insertRecipe(recipeId, recipe) }
    }

    @Test
    fun `syncRecipe updates status correctly on failure`() = runBlocking {
        val recipeId = 1L
        val recipe = Recipe("Test Recipe", "Content", 123L, id = recipeId)

        coEvery { dao.getRecipeById(recipeId) } returns recipe
        coEvery { recipeService.insertRecipe(recipeId, recipe) } returns false

        val result = repository.syncRecipe(recipeId)

        assertThat(result).isFalse()
        coVerify { dao.getRecipeById(recipeId) }
        coVerify { recipeService.insertRecipe(recipeId, recipe) }
    }

    @Test
    fun `syncRecipe returns false for non-existent recipe`() = runBlocking {
        val recipeId = 999L

        coEvery { dao.getRecipeById(recipeId) } returns null

        val result = repository.syncRecipe(recipeId)

        assertThat(result).isFalse()
        coVerify { dao.getRecipeById(recipeId) }
    }

    @Test
    fun `getRecipesBySyncStatus filters correctly`() = runBlocking {
        val syncedRecipes = listOf(
            Recipe("Recipe 1", "Content", 123L, SyncStatus.SYNCED, id = 1L),
            Recipe("Recipe 2", "Content", 456L, SyncStatus.SYNCED, id = 2L)
        )

        coEvery { dao.getRecipesBySyncStatus(SyncStatus.SYNCED) } returns syncedRecipes

        val result = repository.getRecipesBySyncStatus(SyncStatus.SYNCED)

        assertThat(result).isEqualTo(syncedRecipes)
        coVerify { dao.getRecipesBySyncStatus(SyncStatus.SYNCED) }
    }

    @Test
    fun `updateRecipeSyncStatus delegates to dao`() = runBlocking {
        val recipeId = 1L
        val timestamp = System.currentTimeMillis()

        repository.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCED, timestamp, null)

        coVerify { dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCED, timestamp, null) }
    }

    @Test
    fun `syncAllRecipes delegates to service`() = runBlocking {
        repository.syncAllRecipes()

        coVerify { recipeService.syncAllRecipes() }
    }
}
