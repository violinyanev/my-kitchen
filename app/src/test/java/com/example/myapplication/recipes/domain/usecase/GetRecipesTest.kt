package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.data.repository.FakeRecipeRepository
import com.example.myapplication.recipes.domain.util.OrderType
import com.example.myapplication.recipes.domain.util.RecipeOrder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetRecipesTest {

    private lateinit var login: Login
    private lateinit var fakeRepository: FakeRecipeRepository

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        login = Login(fakeRepository)
    }

    @Test
    fun `not logged in by default`() = runBlocking {
        fakeRepository.getLoginState().co
    }

}
