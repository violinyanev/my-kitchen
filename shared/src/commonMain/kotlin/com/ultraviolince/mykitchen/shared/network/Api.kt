package com.ultraviolince.mykitchen.shared.network

import io.ktor.resources.Resource

@Resource("/recipes")
class Recipes {
    @Resource("/{id}")
    class ById(val parent: Recipes = Recipes(), val id: Long)
}

@Resource("/users/login")
class Login

