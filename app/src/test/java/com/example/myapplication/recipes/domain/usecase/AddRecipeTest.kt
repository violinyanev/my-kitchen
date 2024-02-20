package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.data.repository.FakeRecipeRepository
import com.example.myapplication.recipes.domain.model.Recipe
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
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
        val recipe = Recipe(title="test", content = "content", id = 5L, timestamp = 15L)
        addRecipe(recipe)
        assertThat(recipe).isIn(fakeRepository.getTestRecipes())
    }
}
