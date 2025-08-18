package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.service.RecipeNetworkService
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class RecipeRepositoryImplTest {

    private lateinit var dao: RecipeDao
    private lateinit var recipeNetworkService: RecipeNetworkService
    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        recipeNetworkService = mockk()
        repository = RecipeRepositoryImpl(dao, recipeNetworkService)
    }

    @Test
    fun `getRecipes returns dao recipes`() = runBlocking {
        val expectedRecipes = listOf(
            Recipe("Recipe 1", "Content 1", 123L, 1L),
            Recipe("Recipe 2", "Content 2", 456L, 2L)
        )
        every { dao.getRecipes() } returns flowOf(expectedRecipes)

        val result = repository.getRecipes().first()

        assertThat(result).isEqualTo(expectedRecipes)
        verify { dao.getRecipes() }
    }

    @Test
    fun `getRecipeById returns dao recipe`() = runBlocking {
        val expectedRecipe = Recipe("Test Recipe", "Content", 123L, 1L)
        coEvery { dao.getRecipeById(1L) } returns expectedRecipe

        val result = repository.getRecipeById(1L)

        assertThat(result).isEqualTo(expectedRecipe)
        coVerify { dao.getRecipeById(1L) }
    }

    @Test
    fun `getRecipeById returns null when dao returns null`() = runBlocking {
        coEvery { dao.getRecipeById(999L) } returns null

        val result = repository.getRecipeById(999L)

        assertThat(result).isNull()
        coVerify { dao.getRecipeById(999L) }
    }

    @Test
    fun `insertRecipe inserts to dao and service`() = runBlocking {
        val recipe = Recipe("New Recipe", "Content", 123L)
        val expectedId = 5L

        coEvery { dao.insertRecipe(recipe) } returns expectedId
        coEvery { recipeNetworkService.insertRecipe(expectedId, recipe) } returns true

        val result = repository.insertRecipe(recipe)

        assertThat(result).isEqualTo(expectedId)
        coVerify { dao.insertRecipe(recipe) }
        coVerify { recipeNetworkService.insertRecipe(expectedId, recipe) }
    }

    @Test
    fun `deleteRecipe deletes from service and dao`() = runBlocking {
        val recipe = Recipe("Delete Me", "Content", 123L, 42L)

        coEvery { recipeNetworkService.deleteRecipe(42L) } returns true
        coEvery { dao.deleteRecipe(recipe) } returns Unit

        repository.deleteRecipe(recipe)

        coVerify { recipeNetworkService.deleteRecipe(42L) }
        coVerify { dao.deleteRecipe(recipe) }
    }
}
