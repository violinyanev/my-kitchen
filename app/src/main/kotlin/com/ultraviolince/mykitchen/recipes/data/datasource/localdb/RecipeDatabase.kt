package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe

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
