package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetUsers(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<List<User>> {
        return repository.getUsers()
    }
}
