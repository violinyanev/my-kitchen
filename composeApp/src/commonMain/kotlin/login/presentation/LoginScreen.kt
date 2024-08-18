package login.presentation

import LoginViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import koinViewModel
import kotlinx.coroutines.flow.collectLatest
import mykitchen.composeapp.generated.resources.Res
import mykitchen.composeapp.generated.resources.empty
import mykitchen.composeapp.generated.resources.password_hint
import mykitchen.composeapp.generated.resources.save
import mykitchen.composeapp.generated.resources.server_hint
import mykitchen.composeapp.generated.resources.username_hint
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import shared.state.TextFieldState

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = koinViewModel(),
    modifier: Modifier = Modifier
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
                        message = event.message.toString()
                    )
                }
                is LoginViewModel.UiEvent.LoginSuccess -> {
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
        buttonLoading = viewModel.buttonLoading.value,
        eventHandler = {
            viewModel.onEvent(it)
        },
        modifier = modifier
    )
}

@Composable
fun LoginScreenContent(
    serverState: TextFieldState,
    usernameState: TextFieldState,
    passwordState: TextFieldState,
    snackBarHostState: SnackbarHostState,
    buttonLoading: Boolean,
    modifier: Modifier = Modifier,
    eventHandler: (LoginEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!buttonLoading) {
                        eventHandler(LoginEvent.Login)
                    }
                },
                modifier = Modifier.semantics { contentDescription = "Login" }
            ) {
                if (buttonLoading) {
                    val rotationAnimatable = remember {
                        Animatable(0f)
                    }
                    LaunchedEffect(Unit) {
                        rotationAnimatable.animateTo(
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .rotate(rotationAnimatable.value)
                            .then(modifier)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done, // TODO KMP Icons.Default.Autorenew,
                            contentDescription = stringResource(Res.string.save)
                        )
                    }
                } else {
                    Icon(imageVector = Icons.Default.Done, contentDescription = stringResource(Res.string.save))
                }
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
                        if (serverState.hintStringId != Res.string.empty) {
                            Text(text = stringResource(serverState.hintStringId), style = MaterialTheme.typography.subtitle1)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(LoginEvent.ChangeServerFocus(it))
                        }
                        .semantics { contentDescription = "Server URI" }
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
                        if (usernameState.hintStringId != Res.string.empty) {
                            Text(text = stringResource(usernameState.hintStringId), style = MaterialTheme.typography.subtitle1)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(LoginEvent.ChangeUsernameFocus(it))
                        }
                        .semantics { contentDescription = "User name" }
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
                        if (passwordState.hintStringId != Res.string.empty) {
                            Text(text = stringResource(passwordState.hintStringId), style = MaterialTheme.typography.subtitle1)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(LoginEvent.ChangePasswordFocus(it))
                        }
                        .semantics { contentDescription = "Password" }
                )
            }
        }
    }
}

data class LoginScreenState(
    val server: TextFieldState,
    val username: TextFieldState,
    val password: TextFieldState,
    val buttonLoading: Boolean = false
)

class LoginScreenPreviewParameterProvider : PreviewParameterProvider<LoginScreenState> {
    override val values = sequenceOf(
        LoginScreenState(
            server = TextFieldState(text = "", hintStringId = Res.string.server_hint, isHintVisible = true),
            username = TextFieldState(text = "", hintStringId = Res.string.username_hint, isHintVisible = true),
            password = TextFieldState(text = "", hintStringId = Res.string.password_hint, isHintVisible = true)
        ),
        LoginScreenState(
            server = TextFieldState(),
            username = TextFieldState(),
            password = TextFieldState(),
            buttonLoading = true
        )
    )
}

// TODO kmp
// @Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
// @Composable
// private fun AddEditRecipeScreenPreview(
//    @PreviewParameter(LoginScreenPreviewParameterProvider::class) state: LoginScreenState
// ) {
//    MyApplicationTheme {
//        LoginScreenContent(
//            serverState = state.server,
//            usernameState = state.username,
//            passwordState = state.password,
//            snackBarHostState = SnackbarHostState(),
//            buttonLoading = state.buttonLoading,
//            eventHandler = {}
//        )
//    }
// }
