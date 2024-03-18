package com.ultraviolince.mykitchen.recipes.domain.repository

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.User
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    // Recipes
    fun getRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(id: Long): Recipe?
    suspend fun insertRecipe(recipe: Recipe): Long
    suspend fun deleteRecipe(recipe: Recipe)

    // Login
    suspend fun login(user: User, password: String?)
    fun getLoginState(): Flow<LoginState>
    fun getUsers(): Flow<List<User>>
    suspend fun insertUser(user: User): Long
    suspend fun deleteUser(user: User)
}
