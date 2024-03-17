package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User(
    val name: String,
    val email: String,
    val serverUri: String,
    val isDefault: Boolean,
    val token: String? = null,
    @PrimaryKey val id: Long? = null
) {
    override fun toString() = "User[$id] $name (email $email) (srv: $serverUri)"
}

class InvalidUserException(@StringRes val errorString: Int) : Exception()
