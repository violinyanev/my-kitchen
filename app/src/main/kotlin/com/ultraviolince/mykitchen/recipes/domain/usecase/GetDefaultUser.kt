package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDefaultUser(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<User?> {
        return repository.getUsers().map { users ->
            users.firstOrNull { user ->
                user.isDefault
            }
        }
    }
}
