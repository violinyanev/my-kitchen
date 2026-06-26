package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRecipesUseCaseTest {

    @Test
    fun `emits recipes sorted by title ascending`() = runTest {
        val repo = FakeRecipeRepository()
        val useCase = GetRecipesUseCase(repo)
        repo.insertRecipe(Recipe.create("Zucchini", "z"))
        repo.insertRecipe(Recipe.create("Apple pie", "a"))
        val result = useCase(RecipeOrder.Title(ascending = true)).first()
        assertEquals("Apple pie", result[0].title)
        assertEquals("Zucchini", result[1].title)
    }

    @Test
    fun `does not emit deleted recipes`() = runTest {
        val repo = FakeRecipeRepository()
        val useCase = GetRecipesUseCase(repo)
        val r = Recipe.create("Ghost", "boo")
        repo.insertRecipe(r)
        repo.deleteRecipe(r.id)
        val result = useCase(RecipeOrder.Title()).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `uses Date descending order by default`() = runTest {
        val repo = FakeRecipeRepository()
        val useCase = GetRecipesUseCase(repo)
        repo.insertRecipe(Recipe.create("Old", "o"))
        repo.insertRecipe(Recipe.create("New", "n"))
        val result = useCase().first()
        assertEquals(2, result.size)
    }
}
