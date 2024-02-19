package com.example.myapplication.recipes.domain.model

import androidx.annotation.StringRes

class LoginException(@StringRes val errorMsg: Int) : Exception()
