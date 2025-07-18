package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.annotation.StringRes

class LoginException(@param:StringRes val errorMsg: Int) : Exception()
