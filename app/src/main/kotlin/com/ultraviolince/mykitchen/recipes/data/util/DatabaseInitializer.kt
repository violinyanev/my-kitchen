package com.example.myapplication.recipes.data.util

import com.example.myapplication.recipes.data.datasource.RecipeDatabase
import com.example.myapplication.recipes.domain.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object DatabaseInitializer {
    private val sampleData = arrayOf(
        Recipe(
            title = "Delicious Chocolate Cake",
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
                    "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud" +
                    "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            color = 3,
            timestamp = 1
        ),
        Recipe(
            title = "Carrot on a stick",
            content = "Whatsup doc... Feeling like a lonely tune?",
            color = 3,
            timestamp = 1
        )
    )

    suspend fun populateDatabase(database: RecipeDatabase) {
        withContext(Dispatchers.IO) {
            sampleData.forEach { entity ->
                database.recipeDao.insertRecipe(entity)
            }
        }
    }
}
