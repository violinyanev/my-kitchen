package com.ultraviolince.mykitchen.data.remote.dto

import com.ultraviolince.mykitchen.data.local.RecipeEntity

fun RecipeDto.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    content = content,
    timestamp = updatedAt,
    synced = true,
    deleted = false,
)

fun RecipeEntity.toDto(): RecipeDto = RecipeDto(
    id = id,
    title = title,
    content = content,
    createdAt = timestamp,
    updatedAt = timestamp,
)
