package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity.Recipe

@Database(
    entities = [Recipe::class],
    version = 2,
    exportSchema = true

)
abstract class RecipeDatabase : RoomDatabase() {
    abstract val recipeDao: RecipeDao

    companion object {
        const val DATABASE_NAME = "recipedb"

        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Recipe ADD COLUMN imagePath TEXT")
            }
        }
    }
}
