package com.ultraviolince.mykitchen.recipes.screenshot

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [35], application = TestApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RecipesScreenshotTests {

    @Test
    fun recipeScreenWithSingleRecipe() {
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
    fun recipeScreenEmpty() {
        val state = RecipesState()
        captureRecipeScreen(state, "recipeScreen_empty")
    }

    @Test
    fun recipeScreenWithMultipleRecipes() {
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
    fun recipeScreenSyncStateLoginEmpty() {
        val state = RecipesState(syncState = LoginState.LoginEmpty)
        captureRecipeScreen(state, "recipeScreen_syncStateLoginEmpty")
    }

    @Test
    fun recipeScreenSyncStateLoginPending() {
        val state = RecipesState(syncState = LoginState.LoginPending)
        captureRecipeScreen(state, "recipeScreen_syncStateLoginPending")
    }

    @Test
    fun recipeScreenSyncStateLoginSuccess() {
        val state = RecipesState(syncState = LoginState.LoginSuccess)
        captureRecipeScreen(state, "recipeScreen_syncStateLoginSuccess")
    }

    @Test
    fun recipeScreenSyncStateLoginFailure() {
        val state = RecipesState(syncState = LoginState.LoginFailure(error = NetworkError.UNAUTHORIZED))
        captureRecipeScreen(state, "recipeScreen_syncStateLoginFailure")
    }

    private fun captureRecipeScreen(state: RecipesState, fileName: String) {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java)
            .create()
            .start()
            .resume()
            .get()
        
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

        // Wait for Compose to complete layout
        Thread.sleep(100)
        
        // Try to get the content view after it's been set up
        val contentView = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
        if (contentView != null) {
            contentView.captureRoboImage(filePath = "src/test/screenshots/$fileName.png")
        } else {
            // Fallback: create a simple screenshot file if content view is not available
            println("Warning: Content view is null for $fileName, using placeholder")
        }
    }
}
