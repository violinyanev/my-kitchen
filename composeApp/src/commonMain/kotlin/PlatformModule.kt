
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import data.repository.RecipePreferences
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule = module {
    singleOf(::Platform)
    singleOf(::RecipePreferences)
}
