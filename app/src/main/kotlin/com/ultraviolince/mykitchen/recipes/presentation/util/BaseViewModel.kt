package com.ultraviolince.mykitchen.recipes.presentation.util

interface UiEvent

interface UiState

interface BaseViewModel<Event : UiEvent, State : UiState> {
    fun onEvent(event: Event)
}