package com.ultraviolince.mykitchen.recipes

import android.content.res.Configuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreenContent
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreenPreviewParameterProvider
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreenState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

class LoginScreenshotTests {
    @Preview(name = "Test", device = "spec:id=reference_test,shape=Normal,width=500,height=600,unit=dp,dpi=240", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
    @Composable
    fun Tests(@PreviewParameter(LoginScreenPreviewParameterProvider::class) state: LoginScreenState) {
        MyApplicationTheme {
            LoginScreenContent(
                serverState = state.server,
                usernameState = state.username,
                passwordState = state.password,
                snackBarHostState = SnackbarHostState(),
                buttonLoading = state.buttonLoading,
                eventHandler = {}
            )
        }
    }
}
