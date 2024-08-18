sealed class ScreenRoutes(val route: String) {
    object LoginScreen : ScreenRoutes("login")
    object RecipesScreen : ScreenRoutes("recipes")
    object AddEditRecipeScreen : ScreenRoutes("add_edit_recipe")
}
