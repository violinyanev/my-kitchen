package com.ultraviolince.mykitchen.recipes.screenshot

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.presentation.recipes.ImmutableRecipesList
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeScreenContent
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipesState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [35], application = TestApplication::class)
class RecipesScreenshotTests {

    @Test
    fun recipeScreen_withSingleRecipe() {
        val state = RecipesState(
            ImmutableRecipesList(
                listOf(
                    Recipe(
                        "Recipe title",
                        content = "This is a long\nmultipline\ntext\nwith\nmany\nlines\nreally",
                        timestamp = 5
                    )
                )
            )
        )
        captureRecipeScreen(state, "recipeScreen_withSingleRecipe")
    }

    @Test
    fun recipeScreen_empty() {
        val state = RecipesState()
        captureRecipeScreen(state, "recipeScreen_empty")
    }

    @Test
    fun recipeScreen_withMultipleRecipes() {
        val state = RecipesState(
            recipes = ImmutableRecipesList(List(10) { index ->
                Recipe(
                    "Recipe $index",
                    content = "Lorem ipsum dolor sit amet $index",
                    timestamp = 5
                )
            })
        )
        captureRecipeScreen(state, "recipeScreen_withMultipleRecipes")
    }

    @Test
    fun recipeScreen_syncStateLoginEmpty() {
        val state = RecipesState(syncState = LoginState.LoginEmpty)
        captureRecipeScreen(state, "recipeScreen_syncStateLoginEmpty")
    }

    @Test
    fun recipeScreen_syncStateLoginPending() {
        val state = RecipesState(syncState = LoginState.LoginPending)
        captureRecipeScreen(state, "recipeScreen_syncStateLoginPending")
    }

    @Test
    fun recipeScreen_syncStateLoginSuccess() {
        val state = RecipesState(syncState = LoginState.LoginSuccess)
        captureRecipeScreen(state, "recipeScreen_syncStateLoginSuccess")
    }

    @Test
    fun recipeScreen_syncStateLoginFailure() {
        val state = RecipesState(syncState = LoginState.LoginFailure(error = NetworkError.UNAUTHORIZED))
        captureRecipeScreen(state, "recipeScreen_syncStateLoginFailure")
    }

    private fun captureRecipeScreen(state: RecipesState, fileName: String) {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).create().resume().get()
        
        activity.setContent {
            MyApplicationTheme {
                RecipeScreenContent(
                    onAddRecipe = {},
                    onLoginClick = {},
                    onSortClick = {},
                    onEvent = {},
                    onRecipeClicked = {},
                    recipeState = state
                )
            }
        }
        
        // Let compose settle and make sure the view hierarchy is ready
        Thread.sleep(100)
        
        val contentView = activity.findViewById<android.view.View>(android.R.id.content)
        requireNotNull(contentView) { "Content view is null" }
        contentView.captureRoboImage(filePath = "src/test/screenshots/$fileName.png")
    }
}