import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Platform : KoinComponent {
    actual val name: String = "Android ${Build.VERSION.SDK_INT}"

    actual val preferencesPath = "/prefs.preferences_pb"

    val context: Context by inject()

    init {
        Log.i("Created platform $name")
    }

    actual fun createDataStore(): DataStore<Preferences> {
        val path = context.filesDir.resolve(preferencesPath).absolutePath

        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { path.toPath() }
        )
    }
}
