package com.ultraviolince.mykitchen

import KoinInitializer
import android.app.Application

class MyKitchenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KoinInitializer(applicationContext).init()
    }
}
