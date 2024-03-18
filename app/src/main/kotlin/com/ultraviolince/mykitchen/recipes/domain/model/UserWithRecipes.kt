package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

data class UserWithRecipes (
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userCreatorId"
    )
    val recipes: List<Recipe>
)
