package com.ultraviolince.mykitchen.recipes.presentation.editrecipe

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.model.InvalidRecipeException
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AddEditRecipeViewModel(
    private val recipesUseCases: Recipes,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _recipeTitle = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.title_hint
        )
    )
    val recipeTitle: State<RecipeTextFieldState> = _recipeTitle

    private val _recipeContent = mutableStateOf(RecipeTextFieldState(hintStringId = R.string.content_hint))
    val recipeContent: State<RecipeTextFieldState> = _recipeContent

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentRecipeId: Long? = null

    init {
        Log.i("Recipes", "Entering the edit recipe screen")

        // TODO remove this?
        savedStateHandle.get<Int>("recipeId")?.let { recipeIdInt ->
            val recipeId = recipeIdInt.toLong()
            savedStateHandle["recipeId"] = recipeId
            Log.i("Recipes", "Editing recipe with id=$recipeId")
        }

        savedStateHandle.get<Long>("recipeId")?.let {
                recipeId ->
            if (recipeId != -1L) {
                viewModelScope.launch {
                    recipesUseCases.getRecipe(recipeId)?.also {
                            recipe ->
                        currentRecipeId = recipe.id
                        _recipeTitle.value = recipeTitle.value.copy(
                            text = recipe.title,
                            isHintVisible = false
                        )
                        _recipeContent.value = recipeContent.value.copy(
                            text = recipe.content,
                            isHintVisible = false
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditRecipeEvent) {
        when (event) {
            is AddEditRecipeEvent.EnteredTitle -> {
                Log.i("Recipes", "User entered title ${event.value}")
                _recipeTitle.value = recipeTitle.value.copy(text = event.value)
            }
            is AddEditRecipeEvent.ChangeTitleFocus -> {
                _recipeTitle.value = recipeTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused && recipeTitle.value.text.isBlank()
                )
            }
            is AddEditRecipeEvent.EnteredContent -> {
                Log.i("Recipes", "User entered content ${event.value}")
                _recipeContent.value = recipeContent.value.copy(text = event.value)
            }
            is AddEditRecipeEvent.ChangeContentFocus -> {
                _recipeContent.value = recipeContent.value.copy(
                    isHintVisible = !event.focusState.isFocused && recipeContent.value.text.isBlank()
                )
            }
            is AddEditRecipeEvent.SaveRecipe -> {
                Log.i("Recipes", "User is saving the recipe")
                viewModelScope.launch {
                    try {
                        recipesUseCases.addRecipe(
                            Recipe(
                                title = recipeTitle.value.text,
                                content = recipeContent.value.text,
                                timestamp = System.currentTimeMillis(),
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
                Log.i("Recipes", "User is deleting the recipe")
                viewModelScope.launch {
                    // TODO id is enough to pass here
                    recipesUseCases.deleteRecipe(
                        Recipe(
                            title = recipeTitle.value.text,
                            content = recipeContent.value.text,
                            timestamp = System.currentTimeMillis(),
                            id = currentRecipeId
                        )
                    )
                    _eventFlow.emit(UiEvent.DeleteRecipe)
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(@StringRes val message: Int) : UiEvent()
        object SaveRecipe : UiEvent()
        object DeleteRecipe : UiEvent()
    }
}
