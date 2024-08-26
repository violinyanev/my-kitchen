
import data.repository.RecipePreferences
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule = module {
    singleOf(::Platform)
    singleOf(::RecipePreferences)
}
