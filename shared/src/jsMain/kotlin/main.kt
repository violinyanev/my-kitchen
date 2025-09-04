package main

import com.ultraviolince.mykitchen.di.jsAppModule
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.strong
import kotlinx.html.textArea
import kotlinx.html.ul
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event

// Global application scope
private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

// State manager - mirrors the Android ViewModel pattern
private lateinit var recipesUseCases: Recipes

// Current screen state - mirrors Android navigation
enum class Screen { LOGIN, RECIPES, ADD_RECIPE }
private var currentScreen = Screen.LOGIN

fun main() {
    window.onload = {
        console.log("My Kitchen Web App loaded!")
        initializeApp()
    }
}

fun initializeApp() {
    try {
        // Initialize Koin DI (same pattern as Android)
        stopKoin() // In case it was already initialized
        startKoin {
            modules(jsAppModule)
        }

        // Get use cases (same pattern as Android ViewModels)
        val koin = org.koin.core.context.GlobalContext.get()
        recipesUseCases = koin.get<Recipes>()

        // Setup UI and navigation (mirrors Android Activity/Fragment setup)
        setupWebApp()
        
        // Observe state changes (mirrors Android ViewModel/LiveData pattern)
        observeState()

    } catch (e: Exception) {
        console.error("Failed to initialize app:", e)
        showError("Failed to initialize app: ${e.message}")
    }
}

// Setup main container - mirrors Android Activity.onCreate()
fun setupWebApp() {
    val container = document.getElementById("app")
    
    if (container != null) {
        container.innerHTML = ""
        container.append {
            div {
                h1 { +"My Kitchen - Web App" }
                h3 { +"Powered by Kotlin Multiplatform & Shared Architecture" }
                
                p { +"This web app now uses the same composable structure as the Android app!" }
                
                // Navigation tabs - mirrors Android bottom navigation
                div {
                    id = "navigation"
                    attributes["style"] = "margin: 20px 0; border-bottom: 1px solid #ccc;"
                    
                    button {
                        +"Login"
                        attributes["style"] = "margin-right: 10px; padding: 10px; ${if (currentScreen == Screen.LOGIN) "background: #007bff; color: white;" else ""}"
                        onClickFunction = { navigateToScreen(Screen.LOGIN) }
                    }
                    button {
                        +"My Recipes"
                        attributes["style"] = "margin-right: 10px; padding: 10px; ${if (currentScreen == Screen.RECIPES) "background: #007bff; color: white;" else ""}"
                        onClickFunction = { navigateToScreen(Screen.RECIPES) }
                    }
                    button {
                        +"Add Recipe"
                        attributes["style"] = "padding: 10px; ${if (currentScreen == Screen.ADD_RECIPE) "background: #007bff; color: white;" else ""}"
                        onClickFunction = { navigateToScreen(Screen.ADD_RECIPE) }
                    }
                }
                
                // Screen container - mirrors Android fragment container
                div {
                    id = "screen-container"
                    attributes["style"] = "margin-top: 20px;"
                }
            }
        }
    }
    
    renderCurrentScreen()
}

// Navigation - mirrors Android NavController.navigate()
fun navigateToScreen(screen: Screen) {
    currentScreen = screen
    renderCurrentScreen()
}

// Screen rendering - mirrors Android Fragment.onCreateView()
fun renderCurrentScreen() {
    val container = document.getElementById("screen-container")
    container?.innerHTML = ""
    
    when (currentScreen) {
        Screen.LOGIN -> renderLoginScreen(container)
        Screen.RECIPES -> renderRecipesScreen(container)
        Screen.ADD_RECIPE -> renderAddRecipeScreen(container)
    }
}

