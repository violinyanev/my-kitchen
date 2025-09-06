package com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity

import androidx.annotation.StringRes

class LoginException(@param:StringRes val errorMsg: Int) : Exception()
