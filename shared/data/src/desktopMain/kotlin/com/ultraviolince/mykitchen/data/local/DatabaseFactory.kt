package com.ultraviolince.mykitchen.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<RecipeDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".mykitchen/recipe.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<RecipeDatabase>(
        name = dbFile.absolutePath,
    )
}
