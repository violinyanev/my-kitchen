package com.ultraviolince.mykitchen.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeRoomDao {
    @Query("SELECT * FROM recipes WHERE deleted = 0 ORDER BY title ASC")
    fun getRecipesByTitle(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getRecipesByDate(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE deleted = 0")
    suspend fun getAllActive(): List<RecipeEntity>

    @Query("SELECT id FROM recipes WHERE deleted = 1 AND synced = 0")
    suspend fun getUnsyncedDeletedIds(): List<String>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Query("UPDATE recipes SET deleted = 1, synced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE recipes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM recipes WHERE deleted = 1 AND synced = 1")
    suspend fun clearSyncedDeleted()
}
