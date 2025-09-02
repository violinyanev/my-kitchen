package com.ultraviolince.mykitchen.recipes.presentation.editrecipe

import androidx.lifecycle.SavedStateHandle
import com.ultraviolince.mykitchen.recipes.data.repository.FakeRecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.usecase.AddRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.DeleteRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetLoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipes
import com.ultraviolince.mykitchen.recipes.domain.usecase.Login
import com.ultraviolince.mykitchen.recipes.domain.usecase.Logout
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AddEditRecipeViewModelTest {

    private lateinit var addEditRecipeViewModel: AddEditRecipeViewModel
    private lateinit var fakeRepository: FakeRecipeRepository
    private lateinit var recipes: Recipes

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        recipes = Recipes(
            login = Login(fakeRepository),
            logout = Logout(fakeRepository),
            getSyncState = GetLoginState(fakeRepository),
            getRecipes = GetRecipes(fakeRepository),
            deleteRecipe = DeleteRecipe(fakeRepository),
            addRecipe = AddRecipe(fakeRepository),
            getRecipe = GetRecipe(fakeRepository)
        )

        // Create with empty savedStateHandle (new recipe scenario)
        val savedStateHandle = SavedStateHandle()
        addEditRecipeViewModel = AddEditRecipeViewModel(recipes, savedStateHandle)
    }

    @Test
    fun `should_handle_delete_gracefully_when_recipe_has_null_id`() = runBlocking {
        // Arrange - this simulates a new recipe that hasn't been saved yet
        // The viewModel starts with currentRecipeId = null for new recipes

        // Act - trigger delete event without saving first
        addEditRecipeViewModel.onEvent(AddEditRecipeEvent.DeleteRecipe)

        // Assert - should emit DeleteRecipe event without crashing
        val event = addEditRecipeViewModel.eventFlow.first()
        assertThat(event).isEqualTo(AddEditRecipeViewModel.UiEvent.DeleteRecipe)
    }
}
