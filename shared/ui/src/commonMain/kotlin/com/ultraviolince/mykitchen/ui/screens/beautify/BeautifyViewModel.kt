package com.ultraviolince.mykitchen.ui.screens.beautify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.usecase.BeautifyRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.DeleteEnrichmentUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetEnrichmentUseCase
import com.ultraviolince.mykitchen.domain.usecase.RefineFeedbackUseCase
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.error_beautify_failed
import com.ultraviolince.mykitchen.ui.generated.resources.error_refine_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

sealed interface BeautifyUiState {
    data object Loading : BeautifyUiState
    data class Reviewing(
        val enrichment: RecipeEnrichment,
        val isRefining: Boolean = false,
        val error: StringResource? = null,
    ) : BeautifyUiState
    data class Error(val message: StringResource) : BeautifyUiState
}

class BeautifyViewModel(
    private val beautify: BeautifyRecipeUseCase,
    private val getEnrichment: GetEnrichmentUseCase,
    private val refine: RefineFeedbackUseCase,
    private val deleteEnrichment: DeleteEnrichmentUseCase,
    private val recipeId: String,
) : ViewModel() {

    private val _state = MutableStateFlow<BeautifyUiState>(BeautifyUiState.Loading)
    val state: StateFlow<BeautifyUiState> = _state.asStateFlow()

    init {
        loadOrBeautify()
    }

    private fun loadOrBeautify() {
        viewModelScope.launch {
            _state.value = BeautifyUiState.Loading
            val existing = getEnrichment(recipeId)
            val enrichment = existing.getOrNull()
            if (enrichment != null) {
                _state.value = BeautifyUiState.Reviewing(enrichment)
            } else {
                beautifyRecipe()
            }
        }
    }

    fun beautifyRecipe() {
        viewModelScope.launch {
            _state.value = BeautifyUiState.Loading
            val result = beautify(recipeId)
            _state.value = if (result.isSuccess) {
                BeautifyUiState.Reviewing(result.getOrNull()!!)
            } else {
                BeautifyUiState.Error(Res.string.error_beautify_failed)
            }
        }
    }

    fun refineEnrichment(feedback: String) {
        val current = _state.value as? BeautifyUiState.Reviewing ?: return
        viewModelScope.launch {
            _state.update { (it as? BeautifyUiState.Reviewing)?.copy(isRefining = true) ?: it }
            val result = refine(recipeId, feedback)
            _state.value = if (result.isSuccess) {
                BeautifyUiState.Reviewing(result.getOrNull()!!)
            } else {
                current.copy(isRefining = false, error = Res.string.error_refine_failed)
            }
        }
    }

    fun rejectEnrichment(onDone: () -> Unit) {
        viewModelScope.launch {
            deleteEnrichment(recipeId)
            onDone()
        }
    }

    fun clearError() {
        _state.update { (it as? BeautifyUiState.Reviewing)?.copy(error = null) ?: it }
    }
}
