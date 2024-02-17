package com.example.myapplication.recipes.presentation.login

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.recipes.presentation.editrecipe.RecipeTextFieldState
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val serverState = viewModel.server.value
    val usernameState = viewModel.username.value
    val passwordState = viewModel.password.value

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginViewModel.UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        message = "ERROR" // TODO fix... use event.message
                    )
                }
                is LoginViewModel.UiEvent.CheckLogin -> {
                    navController.navigateUp()
                }
            }
        }
    }

    LoginScreenContent(
        serverState = serverState,
        usernameState = usernameState,
        passwordState = passwordState,
        snackBarHostState = snackBarHostState,
        eventHandler = {
            viewModel.onEvent(it)
        },
        modifier = modifier
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreenContent(
    serverState: RecipeTextFieldState,
    usernameState: RecipeTextFieldState,
    passwordState: RecipeTextFieldState,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    eventHandler: (LoginEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    eventHandler(LoginEvent.Login)
                }
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = stringResource(id = R.string.save))
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                TextField(
                    value = serverState.text,
                    onValueChange = {
                        eventHandler(LoginEvent.EnteredServer(it))
                    },
                    placeholder = {
                        if (serverState.hintStringId != ID_NULL) {
                            Text(text = stringResource(serverState.hintStringId), style = MaterialTheme.typography.headlineMedium)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(LoginEvent.ChangeServerFocus(it))
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                TextField(
                    value = usernameState.text,
                    onValueChange = {
                        eventHandler(LoginEvent.EnteredUsername(it))
                    },
                    placeholder = {
                        if (usernameState.hintStringId != ID_NULL) {
                            Text(text = stringResource(usernameState.hintStringId), style = MaterialTheme.typography.headlineMedium)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(LoginEvent.ChangeUsernameFocus(it))
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                TextField(
                    value = passwordState.text,
                    onValueChange = {
                        eventHandler(LoginEvent.EnteredPassword(it))
                    },
                    placeholder = {
                        if (passwordState.hintStringId != ID_NULL) {
                            Text(text = stringResource(passwordState.hintStringId), style = MaterialTheme.typography.headlineMedium)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(LoginEvent.ChangePasswordFocus(it))
                        }
                )
            }
        }
    }
}

data class LoginState(
    val server: RecipeTextFieldState,
    val username: RecipeTextFieldState,
    val password: RecipeTextFieldState
)

class LoginScreenPreviewParameterProvider : PreviewParameterProvider<LoginState> {
    override val values = sequenceOf(
        LoginState(
            server = RecipeTextFieldState(text = "", hintStringId = R.string.title_hint, isHintVisible = true),
            username = RecipeTextFieldState(text = "", hintStringId = R.string.content_hint, isHintVisible = true),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.content_hint, isHintVisible = true)
        )
    )
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
@Composable
private fun AddEditRecipeScreenPreview(
    @PreviewParameter(LoginScreenPreviewParameterProvider::class) state: LoginState
) {
    MyApplicationTheme {
        LoginScreenContent(
            serverState = state.server,
            usernameState = state.username,
            passwordState = state.password,
            snackBarHostState = SnackbarHostState(),
            eventHandler = {}
        )
    }
}
