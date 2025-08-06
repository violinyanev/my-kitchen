package com.ultraviolince.mykitchen.recipes.screenshot

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreenContent
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreenState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [35], application = TestApplication::class)
class LoginScreenshotTests {

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
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).create().resume().get()

        activity.setContent {
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

        // Let compose settle and make sure the view hierarchy is ready
        Thread.sleep(100)

        val contentView = activity.findViewById<android.view.View>(android.R.id.content)
        requireNotNull(contentView) { "Content view is null" }
        contentView.captureRoboImage(filePath = "src/test/screenshots/$fileName.png")
    }
}
