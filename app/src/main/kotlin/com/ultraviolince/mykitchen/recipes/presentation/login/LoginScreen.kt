package com.ultraviolince.mykitchen.recipes.presentation.login

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.presentation.common.components.AnimatedLoadingButton
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.recipes.presentation.login.components.LoginFormFields
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel()
) {
    val isLoggedIn = viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val serverState = viewModel.server.value
    val usernameState = viewModel.username.value
    val passwordState = viewModel.password.value

    val snackBarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginViewModel.UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        message = context.resources.getString(event.message)
                    )
                }
                is LoginViewModel.UiEvent.LoginSuccess -> {
                    navController.navigateUp()
                }
            }
        }
    }

    LoginScreenContent(
        isLoggedIn = isLoggedIn.value,
        serverState = serverState,
        usernameState = usernameState,
        passwordState = passwordState,
        snackBarHostState = snackBarHostState,
        buttonLoading = viewModel.buttonLoading.value,
        eventHandler = {
            viewModel.onEvent(it)
        },
        modifier = modifier
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreenContent(
    isLoggedIn: Boolean,
    serverState: RecipeTextFieldState,
    usernameState: RecipeTextFieldState,
    passwordState: RecipeTextFieldState,
    snackBarHostState: SnackbarHostState,
    buttonLoading: Boolean,
    modifier: Modifier = Modifier,
    eventHandler: (LoginEvent) -> Unit
) {

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            AnimatedLoadingButton(
                onClick = { eventHandler(LoginEvent.Login) },
                isLoading = buttonLoading,
                defaultIcon = Icons.Default.Done,
                contentDescriptionText = "Login"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LoginFormFields(
            isLoggedIn = isLoggedIn,
            serverState = serverState,
            usernameState = usernameState,
            passwordState = passwordState,
            onEvent = eventHandler,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

data class LoginScreenState(
    val isLoggedIn: Boolean = false,
    val server: RecipeTextFieldState,
    val username: RecipeTextFieldState,
    val password: RecipeTextFieldState,
    val buttonLoading: Boolean = false
)

class LoginScreenPreviewParameterProvider : PreviewParameterProvider<LoginScreenState> {
    override val values = sequenceOf(
        LoginScreenState(
            isLoggedIn = true,
            server = RecipeTextFieldState(text = "", hintStringId = R.string.server_hint, isHintVisible = true),
            username = RecipeTextFieldState(text = "", hintStringId = R.string.username_hint, isHintVisible = true),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true)
        ),
        LoginScreenState(
            server = RecipeTextFieldState(text = "", hintStringId = R.string.server_hint, isHintVisible = true),
            username = RecipeTextFieldState(text = "", hintStringId = R.string.username_hint, isHintVisible = true),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true)
        ),
        LoginScreenState(
            server = RecipeTextFieldState(),
            username = RecipeTextFieldState(),
            password = RecipeTextFieldState(),
            buttonLoading = true
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun LoginScreenPreview(
    @PreviewParameter(LoginScreenPreviewParameterProvider::class) state: LoginScreenState
) {
    MyApplicationTheme {
        LoginScreenContent(
            isLoggedIn = state.isLoggedIn,
            serverState = state.server,
            usernameState = state.username,
            passwordState = state.password,
            snackBarHostState = SnackbarHostState(),
            buttonLoading = state.buttonLoading,
            eventHandler = {}
        )
    }
}
