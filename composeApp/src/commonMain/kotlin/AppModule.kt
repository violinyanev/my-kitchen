
import data.datasource.backend.RecipeServiceWrapper
import data.datasource.localdb.RecipeDao
import data.datasource.localdb.RecipeDaoImpl
import data.repository.RecipePreferences
import data.repository.RecipeRepositoryImpl
import domain.repository.RecipeRepository
import domain.usecase.AddRecipe
import domain.usecase.DeleteRecipe
import domain.usecase.GetLoginState
import domain.usecase.GetRecipe
import domain.usecase.GetRecipes
import domain.usecase.Login
import domain.usecase.Recipes
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::RecipeDaoImpl).bind<RecipeDao>()
    singleOf(::RecipeServiceWrapper).bind<RecipeServiceWrapper>()
    singleOf(::RecipePreferences).bind<RecipePreferences>()
    singleOf(::RecipeRepositoryImpl).bind<RecipeRepository>()
    singleOf(::AddRecipe).bind<AddRecipe>()
    singleOf(::DeleteRecipe).bind<DeleteRecipe>()
    singleOf(::GetLoginState).bind<GetLoginState>()
    singleOf(::GetRecipe).bind<GetRecipe>()
    singleOf(::GetRecipes).bind<GetRecipes>()
    singleOf(::Login).bind<Login>()
    singleOf(::Recipes).bind<Recipes>()
}
