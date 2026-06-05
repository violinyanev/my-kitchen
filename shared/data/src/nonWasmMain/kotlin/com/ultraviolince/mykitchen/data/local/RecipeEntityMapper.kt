package com.ultraviolince.mykitchen.data.local

import com.ultraviolince.mykitchen.domain.model.Recipe

fun RecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    synced = synced,
    deleted = deleted,
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    synced = synced,
    deleted = deleted,
)
