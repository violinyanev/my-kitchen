package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.model.UserWithRecipes
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM user")
    fun getUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM recipe")
    fun getRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id =:id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Transaction
    @Query("SELECT * FROM User")
    suspend fun getUsersWithRecipes(): List<UserWithRecipes>
}
