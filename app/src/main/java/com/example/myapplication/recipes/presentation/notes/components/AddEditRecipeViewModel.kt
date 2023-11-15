package com.example.myapplication.recipes.presentation.notes.components

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.recipes.domain.model.InvalidRecipeException
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.usecase.Recipes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditRecipeViewModel @Inject constructor (
    private val recipesUseCases: Recipes,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _recipeTitle = mutableStateOf(RecipeTextFieldState(
        hint = "Enter title..."
    ))
    val recipeTitle: State<RecipeTextFieldState> = _recipeTitle

    private val _recipeContent = mutableStateOf(RecipeTextFieldState(hint = "Enter some content..."))
    val recipeContent: State<RecipeTextFieldState> = _recipeContent

    private val _recipeColor = mutableStateOf(Recipe.Companion.recipeColors.random().toArgb())
    val recipeColor: State<Int> = _recipeColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentRecipeId: Int? = null

    init {
        savedStateHandle.get<Int>("recipeId")?.let {
            recipeId ->
            if (recipeId != -1) {
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
                        _recipeColor.value = recipeColor.value
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditRecipeEvent) {
        when (event) {
            is AddEditRecipeEvent.EnteredTitle -> {
                _recipeTitle.value = recipeTitle.value.copy(text = event.value)
            }
            is AddEditRecipeEvent.ChangeTitleFocus -> {
                _recipeTitle.value = recipeTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused && recipeTitle.value.text.isBlank()
                )
            }
            is AddEditRecipeEvent.EnteredContent -> {
                _recipeContent.value = recipeContent.value.copy(text = event.value)
            }
            is AddEditRecipeEvent.ChangeContentFocus -> {
                _recipeContent.value = recipeContent.value.copy(
                    isHintVisible = !event.focusState.isFocused && recipeContent.value.text.isBlank()
                )
            }
            is AddEditRecipeEvent.ChangeColor -> {
                _recipeColor.value = event.color
            }
            is AddEditRecipeEvent.SaveRecipe -> {
                viewModelScope.launch {
                    try {
                        recipesUseCases.addRecipe(
                            Recipe(
                                title = recipeTitle.value.text,
                                content = recipeContent.value.text,
                                timestamp = System.currentTimeMillis(),
                                id = currentRecipeId,
                                color = recipeColor.value
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveRecipe)
                    } catch (e: InvalidRecipeException) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(message = e.message ?: "Couldn't save recipe"))
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String): UiEvent()
        object SaveRecipe: UiEvent()
    }
}