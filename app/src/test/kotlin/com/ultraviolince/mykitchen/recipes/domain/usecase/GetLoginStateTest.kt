package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.data.repository.FakeRecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetLoginStateTest {

    private lateinit var getLoginState: GetLoginState
    private lateinit var fakeRepository: FakeRecipeRepository

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        getLoginState = GetLoginState(fakeRepository)
    }

    @Test
    fun `getLoginState returns flow from repository`() = runBlocking {
        val loginState = getLoginState().first()

        // FakeRecipeRepository returns LoginEmpty by default
        assertThat(loginState).isEqualTo(LoginState.LoginEmpty)
    }

    @Test
    fun `getLoginState returns correct flow type`() = runBlocking {
        val flow = getLoginState()

        assertThat(flow).isNotNull()
        // Verify we can collect from the flow
        val loginState = flow.first()
        assertThat(loginState).isInstanceOf(LoginState::class.java)
    }
}
