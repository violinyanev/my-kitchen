package data.datasource.localdb

import domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// TODO room @Dao
interface RecipeDao {
    // TODO room @Query("SELECT * FROM recipe")
    fun getRecipes(): Flow<List<Recipe>>

    // TODO room @Query("SELECT * FROM recipe WHERE id =:id")
    suspend fun getRecipeById(id: Long): Recipe?

    // TODO room @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    // TODO room @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    // TODO room @Delete
    suspend fun deleteRecipe(recipe: Recipe)
}

class RecipeDaoImpl() : RecipeDao {
    private val recipesFlow = MutableStateFlow<List<Recipe>>(emptyList())

    init {
        // val r1 = Recipe(title = "test1", content = "content1", timestamp = 1L, id = 1)
        // val r2 = Recipe(title = "test2", content = "content2", timestamp = 1L, id = 2)
        // .value = listOf(r1, r2)
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return recipesFlow
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return recipesFlow.value.find { it.id == id }
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        Log.i("Inserting recipe $recipe")
        val newId = if (recipe.id <= 0L) createNewId() else recipe.id
        val updatedRecipes =
            recipesFlow.value.toMutableList().apply {
                add(recipe)
            }
        recipesFlow.value = updatedRecipes
        return newId
    }

    override suspend fun insertRecipes(recipes: List<Recipe>) {
        for (r in recipes) {
            insertRecipe(r)
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        val existingRecipe = getRecipeById(recipe.id!!)

        existingRecipe?.apply {
            val updatedRecipes =
                recipesFlow.value.toMutableList().apply {
                    remove(existingRecipe)
                }
            recipesFlow.value = updatedRecipes
        }
    }

    private fun createNewId(): Long {
        val maxId =
            recipesFlow.value.maxByOrNull {
                it.id
            }

        return maxId?.id?.let { it + 1 } ?: 1L
    }
}
