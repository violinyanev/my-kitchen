package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetLoginState(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<LoginState> {
        return repository.getLoginState()
    }
}
