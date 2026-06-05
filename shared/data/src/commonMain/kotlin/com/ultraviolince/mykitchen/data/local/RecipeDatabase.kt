package com.ultraviolince.mykitchen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RecipeEntity::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}
