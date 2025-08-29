import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.*

// Mock NavigationState and NavigationEvent classes
@Stable
data class NavigationState(
    val currentRoute: String? = null,
    val previousRoute: String? = null,
    val backStackSize: Int = 0,
    val isNavigating: Boolean = false,
    val navigationError: String? = null
)

sealed class NavigationEvent {
    data class NavigateTo(val route: String) : NavigationEvent()
    data class NavigateToWithArgs(val route: String, val args: Map<String, Any>) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    object ClearBackStack : NavigationEvent()
    data class NavigationError(val error: String) : NavigationEvent()
}

class NavigationStateManager {
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val _navigationEvents = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvents: StateFlow<NavigationEvent?> = _navigationEvents.asStateFlow()
    
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
    
    fun emitNavigationEvent(event: NavigationEvent) {
        _navigationEvents.value = event
    }
    
    fun clearNavigationEvent() {
        _navigationEvents.value = null
    }
    
    fun isCurrentRoute(route: String): Boolean {
        return _navigationState.value.currentRoute == route
    }
    
    fun getCurrentRoute(): String? {
        return _navigationState.value.currentRoute
    }
    
    fun isNavigating(): Boolean {
        return _navigationState.value.isNavigating
    }
    
    fun getBackStackSize(): Int {
        return _navigationState.value.backStackSize
    }
    
    fun canNavigateBack(): Boolean {
        return _navigationState.value.backStackSize > 0
    }
}

// Navigation routes
object NavigationRoutes {
    const val LOGIN = "login"
    const val RECIPES = "recipes"
    const val ADD_EDIT_RECIPE = "add_edit_recipe"
    const val RECIPE_ID_ARG = "recipeId"
}

// Test functions
fun testNavigationStateManager() {
    println("Testing NavigationStateManager...")
    
    val navigationStateManager = NavigationStateManager()
    
    // Test initial state
    val initialState = navigationStateManager.navigationState.value
    assertEquals(NavigationState(), initialState)
    assertNull(initialState.currentRoute)
    assertNull(initialState.previousRoute)
    assertEquals(0, initialState.backStackSize)
    assertFalse(initialState.isNavigating)
    assertNull(initialState.navigationError)
    
    // Test state updates
    navigationStateManager.updateNavigationState(currentRoute = "test_route")
    val updatedState = navigationStateManager.navigationState.value
    assertEquals("test_route", updatedState.currentRoute)
    
    // Test navigation queries
    assertTrue(navigationStateManager.isCurrentRoute("test_route"))
    assertFalse(navigationStateManager.isCurrentRoute("different_route"))
    assertEquals("test_route", navigationStateManager.getCurrentRoute())
    
    // Test back stack
    navigationStateManager.updateNavigationState(backStackSize = 2)
    assertEquals(2, navigationStateManager.getBackStackSize())
    assertTrue(navigationStateManager.canNavigateBack())
    
    // Test navigation events
    val testEvent = NavigationEvent.NavigateTo("new_route")
    navigationStateManager.emitNavigationEvent(testEvent)
    assertEquals(testEvent, navigationStateManager.navigationEvents.value)
    
    navigationStateManager.clearNavigationEvent()
    assertNull(navigationStateManager.navigationEvents.value)
    
    println("✅ NavigationStateManager tests passed!")
}

fun testNavigationRoutes() {
    println("Testing NavigationRoutes...")
    
    assertEquals("login", NavigationRoutes.LOGIN)
    assertEquals("recipes", NavigationRoutes.RECIPES)
    assertEquals("add_edit_recipe", NavigationRoutes.ADD_EDIT_RECIPE)
    assertEquals("recipeId", NavigationRoutes.RECIPE_ID_ARG)
    
    // Test route uniqueness
    val routes = listOf(
        NavigationRoutes.LOGIN,
        NavigationRoutes.RECIPES,
        NavigationRoutes.ADD_EDIT_RECIPE
    )
    val uniqueRoutes = routes.toSet()
    assertEquals(routes.size, uniqueRoutes.size)
    
    // Test route formatting
    val addEditRoute = "${NavigationRoutes.ADD_EDIT_RECIPE}/{${NavigationRoutes.RECIPE_ID_ARG}}"
    assertEquals("add_edit_recipe/{recipeId}", addEditRoute)
    
    println("✅ NavigationRoutes tests passed!")
}

