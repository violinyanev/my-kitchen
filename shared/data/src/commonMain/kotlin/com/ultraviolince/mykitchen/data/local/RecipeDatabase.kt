package com.ultraviolince.mykitchen.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

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
