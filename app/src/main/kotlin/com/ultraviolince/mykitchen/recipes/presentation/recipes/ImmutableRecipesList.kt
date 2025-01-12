package com.ultraviolince.mykitchen.recipes.presentation.recipes

import androidx.compose.runtime.Immutable
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe

@Immutable
data class ImmutableRecipesList(val items: List<Recipe>)
