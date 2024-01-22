package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.data.repository.FakeRecipeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class DeleteRecipesTest {

    private lateinit var deleteRecipe: DeleteRecipe
    private lateinit var fakeRepository: FakeRecipeRepository

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        deleteRecipe = DeleteRecipe(fakeRepository)
    }

    @Test
    fun `Deletes recipe`() = runBlocking {
        val r = fakeRepository.getRecipes().first()[0]

        assertThat(r).isIn(fakeRepository.getTestRecipes())
        deleteRecipe(r)
        assertThat(r).isNotIn(fakeRepository.getTestRecipes())
    }
}
