package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.annotation.StringRes

class LoginException(@StringRes val errorMsg: Int) : Exception()
