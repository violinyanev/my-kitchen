package domain.model

import org.jetbrains.compose.resources.StringResource

class InvalidRecipeException(val errorString: StringResource) : Exception()
