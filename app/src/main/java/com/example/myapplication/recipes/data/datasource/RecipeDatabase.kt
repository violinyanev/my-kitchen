package com.example.myapplication.recipes.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.recipes.domain.model.Recipe

@Database(
    entities = [Recipe::class],
    version = 1
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract val recipeDao: RecipeDao
}
