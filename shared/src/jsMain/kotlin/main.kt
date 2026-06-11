package main

import com.ultraviolince.mykitchen.di.jsAppModule
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.ultraviolince.mykitchen.recipes.presentation.SharedAppCoordinator
import com.ultraviolince.mykitchen.recipes.presentation.AppScreen
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event

// Global application scope
private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

// Shared app coordinator - this is the EXACT same business logic as Android!
private lateinit var appCoordinator: SharedAppCoordinator

@OptIn(kotlin.time.ExperimentalTime::class)

fun main() {
    window.onload = {
        console.log("My Kitchen Web App - Now using EXACT same business logic as Android!")
        initializeSharedApp()
    }
}

fun initializeSharedApp() {
    try {
        // Initialize Koin DI (same as Android)
        stopKoin()
        startKoin {
            modules(jsAppModule)
        }
        
        // Get use cases (same as Android)
        val koin = org.koin.core.context.GlobalContext.get()
        val recipesUseCases = koin.get<Recipes>()
        
        // Initialize shared app coordinator - this replaces individual ViewModels
        appCoordinator = SharedAppCoordinator(recipesUseCases)
        
        // Setup UI container
        setupAppContainer()
        
        // Start observing shared state (same patterns as Android)
        observeAppState()
        
    } catch (e: Exception) {
        console.error("Failed to initialize app:", e)
        showError("Failed to initialize app: ${e.message}")
    }
}

fun setupAppContainer() {
    val container = document.getElementById("app")
    container?.innerHTML = ""
    container?.append {
        div {
            attributes["style"] = "max-width: 1200px; margin: 0 auto; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;"
            
            // Header
            div {
                attributes["style"] = "text-align: center; margin-bottom: 30px; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 12px;"
                h1 { 
                    attributes["style"] = "margin: 0; font-size: 2.5rem;"
                    +"My Kitchen - Web App" 
                }
                p { 
                    attributes["style"] = "margin: 10px 0 0 0; opacity: 0.9; font-size: 1.1rem;"
                    +"🔄 Now using the EXACT same business logic and state management as the Android app!" 
                }
            }
            
            // Navigation (will be shown/hidden based on current screen)
            div {
                id = "navigation"
                attributes["style"] = "display: none; margin-bottom: 20px; text-align: center;"
            }
            
            // Screen container
            div {
                id = "screen-container"
                attributes["style"] = "min-height: 400px;"
            }
            
            // Status footer
            div {
                id = "status-footer"
                attributes["style"] = "margin-top: 30px; padding: 15px; background: #f8f9fa; border-radius: 8px; font-size: 0.9rem;"
            }
        }
    }
}

// Observer pattern - exactly like Android ViewModels!
fun observeAppState() {
    // Observe navigation state
    appScope.launch {
        appCoordinator.currentScreen.collectLatest { screen ->
            renderScreen(screen)
            updateNavigation(screen)
        }
    }
    
    // Observe login state  
    appScope.launch {
        appCoordinator.loginManager.loginUiState.collectLatest { loginState ->
            if (loginState.isLoggedIn && appCoordinator.currentScreen.value == AppScreen.LOGIN) {
                appCoordinator.navigateToRecipes()
            }
        }
    }
}

fun renderScreen(screen: AppScreen) {
    val container = document.getElementById("screen-container")
    container?.innerHTML = ""
    
    when (screen) {
        AppScreen.LOGIN -> renderLoginScreen(container)
        AppScreen.RECIPES -> renderRecipesScreen(container)
        AppScreen.ADD_RECIPE -> renderAddRecipeScreen(container)
    }
}

