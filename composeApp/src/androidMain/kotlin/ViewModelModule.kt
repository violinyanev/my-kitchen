import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

actual val viewModelModule =
    module {
        viewModelOf(::LoginViewModel)
        viewModelOf(::RecipeViewModel)
        viewModelOf(::AddEditRecipeViewModel)
    }
