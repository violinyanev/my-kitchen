package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.domain.repository.RecipeRepository

class Login(private val repository: RecipeRepository) {
    suspend operator fun invoke(server: String, username: String, password: String) {
        repository.login(server, username, password)
    }
}
