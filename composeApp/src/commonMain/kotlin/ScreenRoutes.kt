sealed class ScreenRoutes(val route: String) {
    data object LoginScreen : ScreenRoutes("login")

    data object RecipesScreen : ScreenRoutes("recipes")

    data object AddEditRecipeScreen : ScreenRoutes("add_edit_recipe")
}
