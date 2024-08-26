package domain.model

import org.jetbrains.compose.resources.StringResource

class LoginException(val errorMsg: StringResource) : Exception()
