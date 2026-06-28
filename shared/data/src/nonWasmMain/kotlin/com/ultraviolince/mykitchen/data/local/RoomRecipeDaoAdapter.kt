package com.ultraviolince.mykitchen.data.local

import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRecipeDaoAdapter(private val roomDao: RecipeRoomDao) : RecipeDao {

    override fun getRecipesByTitle(): Flow<List<Recipe>> =
        roomDao.getRecipesByTitle().map { it.map { e -> e.toDomain() } }

    override fun getRecipesByDate(): Flow<List<Recipe>> =
        roomDao.getRecipesByDate().map { it.map { e -> e.toDomain() } }

    override suspend fun getAllActive(): List<Recipe> =
        roomDao.getAllActive().map { it.toDomain() }

    override suspend fun getUnsyncedDeletedIds(): List<String> =
        roomDao.getUnsyncedDeletedIds()

    override suspend fun getById(id: String): Recipe? =
        roomDao.getById(id)?.toDomain()

    override suspend fun insert(recipe: Recipe) =
        roomDao.insert(recipe.toEntity())

    override suspend fun insertAll(recipes: List<Recipe>) =
        roomDao.insertAll(recipes.map { it.toEntity() })

    override suspend fun softDelete(id: String) = roomDao.softDelete(id)

    override suspend fun markSynced(id: String) = roomDao.markSynced(id)

    override suspend fun clearSyncedDeleted() = roomDao.clearSyncedDeleted()
}
