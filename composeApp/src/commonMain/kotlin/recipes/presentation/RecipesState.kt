package recipes.presentation

import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import domain.model.Recipe
import domain.repository.LoginState

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending),
    val isOrderSelectionVisible: Boolean = false,
    val syncState: LoginState = LoginState.LoginEmpty,
)
