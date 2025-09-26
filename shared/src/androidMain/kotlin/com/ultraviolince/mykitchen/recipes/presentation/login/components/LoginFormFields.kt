package com.ultraviolince.mykitchen.recipes.presentation.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.shared.R
import com.ultraviolince.mykitchen.recipes.presentation.common.components.AppPasswordField
import com.ultraviolince.mykitchen.recipes.presentation.common.components.AppTextField
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginEvent
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun LoginFormFields(
    isLoggedIn: Boolean,
    serverState: RecipeTextFieldState,
    usernameState: RecipeTextFieldState,
    passwordState: RecipeTextFieldState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            state = serverState,
            onValueChange = { onEvent(LoginEvent.EnteredServer(it)) },
            onFocusChanged = { onEvent(LoginEvent.ChangeServerFocus(it)) },
            contentDescriptionText = "Server URI",
            textStyle = MaterialTheme.typography.headlineMedium,
            readOnly = isLoggedIn
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            state = usernameState,
            onValueChange = { onEvent(LoginEvent.EnteredUsername(it)) },
            onFocusChanged = { onEvent(LoginEvent.ChangeUsernameFocus(it)) },
            contentDescriptionText = "User name",
            textStyle = MaterialTheme.typography.headlineMedium,
            readOnly = isLoggedIn
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoggedIn) {
            Button(
                onClick = { onEvent(LoginEvent.Logout) },
                modifier = Modifier.semantics { contentDescription = "Logout" }
            ) {
                Text(text = stringResource(R.string.logout))
            }
        } else {
            AppPasswordField(
                state = passwordState,
                onValueChange = { onEvent(LoginEvent.EnteredPassword(it)) },
                onFocusChanged = { onEvent(LoginEvent.ChangePasswordFocus(it)) },
                contentDescriptionText = "Password",
                textStyle = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

data class LoginFormState(
    val isLoggedIn: Boolean,
    val server: RecipeTextFieldState,
    val username: RecipeTextFieldState,
    val password: RecipeTextFieldState
)

class LoginFormFieldsPreviewParameterProvider : PreviewParameterProvider<LoginFormState> {
    override val values = sequenceOf(
        LoginFormState(
            isLoggedIn = false,
            server = RecipeTextFieldState(text = "", hintStringId = R.string.server_hint, isHintVisible = true),
            username = RecipeTextFieldState(text = "", hintStringId = R.string.username_hint, isHintVisible = true),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true)
        ),
        LoginFormState(
            isLoggedIn = true,
            server = RecipeTextFieldState(text = "https://my-server.com", hintStringId = R.string.server_hint, isHintVisible = false),
            username = RecipeTextFieldState(text = "john_doe", hintStringId = R.string.username_hint, isHintVisible = false),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true)
        )
    )
}

@Preview(showBackground = true)
@Composable
internal fun LoginFormFieldsPreview(
    @PreviewParameter(LoginFormFieldsPreviewParameterProvider::class) state: LoginFormState
) {
    MyApplicationTheme {
        LoginFormFields(
            isLoggedIn = state.isLoggedIn,
            serverState = state.server,
            usernameState = state.username,
            passwordState = state.password,
            onEvent = {}
        )
    }
}
