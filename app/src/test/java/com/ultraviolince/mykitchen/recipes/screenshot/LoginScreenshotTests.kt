package com.ultraviolince.mykitchen.recipes.screenshot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreenContent
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreenState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35], application = TestApplication::class)
class LoginScreenshotTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_loggedIn() {
        val state = LoginScreenState(
            isLoggedIn = true,
            server = RecipeTextFieldState(text = "", hintStringId = R.string.server_hint, isHintVisible = true),
            username = RecipeTextFieldState(text = "", hintStringId = R.string.username_hint, isHintVisible = true),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true)
        )
        captureLoginScreen(state, "loginScreen_loggedIn")
    }

    @Test
    fun loginScreen_notLoggedIn() {
        val state = LoginScreenState(
            server = RecipeTextFieldState(text = "", hintStringId = R.string.server_hint, isHintVisible = true),
            username = RecipeTextFieldState(text = "", hintStringId = R.string.username_hint, isHintVisible = true),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true)
        )
        captureLoginScreen(state, "loginScreen_notLoggedIn")
    }

    @Test
    fun loginScreen_loading() {
        val state = LoginScreenState(
            server = RecipeTextFieldState(),
            username = RecipeTextFieldState(),
            password = RecipeTextFieldState(),
            buttonLoading = true
        )
        captureLoginScreen(state, "loginScreen_loading")
    }

    private fun captureLoginScreen(state: LoginScreenState, fileName: String) {
        composeTestRule.setContent {
            MyApplicationTheme {
                LoginScreenContent(
                    serverState = state.server,
                    usernameState = state.username,
                    passwordState = state.password,
                    snackBarHostState = SnackbarHostState(),
                    buttonLoading = state.buttonLoading,
                    eventHandler = {},
                    isLoggedIn = state.isLoggedIn
                )
            }
        }
        
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/$fileName.png")
    }
}