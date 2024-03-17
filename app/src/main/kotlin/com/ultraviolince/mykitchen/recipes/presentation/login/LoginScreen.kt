package com.ultraviolince.mykitchen.recipes.presentation.login

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val snackBarHostState = remember { SnackbarHostState() }

    // TODO better way?
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
        serverState = viewModel.server.value,
        usernameState = viewModel.username.value,
        emailState = viewModel.email.value,
        passwordState = viewModel.password.value,
        snackBarHostState = snackBarHostState,
        stage = viewModel.stage.value,
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
    serverState: RecipeTextFieldState,
    usernameState: RecipeTextFieldState,
    emailState: RecipeTextFieldState,
    passwordState: RecipeTextFieldState,
    snackBarHostState: SnackbarHostState,
    stage: LoginScreenStage,
    buttonLoading: Boolean,
    modifier: Modifier = Modifier,
    eventHandler: (CreateUserEvent) -> Unit
) {
    if(stage == LoginScreenStage.LOADING)
    {
        Box (
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
    else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (!buttonLoading) {
                            eventHandler(CreateUserEvent.Login)
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
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = stringResource(id = R.string.save)
                            )
                        }
                    } else {
                        Icon(imageVector = Icons.Default.Done, contentDescription = stringResource(id = R.string.save))
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

                if(stage == LoginScreenStage.CREATE_USER) {
                    Box {
                        TextField(
                            value = serverState.text,
                            onValueChange = {
                                eventHandler(CreateUserEvent.EnteredServer(it))
                            },
                            placeholder = {
                                if (serverState.hintStringId != ID_NULL) {
                                    Text(
                                        text = stringResource(serverState.hintStringId),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    eventHandler(CreateUserEvent.ChangeServerFocus(it))
                                }
                                .semantics { contentDescription = "Server URI" }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box {
                        TextField(
                            value = emailState.text,
                            onValueChange = {
                                eventHandler(CreateUserEvent.EnteredEmail(it))
                            },
                            placeholder = {
                                if (emailState.hintStringId != ID_NULL) {
                                    Text(
                                        text = stringResource(emailState.hintStringId),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    eventHandler(CreateUserEvent.ChangeEmailFocus(it))
                                }
                                .semantics { contentDescription = "E-Mail" }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box {
                        TextField(
                            value = usernameState.text,
                            onValueChange = {
                                eventHandler(CreateUserEvent.EnteredUsername(it))
                            },
                            placeholder = {
                                if (usernameState.hintStringId != ID_NULL) {
                                    Text(
                                        text = stringResource(usernameState.hintStringId),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    eventHandler(CreateUserEvent.ChangeUsernameFocus(it))
                                }
                                .semantics { contentDescription = "User name" }
                        )
                    }
                }
                else {
                    Text(text = "Enter password for ${usernameState.text}:")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    TextField(
                        value = passwordState.text,
                        onValueChange = {
                            eventHandler(CreateUserEvent.EnteredPassword(it))
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
                                eventHandler(CreateUserEvent.ChangePasswordFocus(it))
                            }
                            .semantics { contentDescription = "Password" }
                    )
                }
            }
        }
    }
}

class LoginScreenPreviewParameterProvider : PreviewParameterProvider<LoginScreenState> {
    override val values = sequenceOf(
        LoginScreenState(
            server = RecipeTextFieldState(),
            username = RecipeTextFieldState(),
            email = RecipeTextFieldState(),
            password = RecipeTextFieldState(),
            stage = LoginScreenStage.LOADING
        ),
        LoginScreenState(
            server = RecipeTextFieldState(text = "", hintStringId = R.string.server_hint, isHintVisible = true),
            username = RecipeTextFieldState(text = "", hintStringId = R.string.username_hint, isHintVisible = true),
            email = RecipeTextFieldState(text = "", hintStringId = R.string.email_hint, isHintVisible = true),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true),
            stage = LoginScreenStage.CREATE_USER
        ),
        LoginScreenState(
            server = RecipeTextFieldState(),
            username = RecipeTextFieldState(),
            email = RecipeTextFieldState(),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true),
            stage = LoginScreenStage.ENTER_PASSWORD
        ),
        LoginScreenState(
            server = RecipeTextFieldState(),
            username = RecipeTextFieldState(),
            email = RecipeTextFieldState(),
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true),
            stage = LoginScreenStage.ENTER_PASSWORD
        ),
        LoginScreenState(
            server = RecipeTextFieldState(),
            username = RecipeTextFieldState(),
            email = RecipeTextFieldState(),
            password = RecipeTextFieldState(text = "Pedro"),
            stage = LoginScreenStage.ENTER_PASSWORD,
            buttonLoading = true
        )
    )
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
@Composable
private fun AddEditRecipeScreenPreview(
    @PreviewParameter(LoginScreenPreviewParameterProvider::class) state: LoginScreenState
) {
    MyApplicationTheme {
        LoginScreenContent(
            serverState = state.server,
            usernameState = state.username,
            emailState = state.email,
            passwordState = state.password,
            stage = state.stage,
            snackBarHostState = SnackbarHostState(),
            buttonLoading = state.buttonLoading,
            eventHandler = {}
        )
    }
}
