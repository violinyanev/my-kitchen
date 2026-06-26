package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetRecipeUseCaseTest {

    @Test
    fun `returns recipe by id`() = runTest {
        val repo = FakeRecipeRepository()
        val r = Recipe.create("Pasta", "Boil water")
        repo.insertRecipe(r)
        val result = GetRecipeUseCase(repo)(r.id)
        assertEquals(r, result)
    }

    @Test
    fun `returns null for missing id`() = runTest {
        val result = GetRecipeUseCase(FakeRecipeRepository())("no-such-id")
        assertNull(result)
    }
}
