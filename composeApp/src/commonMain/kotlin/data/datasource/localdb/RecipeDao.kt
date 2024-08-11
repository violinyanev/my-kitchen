package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id =:id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Upsert
    suspend fun upsertRecipe(recipe: Recipe): Long

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)
}
