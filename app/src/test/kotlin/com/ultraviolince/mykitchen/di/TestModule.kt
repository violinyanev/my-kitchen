package com.ultraviolince.mykitchen.di

import androidx.lifecycle.SavedStateHandle
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class TestModule {
    @Single
    fun provideSavedStateHandle(): SavedStateHandle {
        return SavedStateHandle()
    }

    @Single
    fun provideTestRecipeServiceWrapper(dao: RecipeDao, dataStore: SafeDataStore): RecipeServiceWrapper {
        val mock = mockk<RecipeServiceWrapper>(relaxed = true)
        val loginStateFlow = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

        every { mock.loginState } returns loginStateFlow

        coEvery { mock.login(any(), any(), any()) } coAnswers {
            loginStateFlow.emit(LoginState.LoginPending)
            kotlinx.coroutines.delay(100)
            loginStateFlow.emit(LoginState.LoginSuccess)
        }

        coEvery { mock.logout() } coAnswers {
            loginStateFlow.emit(LoginState.LoginEmpty)
        }

        coEvery { mock.insertRecipe(any(), any()) } returns true
        coEvery { mock.deleteRecipe(any()) } returns true

        return mock
    }
}
