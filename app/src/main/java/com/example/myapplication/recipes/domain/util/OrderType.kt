package com.example.myapplication.recipes.domain.util

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
