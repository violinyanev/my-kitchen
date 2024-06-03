package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository

class GetUser(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: Long): User? {
        return repository.getUserById(id)
    }
}
