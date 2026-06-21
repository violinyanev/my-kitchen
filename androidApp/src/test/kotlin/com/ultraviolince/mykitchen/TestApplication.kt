package com.ultraviolince.mykitchen

class TestApplication : MyKitchenApp() {
    override fun onCreate() {
        // CMP's DefaultAndroidResourceReader requires AndroidContextHolder.context to be set
        // before stringResource() is called during composition. Robolectric doesn't invoke
        // ContentProviders before the first composable render, so we set it directly via
        // reflection before super.onCreate() (which starts Koin and may trigger composition).
        initCmpContext()
        super.onCreate()
    }

    private fun initCmpContext() {
        try {
            val clazz = Class.forName("org.jetbrains.compose.resources.AndroidContextHolder")
            val instance = clazz.getField("INSTANCE").get(null)
            val setter = clazz.getMethod("setContext", android.content.Context::class.java)
            setter.invoke(instance, applicationContext)
        } catch (e: Exception) {
            android.util.Log.w("TestApplication", "CMP context init failed: ${e.message}")
        }
    }
}
