package main

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.js.onClickFunction
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.ul
import kotlinx.html.id

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
            div {
                h1 { +"My Kitchen - Web App" }
                h3 { +"Powered by Kotlin Multiplatform" }
                
                p { +"This web app uses the same shared business logic as the Android and iOS apps!" }
                
                button {
                    +"Create Sample Recipe"
                    onClickFunction = {
                        createSampleRecipe()
                    }
                }
                
                div {
                    id = "recipes-container"
                    ul {
                        id = "recipes-list"
                    }
                }
            }
        }
    }
}

fun createSampleRecipe() {
    val recipe = Recipe(
        title = "Web Sample Recipe",
        content = "This recipe was created from the web app using the shared Kotlin module!",
        timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
        id = (1..1000).random().toLong()
    )
    
    addRecipeToUI(recipe)
    console.log("Created recipe: ${recipe.title}")
}

fun addRecipeToUI(recipe: Recipe) {
    val recipesList = document.getElementById("recipes-list")
    
    recipesList?.append {
        li {
            div {
                h3 { +recipe.title }
                p { +recipe.content }
                p { +"Created: ${kotlinx.datetime.Instant.fromEpochMilliseconds(recipe.timestamp)}" }
            }
        }
    }
}
