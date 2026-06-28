package com.ultraviolince.mykitchen.data.local

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<RecipeDatabase> {
    val dbFile = context.getDatabasePath("recipe.db")
    return Room.databaseBuilder<RecipeDatabase>(
        name = dbFile.absolutePath,
    )
}
