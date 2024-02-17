package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.domain.repository.LoginState
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetSyncState(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<LoginState> {
        return repository.getLoginState()
    }
}
