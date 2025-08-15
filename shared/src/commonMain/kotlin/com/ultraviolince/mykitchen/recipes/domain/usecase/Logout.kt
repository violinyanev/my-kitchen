package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository


class Logout(private val repository: RecipeRepository) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
