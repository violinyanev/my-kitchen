package com.example.myapplication.recipes.data.datasource.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.recipes.domain.model.Recipe

@Database(
    entities = [Recipe::class],
    version = 1,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract val recipeDao: RecipeDao

    companion object {
        const val DATABASE_NAME = "recipedb"
    }
}
