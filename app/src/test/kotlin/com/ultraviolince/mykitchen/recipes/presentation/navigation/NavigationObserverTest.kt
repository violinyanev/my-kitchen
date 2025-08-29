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
class NavigationObserverTest {
    
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
    fun `onNavigationStarted should set isNavigating to true`() = runTest {
        navigationObserver.onNavigationStarted()
        
        val state = navigationStateManager.navigationState.value
        assertTrue(state.isNavigating)
    }
    
    @Test
    fun `onNavigationCompleted should set isNavigating to false`() = runTest {
        // First start navigation
        navigationObserver.onNavigationStarted()
        assertTrue(navigationStateManager.navigationState.value.isNavigating)
        
        // Then complete navigation
        navigationObserver.onNavigationCompleted()
        
        val state = navigationStateManager.navigationState.value
        assertFalse(state.isNavigating)
    }
    
    @Test
    fun `onDestinationChanged should update current and previous routes`() = runTest {
        val mockDestination1 = MockNavDestination("route1")
        val mockDestination2 = MockNavDestination("route2")
        
        // First destination change
        navigationObserver.onDestinationChanged(mockDestination1)
        
        var state = navigationStateManager.navigationState.value
        assertEquals("route1", state.currentRoute)
        assertNull(state.previousRoute) // No previous route on first change
        
        // Second destination change
        navigationObserver.onDestinationChanged(mockDestination2)
        
        state = navigationStateManager.navigationState.value
        assertEquals("route2", state.currentRoute)
        assertEquals("route1", state.previousRoute)
    }
    
    @Test
    fun `onDestinationChanged should handle null destination`() = runTest {
        val mockDestination = MockNavDestination("route1")
        
        // Set initial destination
        navigationObserver.onDestinationChanged(mockDestination)
        assertEquals("route1", navigationStateManager.navigationState.value.currentRoute)
        
        // Change to null destination
        navigationObserver.onDestinationChanged(null)
        
        val state = navigationStateManager.navigationState.value
        assertNull(state.currentRoute)
        assertEquals("route1", state.previousRoute)
    }
    
    @Test
    fun `onBackStackChanged should update back stack size`() = runTest {
        val mockEntries = listOf(
            MockNavBackStackEntry("route1"),
            MockNavBackStackEntry("route2"),
            MockNavBackStackEntry("route3")
        )
        
        navigationObserver.onBackStackChanged(mockEntries)
        
        val state = navigationStateManager.navigationState.value
        assertEquals(3, state.backStackSize)
    }
    
    @Test
    fun `onBackStackChanged should handle empty back stack`() = runTest {
        navigationObserver.onBackStackChanged(emptyList())
        
        val state = navigationStateManager.navigationState.value
        assertEquals(0, state.backStackSize)
    }
    
    @Test
    fun `onNavigationError should set error and emit event`() = runTest {
        val errorMessage = "Navigation failed"
        
        navigationStateManager.navigationEvents.test {
            navigationObserver.onNavigationError(errorMessage)
            
            val state = navigationStateManager.navigationState.value
            assertEquals(errorMessage, state.navigationError)
            assertFalse(state.isNavigating)
            
            val event = awaitItem()
            assertTrue(event is NavigationEvent.NavigationError)
            assertEquals(errorMessage, (event as NavigationEvent.NavigationError).error)
        }
    }
    
    @Test
    fun `getCurrentRoute should return current destination route`() = runTest {
        val mockDestination = MockNavDestination("test_route")
        navigationObserver.onDestinationChanged(mockDestination)
        
        assertEquals("test_route", navigationObserver.getCurrentRoute())
    }
    
    @Test
    fun `getCurrentRoute should return null when no destination`() = runTest {
        assertNull(navigationObserver.getCurrentRoute())
    }
    
    @Test
    fun `getPreviousRoute should return previous route from state manager`() = runTest {
        val mockDestination1 = MockNavDestination("route1")
        val mockDestination2 = MockNavDestination("route2")
        
        navigationObserver.onDestinationChanged(mockDestination1)
        navigationObserver.onDestinationChanged(mockDestination2)
        
        assertEquals("route1", navigationObserver.getPreviousRoute())
    }
    
    @Test
    fun `isCurrentRoute should return true for matching route`() = runTest {
        val mockDestination = MockNavDestination("test_route")
        navigationObserver.onDestinationChanged(mockDestination)
        
        assertTrue(navigationObserver.isCurrentRoute("test_route"))
    }
    
    @Test
    fun `isCurrentRoute should return false for non-matching route`() = runTest {
        val mockDestination = MockNavDestination("test_route")
        navigationObserver.onDestinationChanged(mockDestination)
        
        assertFalse(navigationObserver.isCurrentRoute("different_route"))
    }
    
    @Test
    fun `isCurrentRoute should return false when no destination`() = runTest {
        assertFalse(navigationObserver.isCurrentRoute("any_route"))
    }
    
    @Test
    fun `getBackStackSize should return current back stack size`() = runTest {
        val mockEntries = listOf(
            MockNavBackStackEntry("route1"),
            MockNavBackStackEntry("route2")
        )
        
        navigationObserver.onBackStackChanged(mockEntries)
        
        assertEquals(2, navigationObserver.getBackStackSize())
    }
    
    @Test
    fun `canNavigateBack should return true when back stack has multiple entries`() = runTest {
        val mockEntries = listOf(
            MockNavBackStackEntry("route1"),
            MockNavBackStackEntry("route2")
        )
        
        navigationObserver.onBackStackChanged(mockEntries)
        
        assertTrue(navigationObserver.canNavigateBack())
    }
    
    @Test
    fun `canNavigateBack should return false when back stack has only one entry`() = runTest {
        val mockEntries = listOf(MockNavBackStackEntry("route1"))
        
        navigationObserver.onBackStackChanged(mockEntries)
        
        assertFalse(navigationObserver.canNavigateBack())
    }
    
    @Test
    fun `canNavigateBack should return false when back stack is empty`() = runTest {
        navigationObserver.onBackStackChanged(emptyList())
        
        assertFalse(navigationObserver.canNavigateBack())
    }
    
    @Test
    fun `getBackStackRoutes should return list of route names`() = runTest {
        val mockEntries = listOf(
            MockNavBackStackEntry("route1"),
            MockNavBackStackEntry("route2"),
            MockNavBackStackEntry("route3")
        )
        
        navigationObserver.onBackStackChanged(mockEntries)
        
        val routes = navigationObserver.getBackStackRoutes()
        assertEquals(listOf("route1", "route2", "route3"), routes)
    }
    
    @Test
    fun `getBackStackRoutes should handle empty back stack`() = runTest {
        navigationObserver.onBackStackChanged(emptyList())
        
        val routes = navigationObserver.getBackStackRoutes()
        assertTrue(routes.isEmpty())
    }
    
    @Test
    fun `currentDestination should be observable`() = runTest {
        navigationObserver.currentDestination.test {
            // Initial state should be null
            assertNull(awaitItem())
            
            // Change destination
            val mockDestination = MockNavDestination("test_route")
            navigationObserver.onDestinationChanged(mockDestination)
            
            assertEquals(mockDestination, awaitItem())
        }
    }
    
    @Test
    fun `backStackEntries should be observable`() = runTest {
        navigationObserver.backStackEntries.test {
            // Initial state should be empty
            assertTrue(awaitItem().isEmpty())
            
            // Change back stack
            val mockEntries = listOf(MockNavBackStackEntry("route1"))
            navigationObserver.onBackStackChanged(mockEntries)
            
            assertEquals(mockEntries, awaitItem())
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