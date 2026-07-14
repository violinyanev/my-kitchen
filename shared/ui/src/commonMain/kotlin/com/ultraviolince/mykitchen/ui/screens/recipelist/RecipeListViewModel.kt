package com.ultraviolince.mykitchen.ui.screens.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.SessionExpiredException
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.domain.usecase.DeleteRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetAuthStateUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetEnrichmentsUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetRecipesUseCase
import com.ultraviolince.mykitchen.domain.usecase.LogoutUseCase
import com.ultraviolince.mykitchen.domain.usecase.SyncRecipesUseCase
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.error_delete_failed
import com.ultraviolince.mykitchen.ui.generated.resources.error_sync_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class RecipeListState(
    val recipes: List<Recipe> = emptyList(),
    val order: RecipeOrder = RecipeOrder.Date(),
    val isSyncing: Boolean = false,
    val error: StringResource? = null,
    val isServerReachable: Boolean = true,
    /** Tags of each recipe's server-generated enrichment, keyed by recipe id. */
    val tagsByRecipe: Map<String, List<String>> = emptyMap(),
    val selectedTag: String? = null,
) {
    /** Distinct tags across all enrichments, for the filter chip row. */
    val allTags: List<String>
        get() = tagsByRecipe.values.flatten().distinct().sorted()

    /** Recipes to display, respecting the selected tag filter. */
    val visibleRecipes: List<Recipe>
        get() = selectedTag?.let { tag ->
            recipes.filter { tagsByRecipe[it.id]?.contains(tag) == true }
        } ?: recipes
}

class RecipeListViewModel(
    private val getRecipes: GetRecipesUseCase,
    private val deleteRecipe: DeleteRecipeUseCase,
    private val syncRecipes: SyncRecipesUseCase,
    private val logout: LogoutUseCase,
    private val getAuthState: GetAuthStateUseCase,
    private val getEnrichments: GetEnrichmentsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeListState())
    val state: StateFlow<RecipeListState> = _state.asStateFlow()

    val authState: StateFlow<AuthState> = getAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.LoggedOut)

    init {
        collectRecipes(RecipeOrder.Date())
        loadEnrichmentTags()
    }

    private fun collectRecipes(order: RecipeOrder) {
        viewModelScope.launch {
            getRecipes(order).collect { list ->
                _state.update { it.copy(recipes = list, order = order) }
            }
        }
    }

    private fun loadEnrichmentTags() {
        viewModelScope.launch {
            val enrichments = getEnrichments().getOrNull() ?: return@launch
            _state.update { state ->
                state.copy(tagsByRecipe = enrichments.associate { it.recipeId to it.tags })
            }
        }
    }

    fun setOrder(order: RecipeOrder) {
        collectRecipes(order)
    }

    fun selectTag(tag: String?) {
        _state.update { it.copy(selectedTag = if (it.selectedTag == tag) null else tag) }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                deleteRecipe(id)
            } catch (e: Exception) {
                _state.update { it.copy(error = Res.string.error_delete_failed) }
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null) }
            val result = syncRecipes()
            if (result.exceptionOrNull() is SessionExpiredException) {
                logout.invoke()
                return@launch
            }
            _state.update {
                it.copy(
                    isSyncing = false,
                    isServerReachable = result.isSuccess,
                    error = if (result.isFailure) Res.string.error_sync_failed else null,
                )
            }
            if (result.isSuccess) loadEnrichmentTags()
        }
    }

    fun logout() {
        viewModelScope.launch {
            logout.invoke()
        }
    }
}
