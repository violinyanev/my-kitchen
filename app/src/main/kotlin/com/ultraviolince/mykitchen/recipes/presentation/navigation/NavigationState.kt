package com.ultraviolince.mykitchen.recipes.presentation.navigation

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current navigation state
 */
@Stable
data class NavigationState(
    val currentRoute: String? = null,
    val previousRoute: String? = null,
    val backStackSize: Int = 0,
    val isNavigating: Boolean = false,
    val navigationError: String? = null
)

/**
 * Navigation events that can occur
 */
sealed class NavigationEvent {
    data class NavigateTo(val route: String) : NavigationEvent()
    data class NavigateToWithArgs(val route: String, val args: Map<String, Any>) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    object ClearBackStack : NavigationEvent()
    data class NavigationError(val error: String) : NavigationEvent()
}

/**
 * Manages navigation state and provides navigation actions
 */
class NavigationStateManager {
    
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val _navigationEvents = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvents: StateFlow<NavigationEvent?> = _navigationEvents.asStateFlow()
    
    /**
     * Updates the current navigation state
     */
    fun updateNavigationState(
        currentRoute: String? = null,
        previousRoute: String? = null,
        backStackSize: Int? = null,
        isNavigating: Boolean? = null,
        navigationError: String? = null
    ) {
        _navigationState.value = _navigationState.value.copy(
            currentRoute = currentRoute ?: _navigationState.value.currentRoute,
            previousRoute = previousRoute ?: _navigationState.value.previousRoute,
            backStackSize = backStackSize ?: _navigationState.value.backStackSize,
            isNavigating = isNavigating ?: _navigationState.value.isNavigating,
            navigationError = navigationError ?: _navigationState.value.navigationError
        )
    }
    
    /**
     * Emits a navigation event
     */
    fun emitNavigationEvent(event: NavigationEvent) {
        _navigationEvents.value = event
    }
    
    /**
     * Clears the current navigation event
     */
    fun clearNavigationEvent() {
        _navigationEvents.value = null
    }
    
    /**
     * Checks if the current route matches the given route
     */
    fun isCurrentRoute(route: String): Boolean {
        return _navigationState.value.currentRoute == route
    }
    
    /**
     * Gets the current route
     */
    fun getCurrentRoute(): String? {
        return _navigationState.value.currentRoute
    }
    
    /**
     * Checks if navigation is in progress
     */
    fun isNavigating(): Boolean {
        return _navigationState.value.isNavigating
    }
    
    /**
     * Gets the back stack size
     */
    fun getBackStackSize(): Int {
        return _navigationState.value.backStackSize
    }
    
    /**
     * Checks if there are screens in the back stack
     */
    fun canNavigateBack(): Boolean {
        return _navigationState.value.backStackSize > 0
    }
}