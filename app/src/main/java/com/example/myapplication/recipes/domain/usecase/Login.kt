package com.example.myapplication.recipes.domain.usecase

import android.util.Log
import com.example.myapplication.R
import com.example.myapplication.recipes.domain.model.LoginException
import com.example.myapplication.recipes.domain.repository.RecipeRepository

class Login(private val repository: RecipeRepository) {
    suspend operator fun invoke(server: String, username: String, password: String) {
        Log.e("RECIPES", "serv $server un $username p $password ")

        if (server.isBlank()) {
            throw LoginException(R.string.missing_server)
        }

        if (username.isBlank()) {
            throw LoginException(R.string.missing_username)
        }

        repository.login(server, username, password)
    }
}
