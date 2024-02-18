package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.LoginState
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class FakeBackend {
    // TODO use real test server
    companion object {
        val server = "https://ultraviolince.com:8019"
        val testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InRlc3R1c2VyIn0.hgvrPuJ1j0PlnnsvYD2mHiFpDycfMgvPYd6ilI3wX78"
        val testUserName = "testuser"
        val testUser = "test@user.com"
        val testPassword = "TestPassword"
    }
}