fun updateNavigation(currentScreen: AppScreen) {
    val navigationElement = document.getElementById("navigation")
    navigationElement?.innerHTML = ""
    if (currentScreen == AppScreen.LOGIN) {
        navigationElement?.setAttribute("style", "display: none; margin-bottom: 20px; text-align: center;")
    } else {
        navigationElement?.setAttribute("style", "display: block; margin-bottom: 20px; text-align: center;")
    }
    
    if (currentScreen != AppScreen.LOGIN) {
        navigationElement?.append {
            div {
                attributes["style"] = "display: flex; justify-content: center; gap: 10px;"
                
                button {
                    +"📖 My Recipes"
                    attributes["style"] = "padding: 10px 20px; border: 2px solid #007bff; border-radius: 6px; cursor: pointer; transition: all 0.3s; ${
                        if (currentScreen == AppScreen.RECIPES) "background: #007bff; color: white;" else "background: white; color: #007bff;"
                    }"
                    onClickFunction = { appCoordinator.navigateToRecipes() }
                }
                
                button {
                    +"➕ Add Recipe"
                    attributes["style"] = "padding: 10px 20px; border: 2px solid #28a745; border-radius: 6px; cursor: pointer; transition: all 0.3s; ${
                        if (currentScreen == AppScreen.ADD_RECIPE) "background: #28a745; color: white;" else "background: white; color: #28a745;"
                    }"
                    onClickFunction = { appCoordinator.navigateToAddRecipe() }
                }
                
                button {
                    +"🚪 Logout"
                    attributes["style"] = "padding: 10px 20px; border: 2px solid #dc3545; border-radius: 6px; cursor: pointer; background: white; color: #dc3545; transition: all 0.3s;"
                    onClickFunction = { 
                        appCoordinator.loginManager.logout()
                        appCoordinator.navigateToLogin()
                    }
                }
            }
        }
    }
}

// Login Screen - uses SharedLoginManager (same logic as Android LoginViewModel!)
fun renderLoginScreen(container: org.w3c.dom.Element?) {
    appScope.launch {
        appCoordinator.loginManager.loginUiState.collect { loginState ->
            container?.innerHTML = ""
            container?.append {
                div {
                    attributes["style"] = "max-width: 400px; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1);"
                    
                    h2 { 
                        attributes["style"] = "text-align: center; margin-bottom: 30px; color: #333;"
                        +"🔐 Backend Login" 
                    }
                    
                    if (loginState.isLoggedIn) {
                        div {
                            attributes["style"] = "text-align: center;"
                            p {
                                attributes["style"] = "color: #28a745; font-size: 1.2rem; margin-bottom: 20px;"
                                +"✅ Successfully logged in!"
                            }
                            p { +"Redirecting to recipes..." }
                        }
                    } else {
                        form {
                            onSubmitFunction = { event -> 
                                event.preventDefault()
                                appCoordinator.loginManager.login()
                            }
                            
                            div {
                                attributes["style"] = "margin-bottom: 20px;"
                                label { 
                                    attributes["style"] = "display: block; margin-bottom: 8px; font-weight: 600; color: #555;"
                                    +"🌐 Server URL:" 
                                }
                                input {
                                    type = InputType.url
                                    id = "server-input"
                                    value = loginState.server
                                    disabled = loginState.isLoading
                                    attributes["style"] = "width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 1rem; transition: border-color 0.3s;"
                                    attributes["onchange"] = "updateLoginField('server', this.value)"
                                }
                            }
                            
                            div {
                                attributes["style"] = "margin-bottom: 20px;"
                                label { 
                                    attributes["style"] = "display: block; margin-bottom: 8px; font-weight: 600; color: #555;"
                                    +"👤 Username:" 
                                }
                                input {
                                    type = InputType.text
                                    id = "username-input"
                                    value = loginState.username
                                    disabled = loginState.isLoading
                                    attributes["style"] = "width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 1rem;"
                                    attributes["onchange"] = "updateLoginField('username', this.value)"
                                }
                            }
                            
                            div {
                                attributes["style"] = "margin-bottom: 30px;"
                                label { 
                                    attributes["style"] = "display: block; margin-bottom: 8px; font-weight: 600; color: #555;"
                                    +"🔑 Password:" 
                                }
                                input {
                                    type = InputType.password
                                    id = "password-input"
                                    value = loginState.password
                                    disabled = loginState.isLoading
                                    attributes["style"] = "width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 1rem;"
                                    attributes["onchange"] = "updateLoginField('password', this.value)"
                                }
                            }
                            
                            loginState.loginError?.let { error ->
                                div {
                                    attributes["style"] = "background: #fee; border: 1px solid #fcc; color: #c66; padding: 12px; border-radius: 6px; margin-bottom: 20px;"
                                    +"❌ Error: $error"
                                }
                            }
                            
                            button {
                                type = ButtonType.submit
                                disabled = loginState.isLoading
                                attributes["style"] = "width: 100%; padding: 14px; background: ${if (loginState.isLoading) "#ccc" else "#007bff"}; color: white; border: none; border-radius: 6px; font-size: 1.1rem; font-weight: 600; cursor: ${if (loginState.isLoading) "not-allowed" else "pointer"}; transition: background 0.3s;"
                                if (loginState.isLoading) {
                                    +"🔄 Logging in..."
                                } else {
                                    +"🚀 Login"
                                }
                            }
                        }
                    }
                }
            }
            
            // Add JavaScript functions for input handling
            document.asDynamic().updateLoginField = { field: String, value: String ->
                when (field) {
                    "server" -> appCoordinator.loginManager.updateServer(value)
                    "username" -> appCoordinator.loginManager.updateUsername(value)  
                    "password" -> appCoordinator.loginManager.updatePassword(value)
                }
            }
        }
    }
}

