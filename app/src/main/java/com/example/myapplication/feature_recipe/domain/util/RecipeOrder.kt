package com.example.myapplication.feature_recipe.domain.util

sealed class RecipeOrder (val orderType: OrderType) {
    class Title(orderType: OrderType): RecipeOrder(orderType)
    class Date(orderType: OrderType): RecipeOrder(orderType)
}