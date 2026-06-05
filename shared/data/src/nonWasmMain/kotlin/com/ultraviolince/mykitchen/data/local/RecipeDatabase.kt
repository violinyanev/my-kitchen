package com.ultraviolince.mykitchen.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object RecipeDatabaseConstructor : RoomDatabaseConstructor<RecipeDatabase> {
    override fun initialize(): RecipeDatabase
}

@ConstructedBy(RecipeDatabaseConstructor::class)
@Database(entities = [RecipeEntity::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeRoomDao(): RecipeRoomDao
}
