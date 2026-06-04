package com.ultraviolince.mykitchen.domain.fake

import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeRecipeRepositoryTest {

    @Test
    fun `insertRecipe and getRecipeById round-trips`() = runTest {
        val repo = FakeRecipeRepository()
        val r = Recipe.create("Pasta", "Boil water")
        repo.insertRecipe(r)
        assertEquals(r, repo.getRecipeById(r.id))
    }

    @Test
    fun `deleteRecipe soft-deletes so getRecipes excludes it`() = runTest {
        val repo = FakeRecipeRepository()
        val r = Recipe.create("Pasta", "Boil water")
        repo.insertRecipe(r)
        repo.deleteRecipe(r.id)
        val items = repo.getRecipes(RecipeOrder.Title()).first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun `getRecipes sorts by title ascending`() = runTest {
        val repo = FakeRecipeRepository()
        repo.insertRecipe(Recipe.create("Zucchini", "z"))
        repo.insertRecipe(Recipe.create("Apple", "a"))
        val items = repo.getRecipes(RecipeOrder.Title(ascending = true)).first()
        assertEquals("Apple", items[0].title)
        assertEquals("Zucchini", items[1].title)
    }

    @Test
    fun `login sets LoggedIn state on success`() = runTest {
        val repo = FakeRecipeRepository()
        repo.login("user@test.com", "pass", "http://localhost:5000")
        val state = repo.getAuthState().first()
        assertTrue(state is AuthState.LoggedIn)
    }

    @Test
    fun `logout sets LoggedOut state`() = runTest {
        val repo = FakeRecipeRepository()
        repo.login("user@test.com", "pass", "http://localhost:5000")
        repo.logout()
        val state = repo.getAuthState().first()
        assertEquals(AuthState.LoggedOut, state)
    }
}
