import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.datasource.backend.util.NetworkError
import domain.model.LoginException
import domain.repository.LoginState
import domain.usecase.Recipes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import login.presentation.LoginEvent
import mykitchen.composeapp.generated.resources.Res
import mykitchen.composeapp.generated.resources.password_hint
import mykitchen.composeapp.generated.resources.server_hint
import mykitchen.composeapp.generated.resources.username_hint
import shared.state.TextFieldState

// @KoinViewModel
class LoginViewModel(
    private val recipesUseCases: Recipes
) : ViewModel() {
    private val _server = mutableStateOf(
        TextFieldState(
            text = "https://ultraviolince.com:8019",
            hintStringId = Res.string.server_hint
        )
    )
    val server: State<TextFieldState> = _server
    private val _username = mutableStateOf(
        TextFieldState(
            hintStringId = Res.string.username_hint,
            text = "test@user.com"
        )
    )
    val username: State<TextFieldState> = _username
    private val _buttonLoading = mutableStateOf(false)
    val buttonLoading: State<Boolean> = _buttonLoading

    private val _password = mutableStateOf(
        TextFieldState(
            hintStringId = Res.string.password_hint,
            text = "TestPassword"
        )
    )
    val password: State<TextFieldState> = _password

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EnteredServer -> {
                Log.i("User entered server name ${event.value}")
                _server.value = server.value.copy(text = event.value)
            }
            is LoginEvent.ChangeServerFocus -> {
                _server.value = server.value.copy(
                    isHintVisible = !event.focusState.isFocused && server.value.text.isBlank()
                )
            }
            is LoginEvent.EnteredUsername -> {
                Log.i("User entered user name ${event.value}")
                _username.value = username.value.copy(text = event.value)
            }
            is LoginEvent.ChangeUsernameFocus -> {
                _username.value = username.value.copy(
                    isHintVisible = !event.focusState.isFocused && username.value.text.isBlank()
                )
            }
            is LoginEvent.EnteredPassword -> {
                Log.i("User entered a password with length ${event.value.length}")
                _password.value = password.value.copy(text = event.value)
            }
            is LoginEvent.ChangePasswordFocus -> {
                _password.value = password.value.copy(
                    isHintVisible = !event.focusState.isFocused && password.value.text.isBlank()
                )
            }
            is LoginEvent.Login -> {
                viewModelScope.launch {
                    UiEvent.ShowSnackbar(
                        message = "" // Res.string.server_hint
                    )

                    try {
                        recipesUseCases.login(
                            server = server.value.text,
                            username = username.value.text,
                            password = password.value.text
                        )
                        recipesUseCases.getSyncState().collect {
                            when (it) {
                                is LoginState.LoginSuccess -> {
                                    _eventFlow.emit(
                                        UiEvent.LoginSuccess
                                    )
                                }
                                LoginState.LoginEmpty -> {
                                    _buttonLoading.value = false
                                }
                                is LoginState.LoginFailure -> {
                                    _buttonLoading.value = false
                                    _eventFlow.emit(
                                        UiEvent.ShowSnackbar(
                                            when (it.error) {
                                                // TODO fix all responses
                                                NetworkError.UNKNOWN -> "Unknown error" // Res.string.unknown_error
                                                NetworkError.REQUEST_TIMEOUT -> "Malformed server URI" // Res.string.malformed_server_uri
                                                NetworkError.UNAUTHORIZED -> "Unknown error" // Res.string.unknown_error
                                                NetworkError.CONFLICT -> "Unknown error" // Res.string.unknown_error
                                                NetworkError.TOO_MANY_REQUESTS -> "Unknown error" // Res.string.unknown_error
                                                NetworkError.NO_INTERNET -> "Unknown error" // Res.string.unknown_error
                                                NetworkError.PAYLOAD_TOO_LARGE -> "Unknown error" // Res.string.unknown_error
                                                NetworkError.SERVER_ERROR -> "Malformed server URI" // Res.string.malformed_server_uri
                                                NetworkError.SERIALIZATION -> "Unknown error" // Res.string.unknown_error
                                            }
                                        )
                                    )
                                }
                                LoginState.LoginPending -> {
                                    _buttonLoading.value = true
                                }
                            }
                        }
                    } catch (e: LoginException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = "Exception happened $e" // e.errorMsg
                            )
                        )
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        // data class ShowSnackbar(val message: StringResource) : UiEvent() // TODO kmp
        data class ShowSnackbar(val message: String) : UiEvent()
        data object LoginSuccess : UiEvent()
    }
}
