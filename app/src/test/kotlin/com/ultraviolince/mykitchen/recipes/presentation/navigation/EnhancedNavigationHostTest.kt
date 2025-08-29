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
class EnhancedNavigationHostTest {
    
    private lateinit var navigationStateManager: NavigationStateManager
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        navigationStateManager = NavigationStateManager()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `navigation state manager should be created with default state`() = runTest {
        val initialState = navigationStateManager.navigationState.value
        
        assertNotNull(initialState)
        assertNull(initialState.currentRoute)
        assertNull(initialState.previousRoute)
        assertEquals(0, initialState.backStackSize)
        assertFalse(initialState.isNavigating)
        assertNull(initialState.navigationError)
    }
    
    @Test
    fun `navigation state should be observable`() = runTest {
        navigationStateManager.navigationState.test {
            // Initial state
            val initialState = awaitItem()
            assertEquals(NavigationState(), initialState)
            
            // Update state
            navigationStateManager.updateNavigationState(
                currentRoute = "test_route",
                isNavigating = true
            )
            
            val updatedState = awaitItem()
            assertEquals("test_route", updatedState.currentRoute)
            assertTrue(updatedState.isNavigating)
        }
    }
    
    @Test
    fun `navigation events should be observable`() = runTest {
        navigationStateManager.navigationEvents.test {
            // Initial state should be null
            assertNull(awaitItem())
            
            // Emit event
            val testEvent = NavigationEvent.NavigateTo("test_route")
            navigationStateManager.emitNavigationEvent(testEvent)
            
            val emittedEvent = awaitItem()
            assertEquals(testEvent, emittedEvent)
        }
    }
    
    @Test
    fun `navigation state updates should preserve other values`() = runTest {
        // Set initial state with multiple values
        navigationStateManager.updateNavigationState(
            currentRoute = "initial_route",
            backStackSize = 3,
            isNavigating = true
        )
        
        // Update only one value
        navigationStateManager.updateNavigationState(currentRoute = "new_route")
        
        val state = navigationStateManager.navigationState.value
        assertEquals("new_route", state.currentRoute)
        assertEquals(3, state.backStackSize) // Should be preserved
        assertTrue(state.isNavigating) // Should be preserved
    }
    
    @Test
    fun `navigation state queries should work correctly`() = runTest {
        // Test initial state
        assertFalse(navigationStateManager.isCurrentRoute("any_route"))
        assertNull(navigationStateManager.getCurrentRoute())
        assertFalse(navigationStateManager.isNavigating())
        assertEquals(0, navigationStateManager.getBackStackSize())
        assertFalse(navigationStateManager.canNavigateBack())
        
        // Update state
        navigationStateManager.updateNavigationState(
            currentRoute = "test_route",
            backStackSize = 2,
            isNavigating = true
        )
        
        // Test updated state
        assertTrue(navigationStateManager.isCurrentRoute("test_route"))
        assertEquals("test_route", navigationStateManager.getCurrentRoute())
        assertTrue(navigationStateManager.isNavigating())
        assertEquals(2, navigationStateManager.getBackStackSize())
        assertTrue(navigationStateManager.canNavigateBack())
    }
    
    @Test
    fun `navigation event emission and clearing should work`() = runTest {
        navigationStateManager.navigationEvents.test {
            // Initial state
            assertNull(awaitItem())
            
            // Emit event
            val event = NavigationEvent.NavigateBack
            navigationStateManager.emitNavigationEvent(event)
            assertEquals(event, awaitItem())
            
            // Clear event
            navigationStateManager.clearNavigationEvent()
            assertNull(awaitItem())
        }
    }
    
    @Test
    fun `multiple navigation events should be handled correctly`() = runTest {
        navigationStateManager.navigationEvents.test {
            // Initial state
            assertNull(awaitItem())
            
            // Emit multiple events
            val events = listOf(
                NavigationEvent.NavigateTo("route1"),
                NavigationEvent.NavigateTo("route2"),
                NavigationEvent.NavigateBack,
                NavigationEvent.ClearBackStack
            )
            
            events.forEach { event ->
                navigationStateManager.emitNavigationEvent(event)
                assertEquals(event, awaitItem())
                navigationStateManager.clearNavigationEvent()
                assertNull(awaitItem())
            }
        }
    }
    
    @Test
    fun `navigation state should handle all event types`() = runTest {
        navigationStateManager.navigationEvents.test {
            assertNull(awaitItem())
            
            // Test NavigateTo event
            val navigateEvent = NavigationEvent.NavigateTo("test_route")
            navigationStateManager.emitNavigationEvent(navigateEvent)
            assertEquals(navigateEvent, awaitItem())
            navigationStateManager.clearNavigationEvent()
            
            // Test NavigateToWithArgs event
            val navigateWithArgsEvent = NavigationEvent.NavigateToWithArgs(
                "test_route",
                mapOf("param1" to "value1", "param2" to "value2")
            )
            navigationStateManager.emitNavigationEvent(navigateWithArgsEvent)
            assertEquals(navigateWithArgsEvent, awaitItem())
            navigationStateManager.clearNavigationEvent()
            
            // Test NavigateBack event
            val navigateBackEvent = NavigationEvent.NavigateBack
            navigationStateManager.emitNavigationEvent(navigateBackEvent)
            assertEquals(navigateBackEvent, awaitItem())
            navigationStateManager.clearNavigationEvent()
            
            // Test ClearBackStack event
            val clearBackStackEvent = NavigationEvent.ClearBackStack
            navigationStateManager.emitNavigationEvent(clearBackStackEvent)
            assertEquals(clearBackStackEvent, awaitItem())
            navigationStateManager.clearNavigationEvent()
            
            // Test NavigationError event
            val navigationErrorEvent = NavigationEvent.NavigationError("Test error")
            navigationStateManager.emitNavigationEvent(navigationErrorEvent)
            assertEquals(navigationErrorEvent, awaitItem())
            navigationStateManager.clearNavigationEvent()
        }
    }
    
    @Test
    fun `navigation state should handle complex state transitions`() = runTest {
        navigationStateManager.navigationState.test {
            // Initial state
            assertEquals(NavigationState(), awaitItem())
            
            // Start navigation
            navigationStateManager.updateNavigationState(
                currentRoute = "route1",
                isNavigating = true
            )
            var state = awaitItem()
            assertEquals("route1", state.currentRoute)
            assertTrue(state.isNavigating)
            
            // Complete navigation and add to back stack
            navigationStateManager.updateNavigationState(
                currentRoute = "route2",
                previousRoute = "route1",
                backStackSize = 2,
                isNavigating = false
            )
            state = awaitItem()
            assertEquals("route2", state.currentRoute)
            assertEquals("route1", state.previousRoute)
            assertEquals(2, state.backStackSize)
            assertFalse(state.isNavigating)
            
            // Navigate back
            navigationStateManager.updateNavigationState(
                currentRoute = "route1",
                previousRoute = "route2",
                backStackSize = 1,
                isNavigating = false
            )
            state = awaitItem()
            assertEquals("route1", state.currentRoute)
            assertEquals("route2", state.previousRoute)
            assertEquals(1, state.backStackSize)
            assertFalse(state.isNavigating)
        }
    }
}