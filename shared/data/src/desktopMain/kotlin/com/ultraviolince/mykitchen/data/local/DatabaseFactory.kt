package com.ultraviolince.mykitchen.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<RecipeDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".mykitchen/recipe.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<RecipeDatabase>(
        name = dbFile.absolutePath,
    )
}
