package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.CloudSyncState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetLoginState(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<CloudSyncState> {
        return repository.getLoginState()
    }
}
