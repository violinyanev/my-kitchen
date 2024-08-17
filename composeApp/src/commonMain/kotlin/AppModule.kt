
import com.ultraviolince.mykitchen.recipes.domain.usecase.AddRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.DeleteRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetLoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipes
import com.ultraviolince.mykitchen.recipes.domain.usecase.Login
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::AddRecipe).bind<AddRecipe>()
    singleOf(::DeleteRecipe).bind<DeleteRecipe>()
    singleOf(::GetLoginState).bind<GetLoginState>()
    singleOf(::GetRecipe).bind<GetRecipe>()
    singleOf(::GetRecipes).bind<GetRecipes>()
    singleOf(::Login).bind<Login>()
    singleOf(::Recipes).bind<Recipes>()
}
