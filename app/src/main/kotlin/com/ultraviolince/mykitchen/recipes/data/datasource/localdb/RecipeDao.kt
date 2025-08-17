package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id =:id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Query("SELECT * FROM recipe WHERE syncStatus = :syncStatus")
    suspend fun getRecipesBySyncStatus(syncStatus: SyncStatus): List<Recipe>

    @Query("SELECT * FROM recipe WHERE syncStatus IN (:syncStatuses)")
    suspend fun getRecipesBySyncStatuses(syncStatuses: List<SyncStatus>): List<Recipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("UPDATE recipe SET syncStatus = :syncStatus, lastSyncTimestamp = :lastSyncTimestamp, syncErrorMessage = :syncErrorMessage WHERE id = :recipeId")
    suspend fun updateRecipeSyncStatus(
        recipeId: Long,
        syncStatus: SyncStatus,
        lastSyncTimestamp: Long? = null,
        syncErrorMessage: String? = null
    )

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)
}
