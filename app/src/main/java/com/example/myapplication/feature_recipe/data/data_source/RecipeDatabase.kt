package com.example.myapplication.feature_recipe.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.feature_recipe.domain.model.Recipe

@Database(entities = [Recipe::class],
    version=1)
abstract class RecipeDatabase : RoomDatabase()
{
    abstract val recipeDao: RecipeDao
}
