package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository

class Login(private val repository: RecipeRepository) {
    suspend operator fun invoke(user: User, password: String?) {
        repository.login(user, password)
    }
}