// Login Screen - mirrors Android LoginScreen composable
fun renderLoginScreen(container: org.w3c.dom.Element?) {
    appScope.launch {
        recipesUseCases.getSyncState().collect { loginState ->
            container?.innerHTML = ""
            container?.append {
                when (loginState) {
                    is LoginState.LoginEmpty -> {
                        div {
                            h2 { +"Backend Login" }
                            form {
                                onSubmitFunction = { event ->
                                    event.preventDefault()
                                    handleLogin(event)
                                }
                                
                                div {
                                    attributes["style"] = "margin: 10px 0;"
                                    label { +"Server URL:" }
                                    input {
                                        type = InputType.url
                                        id = "server-input"
                                        placeholder = "http://localhost:5000"
                                        value = "http://localhost:5000"
                                        attributes["style"] = "width: 100%; padding: 8px; margin-top: 5px;"
                                    }
                                }
                                
                                div {
                                    attributes["style"] = "margin: 10px 0;"
                                    label { +"Username:" }
                                    input {
                                        type = InputType.text
                                        id = "username-input"
                                        placeholder = "Enter username"
                                        value = "test@example.com"
                                        attributes["style"] = "width: 100%; padding: 8px; margin-top: 5px;"
                                    }
                                }
                                
                                div {
                                    attributes["style"] = "margin: 10px 0;"
                                    label { +"Password:" }
                                    input {
                                        type = InputType.password
                                        id = "password-input"
                                        placeholder = "Enter password"
                                        value = "password"
                                        attributes["style"] = "width: 100%; padding: 8px; margin-top: 5px;"
                                    }
                                }
                                
                                button {
                                    type = ButtonType.submit
                                    +"Login"
                                    attributes["style"] = "background: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;"
                                }
                            }
                        }
                    }
                    is LoginState.LoginPending -> {
                        div {
                            h2 { +"Logging in..." }
                            p { +"Please wait..." }
                        }
                    }
                    is LoginState.LoginSuccess -> {
                        div {
                            h2 { +"✅ Logged In Successfully" }
                            button {
                                +"Logout"
                                attributes["style"] = "background: #dc3545; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;"
                                onClickFunction = {
                                    appScope.launch {
                                        recipesUseCases.logout()
                                    }
                                }
                            }
                        }
                    }
                    is LoginState.LoginFailure -> {
                        div {
                            h2 { +"❌ Login Failed" }
                            p { +"Error: ${loginState.error}" }
                            button {
                                +"Try Again"
                                attributes["style"] = "background: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;"
                                onClickFunction = { renderCurrentScreen() }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Recipes Screen - mirrors Android RecipeScreen composable
fun renderRecipesScreen(container: org.w3c.dom.Element?) {
    container?.append {
        div {
            h2 { +"My Recipes" }
            
            // Status section - mirrors Android StatusSection composable
            div {
                id = "status-section"
                attributes["style"] = "background: #f8f9fa; padding: 15px; border-radius: 4px; margin: 10px 0;"
            }
            
            // Recipes list - mirrors Android LazyColumn
            ul {
                id = "recipes-list"
                attributes["style"] = "list-style: none; padding: 0;"
            }
        }
    }
    
    // Update status and recipes - mirrors Android ViewModel observation
    appScope.launch {
        recipesUseCases.getSyncState().collect { loginState ->
            updateStatus(loginState)
        }
    }
    
    appScope.launch {
        recipesUseCases.getRecipes().collect { recipes ->
            updateRecipesList(recipes)
        }
    }
}

// Add Recipe Screen - mirrors Android AddEditRecipeScreen composable
fun renderAddRecipeScreen(container: org.w3c.dom.Element?) {
    container?.append {
        div {
            h2 { +"Add Recipe" }
            form {
                onSubmitFunction = { event ->
                    event.preventDefault()
                    handleAddRecipe(event)
                }
                
                div {
                    attributes["style"] = "margin: 10px 0;"
                    label { +"Recipe Title:" }
                    input {
                        type = InputType.text
                        id = "recipe-title-input"
                        placeholder = "Enter recipe title"
                        required = true
                        attributes["style"] = "width: 100%; padding: 8px; margin-top: 5px;"
                    }
                }
                
                div {
                    attributes["style"] = "margin: 10px 0;"
                    label { +"Recipe Content:" }
                    textArea {
                        id = "recipe-content-input"
                        placeholder = "Enter recipe instructions, ingredients, etc."
                        required = true
                        rows = "8"
                        attributes["style"] = "width: 100%; padding: 8px; margin-top: 5px; resize: vertical;"
                    }
                }
                
                div {
                    attributes["style"] = "margin: 20px 0;"
                    button {
                        type = ButtonType.submit
                        +"Add Recipe"
                        attributes["style"] = "background: #28a745; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px;"
                    }
                    button {
                        type = ButtonType.button
                        +"Cancel"
                        attributes["style"] = "background: #6c757d; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer;"
                        onClickFunction = { navigateToScreen(Screen.RECIPES) }
                    }
                }
            }
        }
    }
}

// State observation methods - mirror Android ViewModel observation

fun observeState() {
    // This method mirrors Android lifecycle observation
    // The actual observation happens in individual screen renders
}

fun updateStatus(loginState: LoginState) {
    val statusSection = document.getElementById("status-section")
    
    statusSection?.innerHTML = ""
    statusSection?.append {
        div {
            p {
                strong { +"Backend Connection: " }
                when (loginState) {
                    is LoginState.LoginEmpty -> +"Not connected"
                    is LoginState.LoginPending -> +"Connecting..."
                    is LoginState.LoginSuccess -> +"✅ Connected & Synced"
                    is LoginState.LoginFailure -> +"❌ Connection Failed"
                }
            }
            p {
                strong { +"Data Storage: " }
                +"✅ Browser Local Storage Active"
            }
        }
    }
}

fun updateRecipesList(recipes: List<Recipe>) {
    val recipesList = document.getElementById("recipes-list")
    
    recipesList?.innerHTML = ""
    recipesList?.append {
        if (recipes.isEmpty()) {
            li {
                p { 
                    +"No recipes yet. "
                    button {
                        +"Add one here!"
                        attributes["style"] = "background: none; border: none; color: #007bff; text-decoration: underline; cursor: pointer;"
                        onClickFunction = { navigateToScreen(Screen.ADD_RECIPE) }
                    }
                }
            }
        } else {
            recipes.forEach { recipe ->
                li {
                    div {
                        attributes["style"] = "border: 1px solid #ddd; padding: 15px; margin: 10px 0; border-radius: 8px; background: white;"
                        
                        h3 { +recipe.title }
                        p { 
                            attributes["style"] = "color: #666; margin: 10px 0;"
                            val preview = if (recipe.content.length > 150) {
                                recipe.content.take(150) + "..."
                            } else {
                                recipe.content
                            }
                            +preview
                        }
                        p {
                            attributes["style"] = "font-size: 12px; color: #999; margin-top: 15px;"
                            +"Created: ${formatTimestamp(recipe.timestamp)}"
                            recipe.id?.let { id -> +" | ID: $id" }
                        }
                        
                        button {
                            +"Delete"
                            attributes["style"] = "background: #dc3545; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;"
                            onClickFunction = {
                                if (window.confirm("Are you sure you want to delete '${recipe.title}'?")) {
                                    appScope.launch {
                                        recipesUseCases.deleteRecipe(recipe)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Event handlers - mirror Android event handling

fun handleLogin(event: Event) {
    val serverInput = document.getElementById("server-input") as? HTMLInputElement
    val usernameInput = document.getElementById("username-input") as? HTMLInputElement
    val passwordInput = document.getElementById("password-input") as? HTMLInputElement
    
    val server = serverInput?.value?.trim() ?: ""
    val username = usernameInput?.value?.trim() ?: ""
    val password = passwordInput?.value?.trim() ?: ""
    
    if (server.isEmpty() || username.isEmpty() || password.isEmpty()) {
        window.alert("Please fill in all fields")
        return
    }
    
    appScope.launch {
        recipesUseCases.login(server, username, password)
    }
}

fun handleAddRecipe(event: Event) {
    val titleInput = document.getElementById("recipe-title-input") as? HTMLInputElement
    val contentInput = document.getElementById("recipe-content-input") as? HTMLTextAreaElement
    
    val title = titleInput?.value?.trim() ?: ""
    val content = contentInput?.value?.trim() ?: ""
    
    if (title.isEmpty() || content.isEmpty()) {
        window.alert("Please fill in both title and content")
        return
    }
    
    appScope.launch {
        val recipe = Recipe(
            title = title,
            content = content,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
        recipesUseCases.addRecipe(recipe)
        // Clear form
        titleInput?.value = ""
        contentInput?.value = ""
        // Navigate back to recipes
        navigateToScreen(Screen.RECIPES)
    }
}

fun formatTimestamp(timestamp: Long): String {
    return kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp).toString()
}

fun showError(message: String) {
    val container = document.getElementById("app")
    container?.innerHTML = """
        <div style="color: red; padding: 20px; border: 1px solid red; margin: 20px; border-radius: 4px;">
            <h2>Error</h2>
            <p>$message</p>
        </div>
    """.trimIndent()
}
