package com.ultraviolince.mykitchen.recipes.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.captureRoboImage
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeEvent
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeScreenContent
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import com.ultraviolince.mykitchen.utils.RoborazziTestRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.ultraviolince.mykitchen.R

@RunWith(RoborazziTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class RoborazziRecipeTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun deleteRecipeButton_isDisplayedAndClickable() {
        val events = mutableListOf<AddEditRecipeEvent>()

        composeTestRule.setContent {
            MyApplicationTheme {
                val snackBarHostState = remember { SnackbarHostState() }
                AddEditRecipeScreenContent(
                    titleState = RecipeTextFieldState(
                        text = "Existing Recipe",
                        hintStringId = R.string.title_hint,
                        isHintVisible = false
                    ),
                    contentState = RecipeTextFieldState(
                        text = "This recipe exists",
                        hintStringId = R.string.content_hint,
                        isHintVisible = false
                    ),
                    snackBarHostState = snackBarHostState,
                    eventHandler = { event -> events.add(event) }
                )
            }
        }

        // Verify delete button is displayed
        composeTestRule.onNodeWithContentDescription("Delete recipe").assertIsDisplayed()
        
        // Capture before delete
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .captureRoboImage("delete_existing_recipe_before_click")

        // Click delete button
        composeTestRule.onNodeWithContentDescription("Delete recipe").performClick()
        
        // Capture after delete click
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .captureRoboImage("delete_existing_recipe_after_click")

        // Verify the delete event was triggered
        assert(events.any { it is AddEditRecipeEvent.DeleteRecipe }) {
            "Delete event should be triggered when delete button is clicked"
        }
    }

    @Test
    fun newRecipe_canEnterContentAndAbortWithDelete() {
        val events = mutableListOf<AddEditRecipeEvent>()

        composeTestRule.setContent {
            MyApplicationTheme {
                val snackBarHostState = remember { SnackbarHostState() }
                AddEditRecipeScreenContent(
                    titleState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.title_hint,
                        isHintVisible = true
                    ),
                    contentState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.content_hint,
                        isHintVisible = true
                    ),
                    snackBarHostState = snackBarHostState,
                    eventHandler = { event -> events.add(event) }
                )
            }
        }

        // Enter some content
        composeTestRule.onNodeWithContentDescription("Recipe title")
            .performTextInput("New Recipe")
        composeTestRule.onNodeWithContentDescription("Recipe content")
            .performTextInput("Recipe content")
        
        // Capture with content entered
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .captureRoboImage("new_recipe_with_content_before_abort")

        // Click delete to abort
        composeTestRule.onNodeWithContentDescription("Delete recipe").performClick()
        
        // Capture after abort
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .captureRoboImage("new_recipe_after_abort_delete")

        // Verify delete event was triggered (aborting the new recipe)
        assert(events.any { it is AddEditRecipeEvent.DeleteRecipe }) {
            "Delete event should abort new recipe creation"
        }
    }

    @Test
    fun newRecipe_canSaveRecipe() {
        val events = mutableListOf<AddEditRecipeEvent>()

        composeTestRule.setContent {
            MyApplicationTheme {
                val snackBarHostState = remember { SnackbarHostState() }
                AddEditRecipeScreenContent(
                    titleState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.title_hint,
                        isHintVisible = true
                    ),
                    contentState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.content_hint,
                        isHintVisible = true
                    ),
                    snackBarHostState = snackBarHostState,
                    eventHandler = { event -> events.add(event) }
                )
            }
        }

        // Enter recipe content
        composeTestRule.onNodeWithContentDescription("Recipe title")
            .performTextInput("Test Recipe")
        composeTestRule.onNodeWithContentDescription("Recipe content")
            .performTextInput("Test content")
        
        // Capture before save
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .captureRoboImage("create_recipe_before_save")

        // Save the recipe
        composeTestRule.onNodeWithContentDescription("Save recipe").performClick()
        
        // Capture after save
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .captureRoboImage("create_recipe_after_save")

        // Verify save event was triggered
        assert(events.any { it is AddEditRecipeEvent.SaveRecipe }) {
            "Save event should be triggered when save button is clicked"
        }
    }

    @Test
    fun editRecipeScreen_displaysAllUIElements() {
        val events = mutableListOf<AddEditRecipeEvent>()

        composeTestRule.setContent {
            MyApplicationTheme {
                val snackBarHostState = remember { SnackbarHostState() }
                AddEditRecipeScreenContent(
                    titleState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.title_hint,
                        isHintVisible = true
                    ),
                    contentState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.content_hint,
                        isHintVisible = true
                    ),
                    snackBarHostState = snackBarHostState,
                    eventHandler = { event -> events.add(event) }
                )
            }
        }

        // Verify all UI elements are displayed
        composeTestRule.onNodeWithContentDescription("Recipe title").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Recipe content").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete recipe").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Save recipe").assertIsDisplayed()
        
        // Capture the default state
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .captureRoboImage("edit_recipe_screen_default_state")
    }

    @Test
    fun editRecipeScreen_navigateBackScenario() {
        val events = mutableListOf<AddEditRecipeEvent>()

        composeTestRule.setContent {
            MyApplicationTheme {
                val snackBarHostState = remember { SnackbarHostState() }
                AddEditRecipeScreenContent(
                    titleState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.title_hint,
                        isHintVisible = true
                    ),
                    contentState = RecipeTextFieldState(
                        text = "",
                        hintStringId = R.string.content_hint,
                        isHintVisible = true
                    ),
                    snackBarHostState = snackBarHostState,
                    eventHandler = { event -> events.add(event) }
                )
            }
        }

        // Enter some content (simulating user starting to create a recipe)
        composeTestRule.onNodeWithContentDescription("Recipe title")
            .performTextInput("Unsaved Recipe")
        composeTestRule.onNodeWithContentDescription("Recipe content")
            .performTextInput("This should not be saved")
        
        // Capture the state with unsaved content
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .captureRoboImage("navigate_back_with_unsaved_content")

        // In a real scenario, user would navigate back here
        // We simulate this by just verifying the state hasn't triggered save
        assert(events.none { it is AddEditRecipeEvent.SaveRecipe }) {
            "Save event should not be triggered when navigating back without saving"
        }
    }
}