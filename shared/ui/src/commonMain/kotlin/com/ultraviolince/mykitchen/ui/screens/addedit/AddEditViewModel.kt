package com.ultraviolince.mykitchen.ui.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.usecase.AddRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetEnrichmentUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetRecipeUseCase
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.error_title_required
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class AddEditState(
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val titleError: StringResource? = null,
    /** Server-generated beautification, if the background worker has produced one yet. */
    val enrichment: RecipeEnrichment? = null,
    val showBeautified: Boolean = false,
)

class AddEditViewModel(
    private val addRecipe: AddRecipeUseCase,
    private val getRecipe: GetRecipeUseCase,
    private val getEnrichment: GetEnrichmentUseCase,
    private val recipeId: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditState())
    val state: StateFlow<AddEditState> = _state.asStateFlow()

    init {
        if (recipeId != null) {
            loadRecipe(recipeId)
            loadEnrichment(recipeId)
        }
    }

    private fun loadRecipe(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val recipe = getRecipe(id)
            if (recipe != null) {
                _state.update { it.copy(title = recipe.title, content = recipe.content, isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadEnrichment(id: String) {
        viewModelScope.launch {
            val enrichment = getEnrichment(id).getOrNull() ?: return@launch
            _state.update { it.copy(enrichment = enrichment) }
        }
    }

    fun toggleBeautified() {
        _state.update { it.copy(showBeautified = !it.showBeautified) }
    }

    fun onTitleChange(title: String) {
        _state.update { it.copy(title = title, titleError = null) }
    }

    fun onContentChange(content: String) {
        _state.update { it.copy(content = content) }
    }

    fun save() {
        val title = _state.value.title.trim()
        if (title.isBlank()) {
            _state.update { it.copy(titleError = Res.string.error_title_required) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            addRecipe(title, _state.value.content.trim())
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}
