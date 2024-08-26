package recipes.presentation

import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import domain.model.Recipe

sealed class RecipesEvent {
    data class Order(val recipesOrder: RecipeOrder) : RecipesEvent()
    data class DeleteRecipe(val recipe: Recipe) : RecipesEvent()

    data object RestoreRecipe : RecipesEvent()
    data object ToggleOrderSection : RecipesEvent()
}
