package main

import com.ultraviolince.mykitchen.di.jsAppModule
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.ultraviolince.mykitchen.recipes.presentation.RecipeStateManager
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

// State manager
private lateinit var stateManager: RecipeStateManager

fun main() {
    window.onload = {
        console.log("My Kitchen Web App loaded!")
        initializeApp()
    }
}

fun initializeApp() {
    try {
        // Initialize Koin DI
        stopKoin() // In case it was already initialized
        startKoin {
            modules(jsAppModule)
        }

        // Get use cases and initialize state manager
        val koin = org.koin.core.context.GlobalContext.get()
        val recipesUseCases = koin.get<Recipes>()
        stateManager = RecipeStateManager(recipesUseCases, appScope)

        // Setup UI
        setupWebApp()

        // Observe state changes
        observeState()

    } catch (e: Exception) {
        console.error("Failed to initialize app:", e)
        showError("Failed to initialize app: ${e.message}")
    }
}

fun setupWebApp() {
    val container = document.getElementById("app")
    
    if (container != null) {
        container.innerHTML = ""
        container.append {
            div {
                h1 { +"My Kitchen - Web App" }
                h3 { +"Powered by Kotlin Multiplatform & Shared Architecture" }
                
                p { +"This web app uses the complete shared business logic including login, backend sync, and recipe storage!" }
                
                // Login section
                div {
                    id = "login-section"
                }
                
                // Recipe actions section
                div {
                    id = "recipe-actions"
                    attributes["style"] = "margin-top: 20px;"
                }
                
                // Status section
                div {
                    id = "status-section"
                    attributes["style"] = "margin-top: 20px;"
                }
                
                // Recipes list
                div {
                    id = "recipes-container"
                    attributes["style"] = "margin-top: 30px;"
                    h2 { +"My Recipes" }
                    ul {
                        id = "recipes-list"
                    }
                }
            }
        }
    }
    
    updateLoginUI(LoginState.LoginEmpty)
    updateRecipeActionsUI(false)
}

fun observeState() {
    // Observe login state
    appScope.launch {
        stateManager.loginState.collectLatest { loginState ->
            console.log("Login state changed: $loginState")
            updateLoginUI(loginState)
            updateRecipeActionsUI(loginState is LoginState.LoginSuccess)
            updateStatusUI(loginState)
        }
    }

    // Observe recipes
    appScope.launch {
        stateManager.recipesState.collectLatest { recipes ->
            console.log("Recipes updated: ${recipes.size} recipes")
            updateRecipesUI(recipes)
        }
    }

    // Observe loading state
    appScope.launch {
        stateManager.isLoading.collectLatest { isLoading ->
            updateLoadingUI(isLoading)
        }
    }
}

fun updateLoginUI(loginState: LoginState) {
    val loginSection = document.getElementById("login-section")
    
    loginSection?.innerHTML = ""
    loginSection?.append {
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
                            label { +"Server URL:" }
                            input {
                                type = InputType.url
                                id = "server-input"
                                placeholder = "http://localhost:5000"
                                value = "http://localhost:5000"
                            }
                        }
                        
                        div {
                            label { +"Username:" }
                            input {
                                type = InputType.text
                                id = "username-input"
                                placeholder = "Enter username"
                                value = "test@example.com"
                            }
                        }
                        
                        div {
                            label { +"Password:" }
                            input {
                                type = InputType.password
                                id = "password-input"
                                placeholder = "Enter password"
                                value = "password"
                            }
                        }
                        
                        button {
                            type = ButtonType.submit
                            +"Login"
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
                        onClickFunction = {
                            stateManager.logout()
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
                        onClickFunction = {
                            updateLoginUI(LoginState.LoginEmpty)
                        }
                    }
                }
            }
        }
    }
}

fun updateRecipeActionsUI(isLoggedIn: Boolean) {
    val actionsSection = document.getElementById("recipe-actions")
    
    actionsSection?.innerHTML = ""
    actionsSection?.append {
        div {
            h2 { +"Recipe Actions" }
            
            // Add recipe form
            form {
                onSubmitFunction = { event ->
                    event.preventDefault()
                    handleAddRecipe(event)
                }
                
                div {
                    label { +"Recipe Title:" }
                    input {
                        type = InputType.text
                        id = "recipe-title-input"
                        placeholder = "Enter recipe title"
                        required = true
                    }
                }
                
                div {
                    label { +"Recipe Content:" }
                    textArea {
                        id = "recipe-content-input"
                        placeholder = "Enter recipe instructions, ingredients, etc."
                        required = true
                        rows = "4"
                    }
                }
                
                button {
                    type = ButtonType.submit
                    if (isLoggedIn) {
                        +"Add Recipe (will sync to backend)"
                    } else {
                        +"Add Recipe (local storage only)"
                    }
                }
            }
        }
    }
}

fun updateStatusUI(loginState: LoginState) {
    val statusSection = document.getElementById("status-section")
    
    statusSection?.innerHTML = ""
    statusSection?.append {
        div {
            h2 { +"Status" }
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

fun updateRecipesUI(recipes: List<Recipe>) {
    val recipesList = document.getElementById("recipes-list")
    
    recipesList?.innerHTML = ""
    recipesList?.append {
        if (recipes.isEmpty()) {
            li {
                p { +"No recipes yet. Add one above!" }
            }
        } else {
            recipes.forEach { recipe ->
                li {
                    div {
                        attributes["style"] = "border: 1px solid #ddd; padding: 10px; margin: 5px 0; border-radius: 5px;"
                        
                        h3 { +recipe.title }
                        p { +recipe.content }
                        p {
                            attributes["style"] = "font-size: 12px; color: #666;"
                            +"Created: ${formatTimestamp(recipe.timestamp)}"
                            recipe.id?.let { id -> +" | ID: $id" }
                        }
                        
                        button {
                            +"Delete"
                            attributes["style"] = "background: #dc3545; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;"
                            onClickFunction = {
                                if (window.confirm("Are you sure you want to delete '${recipe.title}'?")) {
                                    stateManager.deleteRecipe(recipe)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun updateLoadingUI(isLoading: Boolean) {
    // Could add loading indicators here
    if (isLoading) {
        console.log("Loading...")
    }
}

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
    
    stateManager.login(server, username, password)
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
    
    stateManager.addRecipe(title, content)
    
    // Clear form
    titleInput?.value = ""
    contentInput?.value = ""
}

fun formatTimestamp(timestamp: Long): String {
    return kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp).toString()
}

fun showError(message: String) {
    val container = document.getElementById("app")
    container?.innerHTML = """
        <div style="color: red; padding: 20px; border: 1px solid red; margin: 20px;">
            <h2>Error</h2>
            <p>$message</p>
        </div>
    """.trimIndent()
}
