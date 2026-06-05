package com.ultraviolince.mykitchen.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<RecipeDatabase> {
    val dbFile = context.getDatabasePath("recipe.db")
    return Room.databaseBuilder<RecipeDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}
