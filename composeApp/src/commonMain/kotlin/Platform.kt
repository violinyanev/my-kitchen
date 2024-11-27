import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Platform() {
    val name: String
    val preferencesPath: String
    fun createDataStore(): DataStore<Preferences>
}
