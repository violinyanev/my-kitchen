package com.ultraviolince.mykitchen.recipes.presentation.addrecipe

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * Shared add recipe logic that can be used by both Android and Web platforms
 * This replaces the Android-specific AddEditRecipeViewModel with a platform-agnostic implementation
 */
class SharedAddRecipeManager(private val recipes: Recipes) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()
    
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Combined state for easy consumption by UI
    val addRecipeUiState: StateFlow<AddRecipeUiState> = combine(
        title, content, isLoading
    ) { title: String, content: String, isLoading: Boolean ->
        AddRecipeUiState(
            title = title,
            content = content,
            isLoading = isLoading,
            canSave = title.trim().isNotEmpty() && content.trim().isNotEmpty() && !isLoading
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddRecipeUiState(
            title = "",
            content = "",
            isLoading = false,
            canSave = false
        )
    )
    
    fun updateTitle(value: String) {
        _title.value = value
    }
    
    fun updateContent(value: String) {
        _content.value = value
    }
    
    fun saveRecipe(onSuccess: () -> Unit = {}) {
        val currentTitle = _title.value.trim()
        val currentContent = _content.value.trim()
        
        if (currentTitle.isEmpty() || currentContent.isEmpty() || _isLoading.value) {
            return
        }
        
        _isLoading.value = true
        scope.launch {
            try {
                val recipe = Recipe(
                    title = currentTitle,
                    content = currentContent,
                    timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                )
                recipes.addRecipe(recipe)
                
                // Clear form
                _title.value = ""
                _content.value = ""
                
                onSuccess()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearForm() {
        _title.value = ""
        _content.value = ""
    }
}

data class AddRecipeUiState(
    val title: String,
    val content: String,
    val isLoading: Boolean,
    val canSave: Boolean
)
