package com.ultraviolince.mykitchen.recipes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest

/**
 * Enhanced navigation host that provides comprehensive navigation functionality
 */
@Composable
fun EnhancedNavigationHost(
    startDestination: String = NavigationRoutes.RECIPES,
    navigationStateManager: NavigationStateManager = remember { NavigationStateManager() },
    content: @Composable (NavHostController, NavigationActions, NavigationObserver) -> Unit
) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) { NavigationActions(navController) }
    val navigationObserver = remember(navigationStateManager) { NavigationObserver(navigationStateManager) }
    
    // Observe navigation state changes
    val navigationState by navigationStateManager.navigationState.collectAsState()
    
    // Handle navigation events
    LaunchedEffect(Unit) {
        navigationStateManager.navigationEvents.collectLatest { event ->
            event?.let {
                when (it) {
                    is NavigationEvent.NavigateTo -> {
                        try {
                            navigationObserver.onNavigationStarted()
                            navController.navigate(it.route)
                            navigationObserver.onNavigationCompleted()
                        } catch (e: Exception) {
                            navigationObserver.onNavigationError(e.message ?: "Navigation failed")
                        }
                    }
                    is NavigationEvent.NavigateToWithArgs -> {
                        try {
                            navigationObserver.onNavigationStarted()
                            // Build route with arguments
                            val routeWithArgs = buildString {
                                append(it.route)
                                if (it.args.isNotEmpty()) {
                                    append("?")
                                    append(it.args.entries.joinToString("&") { (key, value) ->
                                        "$key=$value"
                                    })
                                }
                            }
                            navController.navigate(routeWithArgs)
                            navigationObserver.onNavigationCompleted()
                        } catch (e: Exception) {
                            navigationObserver.onNavigationError(e.message ?: "Navigation failed")
                        }
                    }
                    is NavigationEvent.NavigateBack -> {
                        try {
                            navigationObserver.onNavigationStarted()
                            if (navController.popBackStack()) {
                                navigationObserver.onNavigationCompleted()
                            } else {
                                navigationObserver.onNavigationError("Cannot navigate back")
                            }
                        } catch (e: Exception) {
                            navigationObserver.onNavigationError(e.message ?: "Navigation failed")
                        }
                    }
                    is NavigationEvent.ClearBackStack -> {
                        try {
                            navigationObserver.onNavigationStarted()
                            navController.popBackStack(0, true)
                            navigationObserver.onNavigationCompleted()
                        } catch (e: Exception) {
                            navigationObserver.onNavigationError(e.message ?: "Navigation failed")
                        }
                    }
                    is NavigationEvent.NavigationError -> {
                        navigationObserver.onNavigationError(it.error)
                    }
                }
                navigationStateManager.clearNavigationEvent()
            }
        }
    }
    
    // Observe navigation controller changes
    DisposableEffect(navController) {
        val listener = object : NavHostController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavHostController,
                destination: androidx.navigation.NavDestination?,
                arguments: androidx.navigation.Bundle?
            ) {
                navigationObserver.onDestinationChanged(destination)
            }
        }
        
        navController.addOnDestinationChangedListener(listener)
        
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    
    // Build the navigation graph
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        buildNavigationGraph(navController)
    }
    
    // Provide the navigation components to the content
    content(navController, navigationActions, navigationObserver)
}

/**
 * Composable that provides navigation state to child composables
 */
@Composable
fun NavigationStateProvider(
    navigationStateManager: NavigationStateManager,
    content: @Composable () -> Unit
) {
    val navigationState by navigationStateManager.navigationState.collectAsState()
    
    // Provide navigation state to child composables
    LaunchedEffect(navigationState) {
        // Navigation state has changed, child composables can react to it
    }
    
    content()
}

/**
 * Extension function to get navigation state from the provider
 */
@Composable
fun rememberNavigationState(): NavigationState {
    // This would typically be provided by a CompositionLocal
    // For now, we'll return a default state
    return NavigationState()
}