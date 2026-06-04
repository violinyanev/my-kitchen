package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddRecipeUseCaseTest {

    @Test
    fun `inserts recipe with generated id`() = runTest {
        val repo = FakeRecipeRepository()
        val result = AddRecipeUseCase(repo)("Pasta", "Boil water")
        assertTrue(result.isSuccess)
        val items = repo.getRecipes(RecipeOrder.Title()).first()
        assertEquals(1, items.size)
        assertEquals("Pasta", items[0].title)
        assertTrue(items[0].id.isNotBlank())
    }

    @Test
    fun `returns failure for blank title`() = runTest {
        val result = AddRecipeUseCase(FakeRecipeRepository())("  ", "content")
        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure for blank content`() = runTest {
        val result = AddRecipeUseCase(FakeRecipeRepository())("title", "")
        assertTrue(result.isFailure)
    }

    @Test
    fun `trims whitespace from title and content`() = runTest {
        val repo = FakeRecipeRepository()
        AddRecipeUseCase(repo)("  Pasta  ", "  Boil  ")
        val items = repo.getRecipes(RecipeOrder.Title()).first()
        assertEquals("Pasta", items[0].title)
        assertEquals("Boil", items[0].content)
    }
}