fun testNavigationEvents() {
    println("Testing NavigationEvents...")
    
    val navigateEvent = NavigationEvent.NavigateTo("test_route")
    assertTrue(navigateEvent is NavigationEvent.NavigateTo)
    assertEquals("test_route", (navigateEvent as NavigationEvent.NavigateTo).route)
    
    val navigateWithArgsEvent = NavigationEvent.NavigateToWithArgs(
        "test_route",
        mapOf("param1" to "value1")
    )
    assertTrue(navigateWithArgsEvent is NavigationEvent.NavigateToWithArgs)
    assertEquals("test_route", (navigateWithArgsEvent as NavigationEvent.NavigateToWithArgs).route)
    assertEquals(mapOf("param1" to "value1"), navigateWithArgsEvent.args)
    
    val navigateBackEvent = NavigationEvent.NavigateBack
    assertTrue(navigateBackEvent is NavigationEvent.NavigateBack)
    
    val clearBackStackEvent = NavigationEvent.ClearBackStack
    assertTrue(clearBackStackEvent is NavigationEvent.ClearBackStack)
    
    val navigationErrorEvent = NavigationEvent.NavigationError("Test error")
    assertTrue(navigationErrorEvent is NavigationEvent.NavigationError)
    assertEquals("Test error", (navigationErrorEvent as NavigationEvent.NavigationError).error)
    
    println("✅ NavigationEvents tests passed!")
}

fun testNavigationStateTransitions() {
    println("Testing Navigation State Transitions...")
    
    val navigationStateManager = NavigationStateManager()
    
    // Test initial state
    assertEquals(NavigationState(), navigationStateManager.navigationState.value)
    
    // Test navigation to login
    navigationStateManager.updateNavigationState(
        currentRoute = NavigationRoutes.LOGIN,
        isNavigating = false
    )
    var state = navigationStateManager.navigationState.value
    assertEquals(NavigationRoutes.LOGIN, state.currentRoute)
    assertFalse(state.isNavigating)
    assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.LOGIN))
    
    // Test navigation to recipes
    navigationStateManager.updateNavigationState(
        currentRoute = NavigationRoutes.RECIPES,
        previousRoute = NavigationRoutes.LOGIN,
        backStackSize = 1,
        isNavigating = false
    )
    state = navigationStateManager.navigationState.value
    assertEquals(NavigationRoutes.RECIPES, state.currentRoute)
    assertEquals(NavigationRoutes.LOGIN, state.previousRoute)
    assertEquals(1, state.backStackSize)
    assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.RECIPES))
    
    // Test navigation to add edit recipe
    navigationStateManager.updateNavigationState(
        currentRoute = NavigationRoutes.ADD_EDIT_RECIPE,
        previousRoute = NavigationRoutes.RECIPES,
        backStackSize = 2,
        isNavigating = false
    )
    state = navigationStateManager.navigationState.value
    assertEquals(NavigationRoutes.ADD_EDIT_RECIPE, state.currentRoute)
    assertEquals(NavigationRoutes.RECIPES, state.previousRoute)
    assertEquals(2, state.backStackSize)
    assertTrue(navigationStateManager.isCurrentRoute(NavigationRoutes.ADD_EDIT_RECIPE))
    assertTrue(navigationStateManager.canNavigateBack())
    
    println("✅ Navigation State Transitions tests passed!")
}

fun testNavigationErrorHandling() {
    println("Testing Navigation Error Handling...")
    
    val navigationStateManager = NavigationStateManager()
    
    // Test navigation error state
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
    
    // Test error event emission
    val errorEvent = NavigationEvent.NavigationError(errorMessage)
    navigationStateManager.emitNavigationEvent(errorEvent)
    assertEquals(errorEvent, navigationStateManager.navigationEvents.value)
    
    // Test error clearing
    navigationStateManager.clearNavigationEvent()
    assertNull(navigationStateManager.navigationEvents.value)
    
    println("✅ Navigation Error Handling tests passed!")
}

fun testEdgeCases() {
    println("Testing Edge Cases...")
    
    val navigationStateManager = NavigationStateManager()
    
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
    
    println("✅ Edge Cases tests passed!")
}

fun main() {
    println("🚀 Starting Navigation System Tests...")
    println("=" * 50)
    
    try {
        testNavigationRoutes()
        testNavigationEvents()
        testNavigationStateManager()
        testNavigationStateTransitions()
        testNavigationErrorHandling()
        testEdgeCases()
        
        println("=" * 50)
        println("🎉 All Navigation System Tests Passed!")
        println("✅ Total tests: 6 test suites")
        println("✅ Coverage: State management, routes, events, transitions, error handling, edge cases")
        
    } catch (e: Exception) {
        println("❌ Test failed with error: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}