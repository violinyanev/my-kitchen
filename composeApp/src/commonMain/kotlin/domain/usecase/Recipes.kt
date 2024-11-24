package domain.usecase

// TODO kmp @Single
data class Recipes(
    val login: Login,
    val getSyncState: GetLoginState,
    val getRecipes: GetRecipes,
    val deleteRecipe: DeleteRecipe,
    val addRecipe: AddRecipe,
    val getRecipe: GetRecipe,
)
