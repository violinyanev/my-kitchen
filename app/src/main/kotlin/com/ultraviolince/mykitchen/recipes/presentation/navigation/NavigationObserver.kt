package com.ultraviolince.mykitchen.recipes.presentation.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Observer that tracks navigation changes and updates the navigation state
 */
class NavigationObserver(
    private val navigationStateManager: NavigationStateManager
) {
    
    private val _currentDestination = MutableStateFlow<NavDestination?>(null)
    val currentDestination: StateFlow<NavDestination?> = _currentDestination.asStateFlow()
    
    private val _backStackEntries = MutableStateFlow<List<NavBackStackEntry>>(emptyList())
    val backStackEntries: StateFlow<List<NavBackStackEntry>> = _backStackEntries.asStateFlow()
    
    /**
     * Called when navigation starts
     */
    fun onNavigationStarted() {
        navigationStateManager.updateNavigationState(isNavigating = true)
    }
    
    /**
     * Called when navigation completes
     */
    fun onNavigationCompleted() {
        navigationStateManager.updateNavigationState(isNavigating = false)
    }
    
    /**
     * Called when the current destination changes
     */
    fun onDestinationChanged(destination: NavDestination?) {
        val previousRoute = _currentDestination.value?.route
        val currentRoute = destination?.route
        
        _currentDestination.value = destination
        
        navigationStateManager.updateNavigationState(
            currentRoute = currentRoute,
            previousRoute = previousRoute
        )
    }
    
    /**
     * Called when the back stack changes
     */
    fun onBackStackChanged(entries: List<NavBackStackEntry>) {
        _backStackEntries.value = entries
        
        navigationStateManager.updateNavigationState(
            backStackSize = entries.size
        )
    }
    
    /**
     * Called when navigation fails
     */
    fun onNavigationError(error: String) {
        navigationStateManager.updateNavigationState(
            isNavigating = false,
            navigationError = error
        )
        
        navigationStateManager.emitNavigationEvent(
            NavigationEvent.NavigationError(error)
        )
    }
    
    /**
     * Gets the current route
     */
    fun getCurrentRoute(): String? {
        return _currentDestination.value?.route
    }
    
    /**
     * Gets the previous route
     */
    fun getPreviousRoute(): String? {
        return navigationStateManager.navigationState.value.previousRoute
    }
    
    /**
     * Checks if the current route matches the given route
     */
    fun isCurrentRoute(route: String): Boolean {
        return _currentDestination.value?.route == route
    }
    
    /**
     * Gets the back stack size
     */
    fun getBackStackSize(): Int {
        return _backStackEntries.value.size
    }
    
    /**
     * Checks if there are screens in the back stack
     */
    fun canNavigateBack(): Boolean {
        return _backStackEntries.value.size > 1
    }
    
    /**
     * Gets all routes in the back stack
     */
    fun getBackStackRoutes(): List<String> {
        return _backStackEntries.value.mapNotNull { it.destination.route }
    }
}