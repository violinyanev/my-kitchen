package com.ultraviolince.mykitchen.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object RecipeDatabaseConstructor : RoomDatabaseConstructor<RecipeDatabase>

@ConstructedBy(RecipeDatabaseConstructor::class)
@Database(entities = [RecipeEntity::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeRoomDao(): RecipeRoomDao
}
