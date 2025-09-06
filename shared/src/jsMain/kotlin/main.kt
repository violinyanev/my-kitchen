package main

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.js.onClickFunction
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.ul
import kotlinx.html.id
import kotlinx.html.classes
import kotlinx.html.textArea
import kotlinx.html.input
import kotlinx.html.InputType
import kotlinx.html.label
import kotlinx.html.form
import kotlinx.html.textInput
import kotlinx.html.passwordInput
import kotlinx.html.emailInput
import kotlinx.html.ButtonType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement

fun main() {
    window.onload = {
        console.log("My Kitchen Web App loaded!")
        setupWebApp()
    }
}

fun setupWebApp() {
    val container = document.getElementById("app")
    
    if (container != null) {
        container.innerHTML = ""
        container.append {
            div(classes = "app-container") {
                div(classes = "header") {
                    h1 { +"My Kitchen" }
                    div(classes = "header-actions") {
                        button(classes = "btn btn-secondary") {
                            id = "login-btn"
                            +"Login"
                            onClickFunction = { showLoginDialog() }
                        }
                        button(classes = "btn btn-secondary") {
                            id = "logout-btn"
                            +"Logout"
                            attributes["style"] = "display: none;"
                            onClickFunction = { logout() }
                        }
                    }
                }
                
                div(classes = "main") {
                    div(classes = "recipes-section") {
                        div(classes = "recipes-header") {
                            h2 { +"Your Recipes" }
                            button(classes = "btn btn-primary") {
                                id = "add-recipe-btn"
                                +"Add Recipe"
                                onClickFunction = { showAddRecipeDialog() }
                            }
                        }
                        
                        div(classes = "login-status") {
                            id = "login-status"
                        }
                        
                        div(classes = "recipes-list") {
                            id = "recipes-list"
                        }
                    }
                }
                
                // Login Dialog
                div(classes = "modal") {
                    id = "login-modal"
                    attributes["style"] = "display: none;"
                    div(classes = "modal-content") {
                        div(classes = "modal-header") {
                            h3 { +"Login to Backend" }
                            button(classes = "close-btn") {
                                +"×"
                                onClickFunction = { hideLoginDialog() }
                            }
                        }
                        div(classes = "modal-body") {
                            form {
                                div(classes = "form-group") {
                                    label {
                                        htmlFor = "server-input"
                                        +"Server URL"
                                    }
                                    textInput(classes = "form-control") {
                                        id = "server-input"
                                        placeholder = "http://localhost:5000"
                                        value = "http://localhost:5000"
                                    }
                                }
                                div(classes = "form-group") {
                                    label {
                                        htmlFor = "email-input"
                                        +"Email"
                                    }
                                    emailInput(classes = "form-control") {
                                        id = "email-input"
                                        placeholder = "Enter your email"
                                    }
                                }
                                div(classes = "form-group") {
                                    label {
                                        htmlFor = "password-input"
                                        +"Password"
                                    }
                                    passwordInput(classes = "form-control") {
                                        id = "password-input"
                                        placeholder = "Enter your password"
                                    }
                                }
                                div(classes = "form-actions") {
                                    button(classes = "btn btn-primary") {
                                        id = "login-submit-btn"
                                        +"Login"
                                        onClickFunction = { handleLogin() }
                                    }
                                    button(classes = "btn btn-secondary") {
                                        type = ButtonType.button
                                        +"Cancel"
                                        onClickFunction = { hideLoginDialog() }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Add/Edit Recipe Dialog
                div(classes = "modal") {
                    id = "recipe-modal"
                    attributes["style"] = "display: none;"
                    div(classes = "modal-content") {
                        div(classes = "modal-header") {
                            h3 {
                                id = "recipe-modal-title"
                                +"Add Recipe"
                            }
                            button(classes = "close-btn") {
                                +"×"
                                onClickFunction = { hideRecipeDialog() }
                            }
                        }
                        div(classes = "modal-body") {
                            form {
                                div(classes = "form-group") {
                                    label {
                                        htmlFor = "recipe-title-input"
                                        +"Title"
                                    }
                                    textInput(classes = "form-control") {
                                        id = "recipe-title-input"
                                        placeholder = "Enter recipe title"
                                    }
                                }
                                div(classes = "form-group") {
                                    label {
                                        htmlFor = "recipe-content-input"
                                        +"Content"
                                    }
                                    textArea(classes = "form-control") {
                                        id = "recipe-content-input"
                                        placeholder = "Enter recipe content"
                                        rows = "6"
                                    }
                                }
                                div(classes = "form-actions") {
                                    button(classes = "btn btn-primary") {
                                        id = "recipe-submit-btn"
                                        +"Save Recipe"
                                        onClickFunction = { handleRecipeSubmit() }
                                    }
                                    button(classes = "btn btn-secondary") {
                                        type = ButtonType.button
                                        +"Cancel"
                                        onClickFunction = { hideRecipeDialog() }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Load existing recipes
    loadRecipes()
}

fun loadRecipes() {
    val recipesList = document.getElementById("recipes-list")
    recipesList?.innerHTML = ""
    
    try {
        val recipesJson = window.localStorage.getItem("mykitchen_recipes")
        if (recipesJson != null) {
            val recipes = JSON.parse(recipesJson) as Array<dynamic>
            if (recipes.isNotEmpty()) {
                recipes.forEach { recipeData ->
                    val recipe = Recipe(
                        title = recipeData.title as String,
                        content = recipeData.content as String,
                        timestamp = recipeData.timestamp as Long,
                        id = recipeData.id as Long?
                    )
                    addRecipeToUI(recipe)
                }
            } else {
                showEmptyState()
            }
        } else {
            showEmptyState()
        }
    } catch (e: dynamic) {
        console.warn("Failed to load recipes: ${e.message}")
        showEmptyState()
    }
}

fun showEmptyState() {
    val recipesList = document.getElementById("recipes-list")
    recipesList?.append {
        div(classes = "empty-state") {
            p { +"No recipes yet. Add your first recipe!" }
        }
    }
}

fun addRecipeToUI(recipe: Recipe) {
    val recipesList = document.getElementById("recipes-list")
    
    recipesList?.append {
        div(classes = "recipe-item") {
            div(classes = "recipe-content") {
                h3 { +recipe.title }
                p(classes = "recipe-preview") {
                    +recipe.content.take(150).let { 
                        if (it.length >= 150) "$it..." else it 
                    }
                }
                p(classes = "recipe-timestamp") {
                    +"Created: ${kotlinx.datetime.Instant.fromEpochMilliseconds(recipe.timestamp)}"
                }
            }
            div(classes = "recipe-actions") {
                button(classes = "btn btn-sm btn-secondary") {
                    +"Edit"
                    onClickFunction = { editRecipe(recipe) }
                }
                button(classes = "btn btn-sm btn-danger") {
                    +"Delete"
                    onClickFunction = { deleteRecipe(recipe) }
                }
            }
        }
    }
}

fun saveRecipes(recipes: List<Recipe>) {
    try {
        val recipesJson = JSON.stringify(recipes)
        window.localStorage.setItem("mykitchen_recipes", recipesJson)
    } catch (e: dynamic) {
        console.warn("Failed to save recipes: ${e.message}")
    }
}

fun getRecipes(): List<Recipe> {
    return try {
        val recipesJson = window.localStorage.getItem("mykitchen_recipes")
        if (recipesJson != null) {
            val recipes = JSON.parse(recipesJson) as Array<dynamic>
            recipes.map { recipeData ->
                Recipe(
                    title = recipeData.title as String,
                    content = recipeData.content as String,
                    timestamp = recipeData.timestamp as Long,
                    id = recipeData.id as Long?
                )
            }
        } else {
            emptyList()
        }
    } catch (e: dynamic) {
        console.warn("Failed to load recipes: ${e.message}")
        emptyList()
    }
}

fun showLoginDialog() {
    document.getElementById("login-modal")?.setAttribute("style", "display: block;")
}

fun hideLoginDialog() {
    document.getElementById("login-modal")?.setAttribute("style", "display: none;")
}

fun showAddRecipeDialog() {
    document.getElementById("recipe-modal-title")?.textContent = "Add Recipe"
    document.getElementById("recipe-title-input")?.let { (it as HTMLInputElement).value = "" }
    document.getElementById("recipe-content-input")?.let { (it as HTMLTextAreaElement).value = "" }
    document.getElementById("recipe-modal")?.setAttribute("style", "display: block;")
}

fun editRecipe(recipe: Recipe) {
    document.getElementById("recipe-modal-title")?.textContent = "Edit Recipe"
    document.getElementById("recipe-title-input")?.let { (it as HTMLInputElement).value = recipe.title }
    document.getElementById("recipe-content-input")?.let { (it as HTMLTextAreaElement).value = recipe.content }
    document.getElementById("recipe-modal")?.setAttribute("style", "display: block;")
    
    // Store the recipe being edited
    window.asDynamic().editingRecipe = recipe
}

fun hideRecipeDialog() {
    document.getElementById("recipe-modal")?.setAttribute("style", "display: none;")
    window.asDynamic().editingRecipe = null
}

fun handleLogin() {
    val server = (document.getElementById("server-input") as HTMLInputElement).value
    val email = (document.getElementById("email-input") as HTMLInputElement).value
    val password = (document.getElementById("password-input") as HTMLInputElement).value
    
    if (server.isBlank() || email.isBlank() || password.isBlank()) {
        window.alert("Please fill in all fields")
        return
    }
    
    // For now, just show a success message
    window.alert("Login functionality will be implemented with backend integration")
    hideLoginDialog()
}

fun logout() {
    window.alert("Logout functionality will be implemented with backend integration")
}

fun handleRecipeSubmit() {
    val title = (document.getElementById("recipe-title-input") as HTMLInputElement).value
    val content = (document.getElementById("recipe-content-input") as HTMLTextAreaElement).value
    
    if (title.isBlank() || content.isBlank()) {
        window.alert("Please fill in all fields")
        return
    }
    
    val editingRecipe = window.asDynamic().editingRecipe as Recipe?
    
    if (editingRecipe != null) {
        // Update existing recipe
        val recipes = getRecipes().toMutableList()
        val index = recipes.indexOfFirst { it.id == editingRecipe.id }
        if (index >= 0) {
            recipes[index] = editingRecipe.copy(title = title, content = content)
            saveRecipes(recipes)
            loadRecipes()
        }
    } else {
        // Add new recipe
        val newRecipe = Recipe(
            title = title,
            content = content,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() + (0..999).random()
        )
        
        val recipes = getRecipes().toMutableList()
        recipes.add(newRecipe)
        saveRecipes(recipes)
        loadRecipes()
    }
    
    hideRecipeDialog()
}

fun deleteRecipe(recipe: Recipe) {
    if (window.confirm("Are you sure you want to delete '${recipe.title}'?")) {
        val recipes = getRecipes().toMutableList()
        recipes.removeAll { it.id == recipe.id }
        saveRecipes(recipes)
        loadRecipes()
    }
}