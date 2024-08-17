package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository

//TODO kmp @Single
class Login(private val repository: RecipeRepository) {
    suspend operator fun invoke(server: String, username: String, password: String) {
        repository.login(server, username, password)
    }
}
