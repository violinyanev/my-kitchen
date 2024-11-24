package domain.usecase

import domain.model.Recipe
import domain.repository.RecipeRepository

// TODO kmp @Single
class GetRecipe(
    private val repository: RecipeRepository,
) {
    suspend operator fun invoke(id: Long): Recipe? {
        return repository.getRecipeById(id)
    }
}