// Recipes Screen - uses SharedRecipesManager (same logic as Android RecipeViewModel!)
fun renderRecipesScreen(container: org.w3c.dom.Element?) {
    appScope.launch {
        appCoordinator.recipesManager.recipesUiState.collect { recipesState ->
            container?.innerHTML = ""
            container?.append {
                div {
                    h2 { 
                        attributes["style"] = "color: #333; margin-bottom: 20px; text-align: center;"
                        +"📖 My Recipe Collection" 
                    }
                    
                    // Status section
                    div {
                        attributes["style"] = "background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 25px;"
                        p { 
                            attributes["style"] = "margin: 0; font-weight: 600;"
                            +"📡 Backend Status: ${recipesState.syncStatus}" 
                        }
                        p { 
                            attributes["style"] = "margin: 5px 0 0 0;"
                            +"💾 Local Storage: ✅ Active"
                        }
                    }
                    
                    // Recipes list
                    if (recipesState.recipes.isEmpty()) {
                        div {
                            attributes["style"] = "text-align: center; padding: 40px; background: #fff; border-radius: 8px; border: 2px dashed #ddd;"
                            p { 
                                attributes["style"] = "font-size: 1.2rem; color: #666; margin-bottom: 15px;"
                                +"📝 No recipes yet" 
                            }
                            button {
                                +"➕ Add your first recipe!"
                                attributes["style"] = "background: #28a745; color: white; border: none; padding: 12px 24px; border-radius: 6px; font-size: 1rem; cursor: pointer;"
                                onClickFunction = { appCoordinator.navigateToAddRecipe() }
                            }
                        }
                    } else {
                        div {
                            attributes["style"] = "display: grid; gap: 15px;"
                            recipesState.recipes.forEach { recipe ->
                                div {
                                    attributes["style"] = "background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); transition: transform 0.2s;"
                                    
                                    div {
                                        attributes["style"] = "display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;"
                                        h3 { 
                                            attributes["style"] = "margin: 0; color: #333; flex-grow: 1;"
                                            +recipe.title 
                                        }
                                        button {
                                            +"🗑️ Delete"
                                            attributes["style"] = "background: #dc3545; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 0.9rem;"
                                            onClickFunction = {
                                                if (window.confirm("Delete '${recipe.title}'?")) {
                                                    appCoordinator.recipesManager.deleteRecipe(recipe)
                                                }
                                            }
                                        }
                                    }
                                    
                                    p { 
                                        attributes["style"] = "color: #666; line-height: 1.4; margin-bottom: 10px;"
                                        +if (recipe.content.length > 150) {
                                            recipe.content.take(150) + "..."
                                        } else {
                                            recipe.content
                                        }
                                    }
                                    
                                    p { 
                                        attributes["style"] = "font-size: 0.85rem; color: #999; margin: 0;"
                                        +"📅 Created: ${kotlinx.datetime.Instant.fromEpochMilliseconds(recipe.timestamp).toString().take(19).replace("T", " ")}"
                                        recipe.id?.let { id -> +" | 🆔 ID: $id" }
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

// Add Recipe Screen - uses SharedAddRecipeManager (same logic as Android AddEditRecipeViewModel!)
fun renderAddRecipeScreen(container: org.w3c.dom.Element?) {
    appScope.launch {
        appCoordinator.addRecipeManager.addRecipeUiState.collect { addRecipeState ->
            container?.innerHTML = ""
            container?.append {
                div {
                    attributes["style"] = "max-width: 600px; margin: 0 auto;"
                    
                    h2 { 
                        attributes["style"] = "color: #333; margin-bottom: 25px; text-align: center;"
                        +"➕ Add New Recipe" 
                    }
                    
                    form {
                        onSubmitFunction = { event ->
                            event.preventDefault()
                            appCoordinator.saveRecipeAndNavigateBack()
                        }
                        
                        div {
                            attributes["style"] = "background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1);"
                            
                            div {
                                attributes["style"] = "margin-bottom: 20px;"
                                label { 
                                    attributes["style"] = "display: block; margin-bottom: 8px; font-weight: 600; color: #555;"
                                    +"📝 Recipe Title:" 
                                }
                                input {
                                    type = InputType.text
                                    value = addRecipeState.title
                                    disabled = addRecipeState.isLoading
                                    placeholder = "Enter a delicious recipe title..."
                                    attributes["style"] = "width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 1rem;"
                                    attributes["onchange"] = "updateAddRecipeField('title', this.value)"
                                }
                            }
                            
                            div {
                                attributes["style"] = "margin-bottom: 25px;"
                                label { 
                                    attributes["style"] = "display: block; margin-bottom: 8px; font-weight: 600; color: #555;"
                                    +"📋 Recipe Instructions:" 
                                }
                                textArea {
                                    +addRecipeState.content
                                    disabled = addRecipeState.isLoading
                                    placeholder = "Enter recipe instructions, ingredients, cooking steps, etc..."
                                    rows = "8"
                                    attributes["style"] = "width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 1rem; resize: vertical;"
                                    attributes["onchange"] = "updateAddRecipeField('content', this.value)"
                                }
                            }
                            
                            div {
                                attributes["style"] = "display: flex; gap: 15px; justify-content: center;"
                                
                                button {
                                    type = ButtonType.submit
                                    disabled = !addRecipeState.canSave
                                    attributes["style"] = "padding: 14px 28px; background: ${if (addRecipeState.canSave) "#28a745" else "#ccc"}; color: white; border: none; border-radius: 6px; font-size: 1rem; font-weight: 600; cursor: ${if (addRecipeState.canSave) "pointer" else "not-allowed"};"
                                    if (addRecipeState.isLoading) {
                                        +"🔄 Saving..."
                                    } else {
                                        +"💾 Save Recipe"
                                    }
                                }
                                
                                button {
                                    type = ButtonType.button
                                    +"❌ Cancel"
                                    attributes["style"] = "padding: 14px 28px; background: #6c757d; color: white; border: none; border-radius: 6px; font-size: 1rem; cursor: pointer;"
                                    onClickFunction = { appCoordinator.navigateToRecipes() }
                                }
                            }
                        }
                    }
                }
            }
            
            // Add JavaScript functions for input handling
            document.asDynamic().updateAddRecipeField = { field: String, value: String ->
                when (field) {
                    "title" -> appCoordinator.addRecipeManager.updateTitle(value)
                    "content" -> appCoordinator.addRecipeManager.updateContent(value)
                }
            }
        }
    }
}

fun showError(message: String) {
    val container = document.getElementById("app")
    container?.innerHTML = """
        <div style="color: #dc3545; background: #fee; border: 2px solid #fcc; padding: 20px; margin: 20px; border-radius: 8px; text-align: center;">
            <h2>⚠️ Error</h2>
            <p>$message</p>
            <button onclick="location.reload()" style="background: #dc3545; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer;">
                🔄 Reload App
            </button>
        </div>
    """.trimIndent()
}
