package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.data.repository.FakeAuthRepository
import com.ultraviolince.mykitchen.recipes.data.repository.FakeRecipeRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipesTest {

    private lateinit var fakeRecipeRepository: FakeRecipeRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var recipes: Recipes

    @Before
    fun setUp() {
        fakeRecipeRepository = FakeRecipeRepository()
        fakeAuthRepository = FakeAuthRepository()
        recipes = Recipes(
            login = Login(fakeAuthRepository),
            logout = Logout(fakeAuthRepository),
            getSyncState = GetLoginState(fakeAuthRepository),
            getRecipes = GetRecipes(fakeRecipeRepository),
            deleteRecipe = DeleteRecipe(fakeRecipeRepository),
            addRecipe = AddRecipe(fakeRecipeRepository),
            getRecipe = GetRecipe(fakeRecipeRepository)
        )
    }

    @Test
    fun `Recipes data class holds all use cases correctly`() {
        assertThat(recipes.login).isInstanceOf(Login::class.java)
        assertThat(recipes.logout).isInstanceOf(Logout::class.java)
        assertThat(recipes.getSyncState).isInstanceOf(GetLoginState::class.java)
        assertThat(recipes.getRecipes).isInstanceOf(GetRecipes::class.java)
        assertThat(recipes.deleteRecipe).isInstanceOf(DeleteRecipe::class.java)
        assertThat(recipes.addRecipe).isInstanceOf(AddRecipe::class.java)
        assertThat(recipes.getRecipe).isInstanceOf(GetRecipe::class.java)
    }

    @Test
    fun `Recipes copy works correctly`() {
        val newLogin = Login(fakeAuthRepository)
        val copied = recipes.copy(login = newLogin)

        assertThat(copied.login).isSameInstanceAs(newLogin)
        assertThat(copied.logout).isSameInstanceAs(recipes.logout)
        assertThat(copied).isNotEqualTo(recipes)
    }

    @Test
    fun `Recipes equality works correctly`() {
        val recipes2 = Recipes(
            login = recipes.login,
            logout = recipes.logout,
            getSyncState = recipes.getSyncState,
            getRecipes = recipes.getRecipes,
            deleteRecipe = recipes.deleteRecipe,
            addRecipe = recipes.addRecipe,
            getRecipe = recipes.getRecipe
        )

        assertThat(recipes).isEqualTo(recipes2)
    }
}
