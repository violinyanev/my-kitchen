package com.ultraviolince.mykitchen.recipes.domain.util

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
