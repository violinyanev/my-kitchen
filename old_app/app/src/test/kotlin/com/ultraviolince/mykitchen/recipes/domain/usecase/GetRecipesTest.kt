package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.data.repository.FakeRecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetRecipesTest {

    private lateinit var getRecipe: GetRecipe
    private lateinit var getRecipes: GetRecipes
    private lateinit var fakeRepository: FakeRecipeRepository

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        getRecipe = GetRecipe(fakeRepository)
        getRecipes = GetRecipes(fakeRepository)
    }

    @Test
    fun `Order recipes by title ascending, correct order`() = runBlocking {
        val recipes = getRecipes(RecipeOrder.Title(OrderType.Ascending)).first()

        for (i in 0..recipes.size - 2) {
            assertThat(recipes[i].title).isLessThan(recipes[i + 1].title)
        }
    }

    @Test
    fun `Order recipes by title descending, correct order`() = runBlocking {
        val recipes = getRecipes(RecipeOrder.Title(OrderType.Descending)).first()

        for (i in 0..recipes.size - 2) {
            assertThat(recipes[i].title).isGreaterThan(recipes[i + 1].title)
        }
    }

    @Test
    fun `Order recipes by date ascending, correct order`() = runBlocking {
        val recipes = getRecipes(RecipeOrder.Date(OrderType.Ascending)).first()

        for (i in 0..recipes.size - 2) {
            assertThat(recipes[i].timestamp).isLessThan(recipes[i + 1].timestamp)
        }
    }

    @Test
    fun `Order recipes by date descending, correct order`() = runBlocking {
        val recipes = getRecipes(RecipeOrder.Date(OrderType.Descending)).first()

        for (i in 0..recipes.size - 2) {
            assertThat(recipes[i].timestamp).isGreaterThan(recipes[i + 1].timestamp)
        }
    }

    @Test
    fun `Get recipe by id`() = runBlocking {
        val recipeId = fakeRepository.getTestRecipes()[0]
        val recipe = getRecipe(recipeId.id!!)

        assertThat(recipe).isNotNull()
        assertThat(recipe?.id).isEqualTo(recipeId.id!!)
        assertThat(recipe?.title).isEqualTo(recipeId.title)
    }
}
