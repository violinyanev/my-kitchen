import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.InvalidRecipeException
import domain.model.Recipe
import domain.usecase.Recipes
import editrecipe.presentation.AddEditRecipeEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import mykitchen.composeapp.generated.resources.Res
import mykitchen.composeapp.generated.resources.content_hint
import mykitchen.composeapp.generated.resources.title_hint
import shared.state.TextFieldState

// @KoinViewModel
class AddEditRecipeViewModel(
    private val recipesUseCases: Recipes
    // TODO kmp savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _recipeTitle = mutableStateOf(
        TextFieldState(
            hintStringId = Res.string.title_hint
        )
    )
    val recipeTitle: State<TextFieldState> = _recipeTitle

    private val _recipeContent = mutableStateOf(TextFieldState(hintStringId = Res.string.content_hint))
    val recipeContent: State<TextFieldState> = _recipeContent

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentRecipeId: Long? = null

    init {
        Log.i("Entering the edit recipe screen")

        // TODO remove this?
//        savedStateHandle.get<Int>("recipeId")?.let { recipeIdInt ->
//            val recipeId = recipeIdInt.toLong()
//            savedStateHandle["recipeId"] = recipeId
//            Log.i("Editing recipe with id=$recipeId")
//        }
//
//        savedStateHandle.get<Long>("recipeId")?.let {
//                recipeId ->
//            if (recipeId != -1L) {
//                viewModelScope.launch {
//                    recipesUseCases.getRecipe(recipeId)?.also {
//                            recipe ->
//                        currentRecipeId = recipe.id
//                        _recipeTitle.value = recipeTitle.value.copy(
//                            text = recipe.title,
//                            isHintVisible = false
//                        )
//                        _recipeContent.value = recipeContent.value.copy(
//                            text = recipe.content,
//                            isHintVisible = false
//                        )
//                    }
//                }
//            }
//        }
    }

    fun onEvent(event: AddEditRecipeEvent) {
        when (event) {
            is AddEditRecipeEvent.EnteredTitle -> {
                Log.i("User entered title ${event.value}")
                _recipeTitle.value = recipeTitle.value.copy(text = event.value)
            }
            is AddEditRecipeEvent.ChangeTitleFocus -> {
                _recipeTitle.value = recipeTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused && recipeTitle.value.text.isBlank()
                )
            }
            is AddEditRecipeEvent.EnteredContent -> {
                Log.i("User entered content ${event.value}")
                _recipeContent.value = recipeContent.value.copy(text = event.value)
            }
            is AddEditRecipeEvent.ChangeContentFocus -> {
                _recipeContent.value = recipeContent.value.copy(
                    isHintVisible = !event.focusState.isFocused && recipeContent.value.text.isBlank()
                )
            }
            is AddEditRecipeEvent.SaveRecipe -> {
                Log.i("User is saving the recipe")
                viewModelScope.launch {
                    try {
                        recipesUseCases.addRecipe(
                            Recipe(
                                title = recipeTitle.value.text,
                                content = recipeContent.value.text,
                                timestamp = 0L, // TODO kmp System.currentTimeMillis(),
                                id = currentRecipeId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveRecipe)
                    } catch (e: InvalidRecipeException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.errorString
                            )
                        )
                    }
                }
            }
            is AddEditRecipeEvent.DeleteRecipe -> {
                Log.i("User is deleting the recipe")
                viewModelScope.launch {
                    // TODO id is enough to pass here
                    recipesUseCases.deleteRecipe(
                        Recipe(
                            title = recipeTitle.value.text,
                            content = recipeContent.value.text,
                            timestamp = 0L, // TODO kmp System.currentTimeMillis(),
                            id = currentRecipeId
                        )
                    )
                    _eventFlow.emit(UiEvent.DeleteRecipe)
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent() // TODO kmp use string res
        object SaveRecipe : UiEvent()
        object DeleteRecipe : UiEvent()
    }
}
