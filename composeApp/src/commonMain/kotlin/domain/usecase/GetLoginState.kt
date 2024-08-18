package domain.usecase

import domain.repository.LoginState
import domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

//TODO kmp @Single
class GetLoginState(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<LoginState> {
        return repository.getLoginState()
    }
}
