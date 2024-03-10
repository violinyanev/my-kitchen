package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User(
    val name: String,
    val email: String,
    val serverUri: String,
    val isDefault: Boolean,
    @PrimaryKey val id: Long? = null
) {
    override fun toString() = "User[$id] $name (email $email) (srv: $serverUri)"
}
