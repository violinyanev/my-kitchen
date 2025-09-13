package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity.Recipe as LocalRecipe
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
        val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
        val recipeId = 5L

        coEvery { dao.insertRecipe(localRecipe) } returns recipeId
        coEvery { recipeService.insertRecipe(recipeId, recipe) } returns true

        val result = repository.insertRecipe(recipe)

        assertThat(result).isEqualTo(recipeId)

        // Verify the dao was called to insert and update sync status
        coVerify { dao.insertRecipe(localRecipe) }
        coVerify { recipeService.insertRecipe(recipeId, recipe) }
    }

    @Test
    fun `insertRecipe marks recipe as sync error on service failure`() = runBlocking {
        val recipe = Recipe("Test Recipe", "Content", 123L)
        val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
        val recipeId = 5L

        coEvery { dao.insertRecipe(localRecipe) } returns recipeId
        coEvery { recipeService.insertRecipe(recipeId, recipe) } returns false

        val result = repository.insertRecipe(recipe)

        assertThat(result).isEqualTo(recipeId)

        // Verify calls were made
        coVerify { dao.insertRecipe(localRecipe) }
        coVerify { recipeService.insertRecipe(recipeId, recipe) }
    }

    @Test
    fun `syncRecipe updates status correctly on success`() = runBlocking {
        val recipeId = 1L
        val recipe = Recipe("Test Recipe", "Content", 123L, id = recipeId)
        val localRecipe = LocalRecipe.fromSharedRecipe(recipe)

        coEvery { dao.getRecipeById(recipeId) } returns localRecipe
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
        val localRecipe = LocalRecipe.fromSharedRecipe(recipe)

        coEvery { dao.getRecipeById(recipeId) } returns localRecipe
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
        val localSyncedRecipes = listOf(
            LocalRecipe("Recipe 1", "Content", 123L, syncStatus = SyncStatus.SYNCED, id = 1L),
            LocalRecipe("Recipe 2", "Content", 456L, syncStatus = SyncStatus.SYNCED, id = 2L)
        )
        val expectedRecipes = localSyncedRecipes.map { it.toSharedRecipe() }

        coEvery { dao.getRecipesBySyncStatus(SyncStatus.SYNCED) } returns localSyncedRecipes

        val result = repository.getRecipesBySyncStatus(SyncStatus.SYNCED)

        assertThat(result).isEqualTo(expectedRecipes)
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
