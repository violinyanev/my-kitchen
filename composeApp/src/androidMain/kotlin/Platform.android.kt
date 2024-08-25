import android.os.Build

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Platform {
    actual val name: String = "Android ${Build.VERSION.SDK_INT}"

    init {
        Log.i("Created platform $name")
    }
}
