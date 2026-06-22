package com.ultraviolince.mykitchen.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.error_login_failed
import com.ultraviolince.mykitchen.ui.theme.AppTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true)
@Composable
internal fun LoginScreenPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        LoginScreenContent(
            state = LoginState(),
            onEmailChange = {},
            onPasswordChange = {},
            onServerUrlChange = {},
            onLogin = {},
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true, name = "Login With Error")
@Composable
internal fun LoginScreenErrorPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        LoginScreenContent(
            state = LoginState(email = "test@example.com", error = Res.string.error_login_failed),
            onEmailChange = {},
            onPasswordChange = {},
            onServerUrlChange = {},
            onLogin = {},
        )
    }
}
