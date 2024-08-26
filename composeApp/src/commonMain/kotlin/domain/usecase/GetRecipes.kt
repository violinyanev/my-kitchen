package domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import domain.model.Recipe
import domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// TODO kmp @Single
class GetRecipes(private val repository: RecipeRepository) {
    operator fun invoke(recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending)): Flow<List<Recipe>> {
        return repository.getRecipes().map { recipes ->
            when (recipeOrder.orderType) {
                is OrderType.Ascending -> {
                    when (recipeOrder) {
                        is RecipeOrder.Title -> recipes.sortedBy { it.title.lowercase() }
                        is RecipeOrder.Date -> recipes.sortedBy { it.timestamp }
                    }
                }
                is OrderType.Descending -> {
                    when (recipeOrder) {
                        is RecipeOrder.Title -> recipes.sortedByDescending { it.title.lowercase() }
                        is RecipeOrder.Date -> recipes.sortedByDescending { it.timestamp }
                    }
                }
            }
        }
    }
}
