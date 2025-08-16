package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new columns with default values
        db.execSQL("ALTER TABLE recipe ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'NOT_SYNCED'")
        db.execSQL("ALTER TABLE recipe ADD COLUMN lastSyncTimestamp INTEGER")
        db.execSQL("ALTER TABLE recipe ADD COLUMN syncErrorMessage TEXT")
    }
}

@Database(
    entities = [Recipe::class],
    version = 2,
    exportSchema = true

)
abstract class RecipeDatabase : RoomDatabase() {
    abstract val recipeDao: RecipeDao

    companion object {
        const val DATABASE_NAME = "recipedb"
    }
}
