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
class NavigationStateManagerTest {
    
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
    fun `initial state should have default values`() = runTest {
        val initialState = navigationStateManager.navigationState.value
        
        assertEquals(NavigationState(), initialState)
        assertNull(initialState.currentRoute)
        assertNull(initialState.previousRoute)
        assertEquals(0, initialState.backStackSize)
        assertFalse(initialState.isNavigating)
        assertNull(initialState.navigationError)
    }
    
    @Test
    fun `updateNavigationState should update current route`() = runTest {
        navigationStateManager.updateNavigationState(currentRoute = "test_route")
        
        val state = navigationStateManager.navigationState.value
        assertEquals("test_route", state.currentRoute)
    }
    
    @Test
    fun `updateNavigationState should update previous route`() = runTest {
        navigationStateManager.updateNavigationState(previousRoute = "previous_route")
        
        val state = navigationStateManager.navigationState.value
        assertEquals("previous_route", state.previousRoute)
    }
    
    @Test
    fun `updateNavigationState should update back stack size`() = runTest {
        navigationStateManager.updateNavigationState(backStackSize = 3)
        
        val state = navigationStateManager.navigationState.value
        assertEquals(3, state.backStackSize)
    }
    
    @Test
    fun `updateNavigationState should update isNavigating flag`() = runTest {
        navigationStateManager.updateNavigationState(isNavigating = true)
        
        val state = navigationStateManager.navigationState.value
        assertTrue(state.isNavigating)
    }
    
    @Test
    fun `updateNavigationState should update navigation error`() = runTest {
        navigationStateManager.updateNavigationState(navigationError = "Test error")
        
        val state = navigationStateManager.navigationState.value
        assertEquals("Test error", state.navigationError)
    }
    
    @Test
    fun `updateNavigationState should preserve existing values when not specified`() = runTest {
        // Set initial state
        navigationStateManager.updateNavigationState(
            currentRoute = "initial_route",
            backStackSize = 2,
            isNavigating = true
        )
        
        // Update only some values
        navigationStateManager.updateNavigationState(
            currentRoute = "new_route",
            navigationError = "new_error"
        )
        
        val state = navigationStateManager.navigationState.value
        assertEquals("new_route", state.currentRoute)
        assertEquals(2, state.backStackSize) // Should be preserved
        assertTrue(state.isNavigating) // Should be preserved
        assertEquals("new_error", state.navigationError)
    }
    
    @Test
    fun `emitNavigationEvent should emit event`() = runTest {
        val testEvent = NavigationEvent.NavigateTo("test_route")
        
        navigationStateManager.navigationEvents.test {
            navigationStateManager.emitNavigationEvent(testEvent)
            
            val emittedEvent = awaitItem()
            assertEquals(testEvent, emittedEvent)
        }
    }
    
    @Test
    fun `clearNavigationEvent should clear current event`() = runTest {
        val testEvent = NavigationEvent.NavigateTo("test_route")
        
        navigationStateManager.navigationEvents.test {
            navigationStateManager.emitNavigationEvent(testEvent)
            assertEquals(testEvent, awaitItem())
            
            navigationStateManager.clearNavigationEvent()
            assertNull(awaitItem())
        }
    }
    
    @Test
    fun `isCurrentRoute should return true for matching route`() = runTest {
        navigationStateManager.updateNavigationState(currentRoute = "test_route")
        
        assertTrue(navigationStateManager.isCurrentRoute("test_route"))
    }
    
    @Test
    fun `isCurrentRoute should return false for non-matching route`() = runTest {
        navigationStateManager.updateNavigationState(currentRoute = "test_route")
        
        assertFalse(navigationStateManager.isCurrentRoute("different_route"))
    }
    
    @Test
    fun `isCurrentRoute should return false when no current route`() = runTest {
        assertFalse(navigationStateManager.isCurrentRoute("any_route"))
    }
    
    @Test
    fun `getCurrentRoute should return current route`() = runTest {
        navigationStateManager.updateNavigationState(currentRoute = "test_route")
        
        assertEquals("test_route", navigationStateManager.getCurrentRoute())
    }
    
    @Test
    fun `getCurrentRoute should return null when no current route`() = runTest {
        assertNull(navigationStateManager.getCurrentRoute())
    }
    
    @Test
    fun `isNavigating should return current navigation state`() = runTest {
        navigationStateManager.updateNavigationState(isNavigating = true)
        assertTrue(navigationStateManager.isNavigating())
        
        navigationStateManager.updateNavigationState(isNavigating = false)
        assertFalse(navigationStateManager.isNavigating())
    }
    
    @Test
    fun `getBackStackSize should return current back stack size`() = runTest {
        navigationStateManager.updateNavigationState(backStackSize = 5)
        
        assertEquals(5, navigationStateManager.getBackStackSize())
    }
    
    @Test
    fun `canNavigateBack should return true when back stack has items`() = runTest {
        navigationStateManager.updateNavigationState(backStackSize = 2)
        
        assertTrue(navigationStateManager.canNavigateBack())
    }
    
    @Test
    fun `canNavigateBack should return false when back stack is empty`() = runTest {
        navigationStateManager.updateNavigationState(backStackSize = 0)
        
        assertFalse(navigationStateManager.canNavigateBack())
    }
    
    @Test
    fun `canNavigateBack should return false when back stack has only one item`() = runTest {
        navigationStateManager.updateNavigationState(backStackSize = 1)
        
        assertFalse(navigationStateManager.canNavigateBack())
    }
    
    @Test
    fun `navigation state should be observable`() = runTest {
        navigationStateManager.navigationState.test {
            // Initial state
            assertEquals(NavigationState(), awaitItem())
            
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
            val testEvent = NavigationEvent.NavigateBack
            navigationStateManager.emitNavigationEvent(testEvent)
            
            assertEquals(testEvent, awaitItem())
        }
    }
}