package domain.usecase

import domain.model.InvalidRecipeException
import domain.model.Recipe
import domain.repository.RecipeRepository

// TODO kmp @Single
class AddRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.title.isBlank()) {
            throw InvalidRecipeException("Missing recipe title") // TODO kmp (Res.string.missing_title)
        }

        if (recipe.content.isBlank()) {
            throw InvalidRecipeException("Recipe body can not be empty") // TODO kmp (Res.string.missing_body)
        }
        repository.insertRecipe(recipe)
    }
}
