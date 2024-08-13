package com.ultraviolince.mykitchen.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import database.RecipesDatabase

fun getRecipesDatabase(context: Context): RecipesDatabase {
    val dbFile = context.getDatabasePath("recipes.db")
    return Room.databaseBuilder<RecipesDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}