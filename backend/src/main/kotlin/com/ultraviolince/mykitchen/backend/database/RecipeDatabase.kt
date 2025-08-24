package com.ultraviolince.mykitchen.backend.database

import com.ultraviolince.mykitchen.backend.model.Recipe
import com.ultraviolince.mykitchen.backend.model.RecipeRequest
import com.ultraviolince.mykitchen.backend.model.User
import kotlinx.serialization.Serializable
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

@Serializable
data class RecipesData(
    val recipes: List<Recipe> = emptyList()
)

class RecipeDatabase(private val file: Path, createBackup: Boolean = false) {
    private var data: RecipesData
    private var nextId: Int
    private val yaml = Yaml(
        DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        }
    )

    init {
        // Create file if it doesn't exist
        if (!file.exists()) {
            file.writeText(yaml.dump(mapOf("recipes" to emptyList<Map<String, Any>>())))
        } else if (createBackup) {
            createBackup()
        }

        // Load data from file
        data = loadFromFile()

        // Calculate next ID
        nextId = if (data.recipes.isNotEmpty()) {
            data.recipes.maxOf { it.id } + 1
        } else {
            1
        }
    }

    private fun createBackup() {
        val backupDirectory = file.parent.resolve("backup")
        backupDirectory.createDirectories()
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))
        val backupFile = backupDirectory.resolve("${file.fileName.toString().substringBeforeLast(".")}-$date.yaml")
        File(file.toString()).copyTo(File(backupFile.toString()), overwrite = true)
    }

    @Suppress("UNCHECKED_CAST", "MagicNumber")
    private fun loadFromFile(): RecipesData {
        return try {
            val yamlContent = yaml.load<Map<String, Any>>(file.toFile().inputStream())
            val recipesList = yamlContent["recipes"] as? List<Map<String, Any>> ?: emptyList()
            val recipes = recipesList.map { recipeMap ->
                Recipe(
                    id = when (val id = recipeMap["id"]) {
                        is Int -> id
                        is String -> id.toInt()
                        else -> 0
                    },
                    title = recipeMap["title"] as? String ?: "",
                    body = recipeMap["body"] as? String ?: "",
                    timestamp = when (val ts = recipeMap["timestamp"]) {
                        is Long -> ts
                        is Int -> ts.toLong()
                        is String -> ts.toLong()
                        else -> System.currentTimeMillis() / 1000
                    },
                    user = recipeMap["user"] as? String ?: ""
                )
            }
            RecipesData(recipes)
        } catch (e: Exception) {
            println("Found database file cannot be validated! Creating a backup and a new database")
            createBackupOnError()
            val emptyData = RecipesData()
            saveToFile(emptyData)
            emptyData
        }
    }

    private fun createBackupOnError() {
        val backupDirectory = file.parent.resolve("backup")
        backupDirectory.createDirectories()
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))
        val backupFile = backupDirectory.resolve(
            "${file.fileName.toString().substringBeforeLast(".")}-incompatible-$date.yaml"
        )
        File(file.toString()).copyTo(File(backupFile.toString()), overwrite = true)
    }

    private fun saveToFile(recipesData: RecipesData) {
        val yamlMap = mapOf(
            "recipes" to recipesData.recipes.map { recipe ->
                mapOf(
                    "id" to recipe.id,
                    "title" to recipe.title,
                    "body" to recipe.body,
                    "timestamp" to recipe.timestamp,
                    "user" to recipe.user
                )
            }
        )
        file.writeText(yaml.dump(yamlMap))
        data = recipesData
    }

    fun get(user: User, all: Boolean): List<Recipe> {
        return if (all) {
            data.recipes
        } else {
            data.recipes.filter { it.user == user.name }
        }
    }

    fun put(user: User, recipeRequest: RecipeRequest): Pair<Recipe?, String?> {
        // Validate timestamp
        val timestamp = recipeRequest.timestamp ?: (System.currentTimeMillis() / 1000)

        // Handle ID
        val id = if (recipeRequest.id != null) {
            if (data.recipes.any { it.id == recipeRequest.id }) {
                return null to "Recipe with id ${recipeRequest.id} exists!"
            }
            recipeRequest.id
        } else {
            nextId.also { nextId++ }
        }

        // Validate title
        if (recipeRequest.title.trim().isEmpty()) {
            return null to "Recipe title can't be empty"
        }

        val newRecipe = Recipe(
            id = id,
            title = recipeRequest.title,
            body = recipeRequest.body,
            timestamp = timestamp,
            user = user.name
        )

        val updatedData = data.copy(recipes = data.recipes + newRecipe)
        saveToFile(updatedData)

        return newRecipe to null
    }

    fun delete(user: User, recipeId: Int): Pair<Boolean, Any> {
        val recipeIndex = data.recipes.indexOfFirst { it.id == recipeId }
        if (recipeIndex == -1) {
            return false to "There is no recipe with id $recipeId"
        }

        val recipe = data.recipes[recipeIndex]
        if (recipe.user != user.name) {
            return false to "Recipe $recipeId does not belong to you, you can't delete it!"
        }

        val updatedRecipes = data.recipes.toMutableList().apply { removeAt(recipeIndex) }
        val updatedData = data.copy(recipes = updatedRecipes)
        saveToFile(updatedData)

        return true to recipe
    }
}
