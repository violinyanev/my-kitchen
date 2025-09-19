package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.firebase.FirebaseManager
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity.Recipe as LocalRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
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
    private lateinit var recipeService: RecipeServiceWrapper
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        recipeService = mockk()
        firebaseManager = mockk(relaxed = true)
        repository = RecipeRepositoryImpl(dao, recipeService, firebaseManager)
    }

    @Test
    fun `login delegates to recipeService`() = runBlocking {
        val server = "http://example.com"
        val email = "test@example.com"
        val password = "password"

        coEvery { recipeService.login(server, email, password) } returns Unit

        repository.login(server, email, password)

        coVerify { recipeService.login(server, email, password) }
    }

    @Test
    fun `logout delegates to recipeService`() = runBlocking {
        coEvery { recipeService.logout() } returns Unit

        repository.logout()

        coVerify { recipeService.logout() }
    }

    @Test
    fun `getLoginState returns recipeService loginState`() = runBlocking {
        val expectedLoginState = LoginState.LoginSuccess
        every { recipeService.loginState } returns kotlinx.coroutines.flow.MutableStateFlow(expectedLoginState)

        val result = repository.getLoginState().first()

        assertThat(result).isEqualTo(expectedLoginState)
        verify { recipeService.loginState }
    }

    @Test
    fun `getRecipes returns dao recipes`() = runBlocking {
        val localRecipes = listOf(
            LocalRecipe("Recipe 1", "Content 1", 123L, 1L),
            LocalRecipe("Recipe 2", "Content 2", 456L, 2L)
        )
        val expectedRecipes = listOf(
            Recipe("Recipe 1", "Content 1", 123L, 1L),
            Recipe("Recipe 2", "Content 2", 456L, 2L)
        )
        every { dao.getRecipes() } returns flowOf(localRecipes)

        val result = repository.getRecipes().first()

        assertThat(result).isEqualTo(expectedRecipes)
        verify { dao.getRecipes() }
    }

    @Test
    fun `getRecipeById returns dao recipe`() = runBlocking {
        val localRecipe = LocalRecipe("Test Recipe", "Content", 123L, 1L)
        val expectedRecipe = Recipe("Test Recipe", "Content", 123L, 1L)
        coEvery { dao.getRecipeById(1L) } returns localRecipe

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
        val localRecipe = LocalRecipe("New Recipe", "Content", 123L)
        val expectedId = 5L

        coEvery { dao.insertRecipe(localRecipe) } returns expectedId
        coEvery { recipeService.insertRecipe(expectedId, recipe) } returns true

        val result = repository.insertRecipe(recipe)

        assertThat(result).isEqualTo(expectedId)
        coVerify { dao.insertRecipe(localRecipe) }
        coVerify { recipeService.insertRecipe(expectedId, recipe) }
    }

    @Test
    fun `deleteRecipe deletes from service and dao`() = runBlocking {
        val recipe = Recipe("Delete Me", "Content", 123L, 42L)
        val localRecipe = LocalRecipe("Delete Me", "Content", 123L, 42L)

        coEvery { recipeService.deleteRecipe(42L) } returns true
        coEvery { dao.deleteRecipe(localRecipe) } returns Unit

        repository.deleteRecipe(recipe)

        coVerify { recipeService.deleteRecipe(42L) }
        coVerify { dao.deleteRecipe(localRecipe) }
    }

    @Test
    fun `deleteRecipe handles null id gracefully without crashing`() = runBlocking {
        val recipe = Recipe("Unsaved Recipe", "Content", 123L, null)

        // Should not call service or dao when id is null
        repository.deleteRecipe(recipe)

        // Verify that neither service nor dao were called
        coVerify(exactly = 0) { recipeService.deleteRecipe(any()) }
        coVerify(exactly = 0) { dao.deleteRecipe(any()) }
    }
}
