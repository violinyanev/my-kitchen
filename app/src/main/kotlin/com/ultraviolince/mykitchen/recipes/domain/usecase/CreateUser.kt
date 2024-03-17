package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.model.InvalidRecipeException
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository

class CreateUser(private val repository: RecipeRepository) {
    suspend operator fun invoke(user: User) {
        if (user.serverUri.isBlank()) {
            throw InvalidRecipeException(R.string.malformed_server_uri)
        }

        if (user.name.isBlank()) {
            throw InvalidRecipeException(R.string.missing_username)
        }

        if (user.email.isBlank()) {
            throw InvalidRecipeException(R.string.missing_email)
        }
        repository.insertUser(user)
    }
}
