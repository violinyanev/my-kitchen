package com.example.myapplication.recipes.domain.use_case

import com.example.myapplication.recipes.data.repository.FakeRecipeRepository
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.usecase.GetRecipes
import com.example.myapplication.recipes.domain.util.OrderType
import com.example.myapplication.recipes.domain.util.RecipeOrder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetRecipesTest {

    private lateinit var getRecipes: GetRecipes
    private lateinit var fakeRepository: FakeRecipeRepository

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        getRecipes = GetRecipes(fakeRepository)

        val recipesToInsert = mutableListOf<Recipe>()
        ('a'..'z').forEachIndexed { index, c ->
            recipesToInsert.add(
                Recipe(
                    title = c.toString(),
                    content = c.toString(),
                    timestamp = index.toLong(),
                    color = index
                )
            )
        }
        recipesToInsert.shuffle()
        runBlocking {
            recipesToInsert.forEach { fakeRepository.insertRecipe(it) }
        }
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
}
