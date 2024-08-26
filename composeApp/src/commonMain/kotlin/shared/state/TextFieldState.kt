package shared.state

import mykitchen.composeapp.generated.resources.Res
import mykitchen.composeapp.generated.resources.empty
import org.jetbrains.compose.resources.StringResource

data class TextFieldState(
    val text: String = "",
    val isHintVisible: Boolean = false,
    val hintStringId: StringResource = Res.string.empty
)
