package com.ultraviolince.mykitchen.recipes

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeScreenContent
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeScreenStatePreviewParameterProvider
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipesState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

class RecipesScreenshotTests {
    @Preview(name = "Test", device = "spec:id=reference_test,shape=Normal,width=500,height=600,unit=dp,dpi=240", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
    @Composable
    fun Tests(
        @PreviewParameter(RecipeScreenStatePreviewParameterProvider::class) recipesState: RecipesState ) {
        MyApplicationTheme {
            RecipeScreenContent(
                onAddRecipe = {},
                onLoginClick = {},
                onSortClick = {},
                onEvent = {},
                onRecipeClicked = {},
                recipeState = recipesState
            )
        }
    }
}
