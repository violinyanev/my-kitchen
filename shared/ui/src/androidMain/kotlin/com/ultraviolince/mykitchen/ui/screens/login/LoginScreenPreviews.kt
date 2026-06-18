package com.ultraviolince.mykitchen.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.error_login_failed
import com.ultraviolince.mykitchen.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
internal fun LoginScreenPreview() {
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

@Preview(showBackground = true, name = "Login With Error")
@Composable
internal fun LoginScreenErrorPreview() {
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
