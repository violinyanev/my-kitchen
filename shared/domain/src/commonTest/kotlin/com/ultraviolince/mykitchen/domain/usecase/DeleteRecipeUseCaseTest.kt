package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteRecipeUseCaseTest {

    @Test
    fun `soft-deletes recipe so it no longer appears in list`() = runTest {
        val repo = FakeRecipeRepository()
        val r = Recipe.create("Pasta", "content")
        repo.insertRecipe(r)
        DeleteRecipeUseCase(repo)(r.id)
        val items = repo.getRecipes(RecipeOrder.Title()).first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun `deleting non-existent id does not throw`() = runTest {
        val repo = FakeRecipeRepository()
        DeleteRecipeUseCase(repo)("does-not-exist") // should not throw
    }
}
