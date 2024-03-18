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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
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
import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.recipes.presentation.util.Screen
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
                is LoginViewModel.UiEvent.CreateUser -> {
                    navController.navigate(Screen.CreateUserScreen.route)
                }
                is LoginViewModel.UiEvent.LoginSuccess -> {
                    navController.navigateUp()
                }
            }
        }
    }

    LoginScreenContent(
        passwordState = viewModel.password.value,
        snackBarHostState = snackBarHostState,
        stage = viewModel.stage.value,
        onSwitchUser = {
            navController.navigate(Screen.CreateUserScreen.route)
        },
        eventHandler = {
            viewModel.onEvent(it)
        },
        modifier = modifier
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreenContent(
    passwordState: RecipeTextFieldState,
    snackBarHostState: SnackbarHostState,
    stage: LoginScreenStage,
    onSwitchUser: () -> Unit,
    modifier: Modifier = Modifier,
    eventHandler: (LoginEvent) -> Unit
) {
    if(stage == LoginScreenStage.Loading)
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
                        if (stage is LoginScreenStage.EnterPassword) {
                            eventHandler(LoginEvent.Login)
                        }
                    },
                    modifier = Modifier.semantics { contentDescription = "Login" }
                ) {
                    if (stage is LoginScreenStage.AwaitServerResponse) {
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

                Row {
                    val userName =
                        (stage as? LoginScreenStage.EnterPassword)?.user?.name ?:
                        (stage as? LoginScreenStage.AwaitServerResponse)?.user?.name ?: "unknown"
                    Text ("Enter password for user")
                    Spacer(modifier = Modifier.width(16.dp))
                    Button (
                        onClick = {
                            onSwitchUser()
                        }
                    ) {
                        Text (userName)
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = stringResource(id = R.string.save)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    TextField(
                        value = "★".repeat(passwordState.text.length),
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
                            .semantics { contentDescription = "Password" }
                    )
                }
            }
        }
    }
}

class LoginScreenPreviewParameterProvider : PreviewParameterProvider<LoginScreenState> {
    val user = User(name = "pedro", email="", serverUri = "server.com", isDefault = true)
    override val values = sequenceOf(
        LoginScreenState(
            password = RecipeTextFieldState(),
            stage = LoginScreenStage.Loading
        ),
        LoginScreenState(
            password = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true),
            stage = LoginScreenStage.EnterPassword(user)
        ),
        LoginScreenState(
            password = RecipeTextFieldState(text = "Pedro"),
            stage = LoginScreenStage.EnterPassword(user)
        ),
        LoginScreenState(
            password = RecipeTextFieldState(text = "Pedro"),
            stage = LoginScreenStage.AwaitServerResponse(user),
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
            passwordState = state.password,
            stage = state.stage,
            onSwitchUser = {},
            snackBarHostState = SnackbarHostState(),
            eventHandler = {}
        )
    }
}
