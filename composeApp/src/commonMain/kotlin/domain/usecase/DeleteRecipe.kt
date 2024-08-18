package domain.usecase

import domain.model.Recipe
import domain.repository.RecipeRepository

// TODO kmp @Single
class DeleteRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.deleteRecipe(recipe)
    }
}
