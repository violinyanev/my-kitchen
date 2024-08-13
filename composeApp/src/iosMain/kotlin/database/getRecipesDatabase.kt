package database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

fun getRecipesDatabase(): RecipesDatabase {
    val dbFile = NSHomeDirectory() + "/recipes.db"
    return Room.databaseBuilder<RecipesDatabase>(
        name = dbFile,
        factory = { RecipesDatabase::class.instantiateImpl() }
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}