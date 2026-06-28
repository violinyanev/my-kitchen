package com.ultraviolince.mykitchen.data.remote.dto

import com.ultraviolince.mykitchen.domain.model.Recipe

fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    content = content,
    timestamp = updatedAt,
    synced = true,
    deleted = false,
)

fun Recipe.toDto(): RecipeDto = RecipeDto(
    id = id,
    title = title,
    content = content,
    createdAt = timestamp,
    updatedAt = timestamp,
)
