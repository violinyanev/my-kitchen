package com.example.myapplication.recipes.presentation.recipes

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.presentation.recipes.components.RecipeItem
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test

class RecipeItemTest {

    @get:Rule
    val rule: ComposeContentTestRule = createComposeRule()

    @Test
    fun testRecipeItem() {
        val title = "title text"
        val content = "content text"

        rule.setContent {
            MyApplicationTheme {
                RecipeItem(
                    Recipe(
                        title = title,
                        content = content,
                        timestamp = 1
                    )
                )
            }
        }

        rule.onNodeWithText(title).assertExists()
        rule.onNodeWithText(content).assertExists()
    }
}
