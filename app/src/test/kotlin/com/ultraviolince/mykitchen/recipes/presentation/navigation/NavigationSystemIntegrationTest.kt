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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationSystemIntegrationTest {
    
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
    fun `test complete navigation flow from login to recipes to add recipe`() = runTest {
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
            
            // Navigate to recipes (simulating successful login)
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.RECIPES,
                previousRoute = NavigationRoutes.LOGIN,
                backStackSize = 1,
                isNavigating = false
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertEquals(NavigationRoutes.LOGIN, state.previousRoute)
            assertEquals(1, state.backStackSize)
            
            // Navigate to add recipe
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.ADD_EDIT_RECIPE,
                previousRoute = NavigationRoutes.RECIPES,
                backStackSize = 2,
                isNavigating = false
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, state.currentRoute)
            assertEquals(NavigationRoutes.RECIPES, state.previousRoute)
            assertEquals(2, state.backStackSize)
            
            // Navigate back to recipes
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.RECIPES,
                previousRoute = NavigationRoutes.ADD_EDIT_RECIPE,
                backStackSize = 1,
                isNavigating = false
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, state.previousRoute)
            assertEquals(1, state.backStackSize)
        }
    }
    
    @Test
    fun `test navigation with observer integration`() = runTest {
        // Test destination changes
        val loginDestination = MockNavDestination(NavigationRoutes.LOGIN)
        val recipesDestination = MockNavDestination(NavigationRoutes.RECIPES)
        val addRecipeDestination = MockNavDestination(NavigationRoutes.ADD_EDIT_RECIPE)
        
        // Navigate to login
        navigationObserver.onDestinationChanged(loginDestination)
        assertEquals(NavigationRoutes.LOGIN, navigationObserver.getCurrentRoute())
        assertTrue(navigationObserver.isCurrentRoute(NavigationRoutes.LOGIN))
        
        // Navigate to recipes
        navigationObserver.onDestinationChanged(recipesDestination)
        assertEquals(NavigationRoutes.RECIPES, navigationObserver.getCurrentRoute())
        assertEquals(NavigationRoutes.LOGIN, navigationObserver.getPreviousRoute())
        assertTrue(navigationObserver.isCurrentRoute(NavigationRoutes.RECIPES))
        
        // Navigate to add recipe
        navigationObserver.onDestinationChanged(addRecipeDestination)
        assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, navigationObserver.getCurrentRoute())
        assertEquals(NavigationRoutes.RECIPES, navigationObserver.getPreviousRoute())
        assertTrue(navigationObserver.isCurrentRoute(NavigationRoutes.ADD_EDIT_RECIPE))
        
        // Test back stack changes
        val backStackEntries = listOf(
            MockNavBackStackEntry(NavigationRoutes.LOGIN),
            MockNavBackStackEntry(NavigationRoutes.RECIPES),
            MockNavBackStackEntry(NavigationRoutes.ADD_EDIT_RECIPE)
        )
        navigationObserver.onBackStackChanged(backStackEntries)
        
        assertEquals(3, navigationObserver.getBackStackSize())
        assertTrue(navigationObserver.canNavigateBack())
        
        val routes = navigationObserver.getBackStackRoutes()
        assertEquals(
            listOf(NavigationRoutes.LOGIN, NavigationRoutes.RECIPES, NavigationRoutes.ADD_EDIT_RECIPE),
            routes
        )
    }
    
    @Test
    fun `test navigation error scenarios`() = runTest {
        navigationStateManager.navigationEvents.test {
            // Initial state
            assertNull(awaitItem())
            
            // Test navigation error
            val errorMessage = "Failed to navigate to recipes"
            navigationStateManager.emitNavigationEvent(
                NavigationEvent.NavigationError(errorMessage)
            )
            
            val errorEvent = awaitItem()
            assertTrue(errorEvent is NavigationEvent.NavigationError)
            assertEquals(errorMessage, (errorEvent as NavigationEvent.NavigationError).error)
            
            // Verify state is updated
            val state = navigationStateManager.navigationState.value
            assertEquals(errorMessage, state.navigationError)
            assertFalse(state.isNavigating)
            
            // Clear error
            navigationStateManager.clearNavigationEvent()
            assertNull(awaitItem())
        }
    }
    
    @Test
    fun `test navigation state transitions with loading states`() = runTest {
        navigationStateManager.navigationState.test {
            // Initial state
            assertEquals(NavigationState(), awaitItem())
            
            // Start navigation to recipes
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.LOGIN,
                isNavigating = true
            )
            var state = awaitItem()
            assertEquals(NavigationRoutes.LOGIN, state.currentRoute)
            assertTrue(state.isNavigating)
            
            // Complete navigation to recipes
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.RECIPES,
                previousRoute = NavigationRoutes.LOGIN,
                backStackSize = 1,
                isNavigating = false
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertEquals(NavigationRoutes.LOGIN, state.previousRoute)
            assertEquals(1, state.backStackSize)
            assertFalse(state.isNavigating)
            
            // Start navigation to add recipe
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
    fun `test navigation state queries and validation`() = runTest {
        // Test initial state queries
        assertFalse(navigationStateManager.isCurrentRoute(NavigationRoutes.LOGIN))
        assertFalse(navigationStateManager.isCurrentRoute(NavigationRoutes.RECIPES))
        assertFalse(navigationStateManager.isCurrentRoute(NavigationRoutes.ADD_EDIT_RECIPE))
        assertNull(navigationStateManager.getCurrentRoute())
        assertFalse(navigationStateManager.isNavigating())
        assertEquals(0, navigationStateManager.getBackStackSize())
        assertFalse(navigationStateManager.canNavigateBack())
        
        // Test state after navigation to recipes
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.RECIPES,
            backStackSize = 1,
            isNavigating = false
        )
        
        assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.RECIPES))
        assertFalse(navigationStateManager.isCurrentRoute(NavigationRoutes.LOGIN))
        assertFalse(navigationStateManager.isCurrentRoute(NavigationRoutes.ADD_EDIT_RECIPE))
        assertEquals(NavigationRoutes.RECIPES, navigationStateManager.getCurrentRoute())
        assertFalse(navigationStateManager.isNavigating())
        assertEquals(1, navigationStateManager.getBackStackSize())
        assertFalse(navigationStateManager.canNavigateBack())
        
        // Test state after adding to back stack
        navigationStateManager.updateNavigationState(
            currentRoute = NavigationRoutes.ADD_EDIT_RECIPE,
            backStackSize = 2,
            isNavigating = false
        )
        
        assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.ADD_EDIT_RECIPE))
        assertFalse(navigationStateManager.isCurrentRoute(NavigationRoutes.RECIPES))
        assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, navigationStateManager.getCurrentRoute())
        assertEquals(2, navigationStateManager.getBackStackSize())
        assertTrue(navigationStateManager.canNavigateBack())
    }
    
    @Test
    fun `test navigation state edge cases and boundary conditions`() = runTest {
        // Test with null values
        navigationStateManager.updateNavigationState(
            currentRoute = null,
            previousRoute = null,
            backStackSize = 0,
            isNavigating = false,
            navigationError = null
        )
        
        val state = navigationStateManager.navigationState.value
        assertNull(state.currentRoute)
        assertNull(state.previousRoute)
        assertEquals(0, state.backStackSize)
        assertFalse(state.isNavigating)
        assertNull(state.navigationError)
        
        // Test with extreme values
        navigationStateManager.updateNavigationState(
            currentRoute = "a".repeat(1000),
            backStackSize = Int.MAX_VALUE,
            isNavigating = true
        )
        
        val extremeState = navigationStateManager.navigationState.value
        assertEquals("a".repeat(1000), extremeState.currentRoute)
        assertEquals(Int.MAX_VALUE, extremeState.backStackSize)
        assertTrue(extremeState.isNavigating)
        assertTrue(navigationStateManager.canNavigateBack())
        
        // Test with negative back stack size
        navigationStateManager.updateNavigationState(backStackSize = -1)
        
        val negativeState = navigationStateManager.navigationState.value
        assertEquals(-1, negativeState.backStackSize)
        assertFalse(navigationStateManager.canNavigateBack())
    }
    
    @Test
    fun `test navigation state consistency across multiple updates`() = runTest {
        navigationStateManager.navigationState.test {
            // Initial state
            assertEquals(NavigationState(), awaitItem())
            
            // Multiple rapid updates
            navigationStateManager.updateNavigationState(currentRoute = NavigationRoutes.LOGIN)
            navigationStateManager.updateNavigationState(isNavigating = true)
            navigationStateManager.updateNavigationState(backStackSize = 1)
            navigationStateManager.updateNavigationState(previousRoute = null)
            
            val finalState = awaitItem()
            assertEquals(NavigationRoutes.LOGIN, finalState.currentRoute)
            assertTrue(finalState.isNavigating)
            assertEquals(1, finalState.backStackSize)
            assertNull(finalState.previousRoute)
            assertNull(finalState.navigationError)
            
            // Verify all properties are correctly set
            assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.LOGIN))
            assertEquals(NavigationRoutes.LOGIN, navigationStateManager.getCurrentRoute())
            assertTrue(navigationStateManager.isNavigating())
            assertEquals(1, navigationStateManager.getBackStackSize())
            assertFalse(navigationStateManager.canNavigateBack())
        }
    }
    
    @Test
    fun `test navigation state with complex scenarios`() = runTest {
        // Scenario: User navigates through multiple screens with errors and recovery
        
        navigationStateManager.navigationState.test {
            // Start at login
            assertEquals(NavigationState(), awaitItem())
            
            // Navigate to recipes (successful)
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.RECIPES,
                backStackSize = 1,
                isNavigating = false
            )
            var state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertEquals(1, state.backStackSize)
            
            // Try to navigate to add recipe (fails)
            navigationStateManager.updateNavigationState(
                isNavigating = true,
                navigationError = "Network error"
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertTrue(state.isNavigating)
            assertEquals("Network error", state.navigationError)
            
            // Retry navigation to add recipe (succeeds)
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.ADD_EDIT_RECIPE,
                previousRoute = NavigationRoutes.RECIPES,
                backStackSize = 2,
                isNavigating = false,
                navigationError = null
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, state.currentRoute)
            assertEquals(NavigationRoutes.RECIPES, state.previousRoute)
            assertEquals(2, state.backStackSize)
            assertFalse(state.isNavigating)
            assertNull(state.navigationError)
            
            // Navigate back to recipes
            navigationStateManager.updateNavigationState(
                currentRoute = NavigationRoutes.RECIPES,
                previousRoute = NavigationRoutes.ADD_EDIT_RECIPE,
                backStackSize = 1,
                isNavigating = false
            )
            state = awaitItem()
            assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
            assertEquals(1, state.backStackSize)
        }
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