package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.User

@Database(
    entities = [Recipe::class, User::class],
    version = 2,
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract val recipeDao: RecipeDao

    companion object {
        const val DATABASE_NAME = "recipedb"
    }
}
