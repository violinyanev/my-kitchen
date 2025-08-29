package com.ultraviolince.mykitchen.recipes.presentation.navigation

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationStatesTest {
    
    private lateinit var navigationStateManager: NavigationStateManager
    private lateinit var navigationObserver: NavigationObserver
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        navigationStateManager = NavigationStateManager()
        navigationObserver = NavigationObserver(navigationStateManager)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `test initial navigation state`() = runTest {
        val initialState = navigationStateManager.navigationState.value
        
        assertEquals(NavigationState(), initialState)
        assertNull(initialState.currentRoute)
        assertNull(initialState.previousRoute)
        assertEquals(0, initialState.backStackSize)
        assertFalse(initialState.isNavigating)
        assertNull(initialState.navigationError)
    }
    
    @Test
    fun `test navigation to login state`() = runTest {
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.LOGIN,
            isNavigating = false
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals(NavigationRoutes.LOGIN, state.currentRoute)
        assertFalse(state.isNavigating)
        assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.LOGIN))
    }
    
    @Test
    fun `test navigation to recipes state`() = runTest {
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.RECIPES,
            previousRoute = NavigationRoutes.LOGIN,
            backStackSize = 1,
            isNavigating = false
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
        assertEquals(NavigationRoutes.LOGIN, state.previousRoute)
        assertEquals(1, state.backStackSize)
        assertFalse(state.isNavigating)
        assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.RECIPES))
    }
    
    @Test
    fun `test navigation to add edit recipe state`() = runTest {
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.ADD_EDIT_RECIPE,
            previousRoute = NavigationRoutes.RECIPES,
            backStackSize = 2,
            isNavigating = false
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, state.currentRoute)
        assertEquals(NavigationRoutes.RECIPES, state.previousRoute)
        assertEquals(2, state.backStackSize)
        assertFalse(state.isNavigating)
        assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.ADD_EDIT_RECIPE))
    }
    
    @Test
    fun `test navigation in progress state`() = runTest {
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.RECIPES,
            isNavigating = true
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
        assertTrue(state.isNavigating)
        assertTrue(navigationStateManager.isNavigating())
    }
    
    @Test
    fun `test navigation error state`() = runTest {
        val errorMessage = "Navigation failed"
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.RECIPES,
            isNavigating = false,
            navigationError = errorMessage
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
        assertFalse(state.isNavigating)
        assertEquals(errorMessage, state.navigationError)
    }
    
    @Test
    fun `test deep navigation stack state`() = runTest {
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.ADD_EDIT_RECIPE,
            previousRoute = NavigationRoutes.RECIPES,
            backStackSize = 5,
            isNavigating = false
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals(5, state.backStackSize)
        assertTrue(navigationStateManager.canNavigateBack())
        assertEquals(5, navigationStateManager.getBackStackSize())
    }
    
    @Test
    fun `test single screen state`() = runTest {
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.RECIPES,
            backStackSize = 1,
            isNavigating = false
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals(1, state.backStackSize)
        assertFalse(navigationStateManager.canNavigateBack())
        assertEquals(1, navigationStateManager.getBackStackSize())
    }
    
    @Test
    fun `test navigation state transitions`() = runTest {
        navigationStateManager.navigationState.test {
            // Initial state
            assertEquals(NavigationState(), awaitItem())
            
            // Navigate to login
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.LOGIN,
                isNavigating = false
            )
            var state = awaitItem()
            assertEquals(NavigationRoutes.LOGIN, state.currentRoute)
            assertFalse(state.isNavigating)
            
            // Navigate to recipes (with navigation in progress)
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.RECIPES,
                previousRoute = NavigationRoutes.LOGIN,
                backStackSize = 1,
                isNavigating = true
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertEquals(NavigationRoutes.LOGIN, state.previousRoute)
            assertEquals(1, state.backStackSize)
            assertTrue(state.isNavigating)
            
            // Complete navigation
            navigationStateManager.updateNavigationState(isNavigating = false)
            state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertFalse(state.isNavigating)
            
            // Navigate to add edit recipe
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.ADD_EDIT_RECIPE,
                previousRoute = NavigationRoutes.RECIPES,
                backStackSize = 2,
                isNavigating = true
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, state.currentRoute)
            assertEquals(NavigationRoutes.RECIPES, state.previousRoute)
            assertEquals(2, state.backStackSize)
            assertTrue(state.isNavigating)
            
            // Complete navigation
            navigationStateManager.updateNavigationState(isNavigating = false)
            state = awaitItem()
            assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, state.currentRoute)
            assertFalse(state.isNavigating)
        }
    }
    
    @Test
    fun `test navigation error handling`() = runTest {
        navigationStateManager.navigationEvents.test {
            // Initial state
            assertNull(awaitItem())
            
            // Emit navigation error
            val errorMessage = "Navigation failed"
            navigationStateManager.emitNavigationEvent(
                NavigationEvent.NavigationError(errorMessage)
            )
            
            val event = awaitItem()
            assertTrue(event is NavigationEvent.NavigationError)
            assertEquals(errorMessage, (event as NavigationEvent.NavigationError).error)
            
            // Clear event
            navigationStateManager.clearNavigationEvent()
            assertNull(awaitItem())
        }
    }
    
    @Test
    fun `test navigation state with observer integration`() = runTest {
        val mockDestination = MockNavDestination(NavigationRoutes.RECIPES)
        
        // Test destination change
        navigationObserver.onDestinationChanged(mockDestination)
        
        val state = navigationStateManager.navigationState.value
        assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
        assertEquals(NavigationRoutes.RECIPES, navigationObserver.getCurrentRoute())
        assertTrue(navigationObserver.isCurrentRoute(NavigationRoutes.RECIPES))
        
        // Test back stack change
        val mockEntries = listOf(
            MockNavBackStackEntry(NavigationRoutes.LOGIN),
            MockNavBackStackEntry(NavigationRoutes.RECIPES)
        )
        navigationObserver.onBackStackChanged(mockEntries)
        
        val updatedState = navigationStateManager.navigationState.value
        assertEquals(2, updatedState.backStackSize)
        assertEquals(2, navigationObserver.getBackStackSize())
        assertTrue(navigationObserver.canNavigateBack())
        
        val routes = navigationObserver.getBackStackRoutes()
        assertEquals(listOf(NavigationRoutes.LOGIN, NavigationRoutes.RECIPES), routes)
    }
    
    @Test
    fun `test navigation state edge cases`() = runTest {
        // Test with null current route
        navigationStateManager.updateNavigationState(currentRoute = null)
        assertNull(navigationStateManager.getCurrentRoute())
        assertFalse(navigationStateManager.isCurrentRoute("any_route"))
        
        // Test with empty back stack
        navigationStateManager.updateNavigationState(backStackSize = 0)
        assertFalse(navigationStateManager.canNavigateBack())
        assertEquals(0, navigationStateManager.getBackStackSize())
        
        // Test with large back stack
        navigationStateManager.updateNavigationState(backStackSize = 100)
        assertTrue(navigationStateManager.canNavigateBack())
        assertEquals(100, navigationStateManager.getBackStackSize())
        
        // Test with very long route name
        val longRoute = "a".repeat(1000)
        navigationStateManager.updateNavigationState(currentRoute = longRoute)
        assertEquals(longRoute, navigationStateManager.getCurrentRoute())
        assertTrue(navigationStateManager.isCurrentRoute(longRoute))
    }
    
    // Mock classes for testing
    private class MockNavDestination(override val route: String?) : androidx.navigation.NavDestination("test")
    
    private class MockNavBackStackEntry(route: String) : androidx.navigation.NavBackStackEntry {
        override val destination = MockNavDestination(route)
        override val arguments = null
        override val id = 0
        override val lifecycle = androidx.lifecycle.Lifecycle.State.CREATED
        override val savedStateHandle = androidx.lifecycle.SavedStateHandle()
    }
}