# Phase 2: Shared Domain Layer — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the complete shared domain layer — all models, repository interfaces, and use cases — covered by unit tests running on JVM (desktop).

**Architecture:** Pure Kotlin module (`shared/domain`) with zero platform dependencies. All business logic lives here. Use fakes (not mocks) in tests for KMP compatibility. TDD: write failing test → implement → green → commit.

**Tech Stack:** kotlin.test, kotlinx-coroutines-test, Turbine, kotlin.time.Instant, Koin (module definitions only)

---

## File Structure

Files created or modified in this phase:

```
shared/domain/src/
├── commonMain/kotlin/com/ultraviolince/mykitchen/domain/
│   ├── model/
│   │   ├── Recipe.kt                  # (exists — update Instant import)
│   │   └── User.kt                    # NEW: User data class + AuthState
│   ├── repository/
│   │   └── RecipeRepository.kt        # NEW: repository interface
│   ├── usecase/
│   │   ├── GetRecipesUseCase.kt       # NEW
│   │   ├── GetRecipeUseCase.kt        # NEW
│   │   ├── AddRecipeUseCase.kt        # NEW
│   │   ├── DeleteRecipeUseCase.kt     # NEW
│   │   ├── SyncRecipesUseCase.kt      # NEW
│   │   ├── LoginUseCase.kt            # NEW
│   │   └── LogoutUseCase.kt           # NEW
│   └── di/
│       └── DomainModule.kt            # NEW: Koin module
└── commonTest/kotlin/com/ultraviolince/mykitchen/domain/
    ├── fake/
    │   └── FakeRecipeRepository.kt    # NEW: in-memory fake
    ├── usecase/
    │   ├── GetRecipesUseCaseTest.kt   # NEW
    │   ├── GetRecipeUseCaseTest.kt    # NEW
    │   ├── AddRecipeUseCaseTest.kt    # NEW
    │   ├── DeleteRecipeUseCaseTest.kt # NEW
    │   ├── SyncRecipesUseCaseTest.kt  # NEW
    │   ├── LoginUseCaseTest.kt        # NEW
    │   └── LogoutUseCaseTest.kt       # NEW
    └── model/
        └── RecipeTest.kt              # NEW: model validation tests
```

---

## Background: Domain Concepts

The app is an **offline-first recipe manager** with optional backend sync:

- A **Recipe** has a UUID `id`, `title`, `content` (markdown), `timestamp`, a `synced` flag, and a `deleted` flag (soft delete).
- A **User** is authenticated against the backend. The domain holds their `email`, `serverUrl`, and optional JWT `token`.
- **RecipeOrder** controls sort: by title or by date, ascending or descending.
- The **RecipeRepository** interface is what use cases call — it is implemented in `:shared:data`.
- **Use cases** each do exactly one thing; they are thin orchestrators.

### Use Case Behaviors
- `GetRecipesUseCase(order)` — emits `Flow<List<Recipe>>` (non-deleted, ordered)
- `GetRecipeUseCase(id)` — returns `Recipe?`
- `AddRecipeUseCase(title, content)` — validates non-blank, generates UUID + timestamp, inserts
- `DeleteRecipeUseCase(id)` — soft-deletes (sets `deleted = true`)
- `SyncRecipesUseCase()` — delegates to `repository.syncRecipes()`, returns `Result<Unit>`
- `LoginUseCase(email, password, serverUrl)` — validates inputs, delegates to `repository.login()`
- `LogoutUseCase()` — delegates to `repository.logout()`

---

## Task 1 — Update `Recipe.kt` model

**Goal:** Ensure the existing Recipe model uses `kotlin.time.Instant` (not deprecated `kotlinx.datetime.Instant`), and add a factory method.

- [ ] Read `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/model/Recipe.kt`
- [ ] Verify import is `kotlin.time.Instant` (was fixed in Phase 1 — just confirm)
- [ ] Add a companion object factory: `Recipe.create(title, content)` that generates a UUID `id` and current `Clock.System.now()` timestamp, `synced = false`, `deleted = false`
- [ ] Write a failing test in `commonTest/.../model/RecipeTest.kt`:
  ```kotlin
  @Test fun `create sets non-blank id and current timestamp`() {
      val r = Recipe.create("Pasta", "Cook it")
      assertTrue(r.id.isNotBlank())
      assertEquals("Pasta", r.title)
      assertFalse(r.synced)
      assertFalse(r.deleted)
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect FAIL (factory doesn't exist yet)
- [ ] Implement `Recipe.create()` in `Recipe.kt` using `uuid4()` from `com.benasher44:uuid` — actually use a simple `kotlin.uuid.Uuid.random().toString()` (Kotlin 2.0+ stdlib)
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add Recipe.create() factory method`

> **Note:** `kotlin.uuid.Uuid` is in Kotlin stdlib 2.0+. Use `kotlin.uuid.Uuid.random().toString()` for id generation. For timestamp, use `kotlinx.datetime.Clock.System.now()` — wait, since we use `kotlin.time.Instant`, use `kotlin.time.Clock.System.now()` if available, otherwise `kotlinx.datetime.Clock.System.now().toKotlinInstant()`. Actually: `kotlin.time.Instant` IS `kotlinx.datetime.Instant` in latest kotlinx-datetime — just use `kotlinx.datetime.Clock.System.now()` which returns `kotlinx.datetime.Instant` — but our model stores `kotlin.time.Instant`. Simplest fix: store the timestamp as `Long` (epoch millis) in the factory, converting with `Clock.System.now().toEpochMilliseconds()` — but the model uses `Instant`. Use `kotlinx.datetime.Clock.System.now()` and call `.toKotlinInstant()` — OR just keep `kotlinx.datetime.Instant` in the model since Kotlin 2.x stdlib `kotlin.time.Instant` only exists in very recent compiler versions. **Practical choice**: change the model to use `Long` for timestamp (simpler, no dependency issues) and keep it serializable. Update the model and tests accordingly.

**Revised approach for timestamp:** Use `Long` (epoch milliseconds) in the model.

- [ ] Change `Recipe.timestamp: Instant` to `Recipe.timestamp: Long` in `Recipe.kt`
- [ ] Remove the `kotlinx-datetime` or `kotlin.time` Instant import from Recipe.kt
- [ ] Update `Recipe.create()` to use `System.currentTimeMillis()` (available in KMP commonMain via `kotlin.system` — actually use `kotlinx.datetime.Clock.System.now().toEpochMilliseconds()`)
- [ ] Add `kotlinx-datetime` import in `Recipe.kt`: `import kotlinx.datetime.Clock`
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): use Long for Recipe.timestamp, add Recipe.create()`

---

## Task 2 — Add `User.kt` model

**Goal:** Define `User` data class and `AuthState` sealed class.

- [ ] Create `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/model/User.kt`:
  ```kotlin
  package com.ultraviolince.mykitchen.domain.model

  import kotlinx.serialization.Serializable

  @Serializable
  data class User(
      val email: String,
      val serverUrl: String,
      val token: String? = null,
  )

  sealed class AuthState {
      data object LoggedOut : AuthState()
      data class LoggedIn(val user: User) : AuthState()
  }
  ```
- [ ] Write test in `commonTest/.../model/RecipeTest.kt` (same file is fine — or create `UserTest.kt`):
  ```kotlin
  @Test fun `AuthState LoggedIn holds user`() {
      val user = User("a@b.com", "http://localhost:5000", "tok")
      val state = AuthState.LoggedIn(user)
      assertEquals("a@b.com", (state as AuthState.LoggedIn).user.email)
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS (just a data class, trivial)
- [ ] Commit: `feat(domain): add User model and AuthState`

---

## Task 3 — Add `RecipeOrder.kt` and `RecipeRepository` interface

**Goal:** Define the ordering enum and repository contract that use cases depend on.

- [ ] Create `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/model/RecipeOrder.kt`:
  ```kotlin
  package com.ultraviolince.mykitchen.domain.model

  sealed class RecipeOrder(val ascending: Boolean) {
      class Title(ascending: Boolean = true) : RecipeOrder(ascending)
      class Date(ascending: Boolean = false) : RecipeOrder(ascending)
  }
  ```
- [ ] Create `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/repository/RecipeRepository.kt`:
  ```kotlin
  package com.ultraviolince.mykitchen.domain.repository

  import com.ultraviolince.mykitchen.domain.model.AuthState
  import com.ultraviolince.mykitchen.domain.model.Recipe
  import com.ultraviolince.mykitchen.domain.model.RecipeOrder
  import kotlinx.coroutines.flow.Flow

  interface RecipeRepository {
      fun getRecipes(order: RecipeOrder): Flow<List<Recipe>>
      suspend fun getRecipeById(id: String): Recipe?
      suspend fun insertRecipe(recipe: Recipe)
      suspend fun deleteRecipe(id: String)
      suspend fun syncRecipes(): Result<Unit>
      suspend fun login(email: String, password: String, serverUrl: String): Result<Unit>
      suspend fun logout()
      fun getAuthState(): Flow<AuthState>
  }
  ```
- [ ] Run `./gradlew :shared:domain:compileKotlinDesktop` — expect PASS (interfaces compile)
- [ ] Commit: `feat(domain): add RecipeOrder and RecipeRepository interface`

---

## Task 4 — Create `FakeRecipeRepository` for tests

**Goal:** Build an in-memory fake that all use case tests will use. Write it TDD: it should pass a basic contract test.

- [ ] Create `shared/domain/src/commonTest/kotlin/com/ultraviolince/mykitchen/domain/fake/FakeRecipeRepository.kt`:
  ```kotlin
  package com.ultraviolince.mykitchen.domain.fake

  import com.ultraviolince.mykitchen.domain.model.AuthState
  import com.ultraviolince.mykitchen.domain.model.Recipe
  import com.ultraviolince.mykitchen.domain.model.RecipeOrder
  import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
  import kotlinx.coroutines.flow.Flow
  import kotlinx.coroutines.flow.MutableStateFlow
  import kotlinx.coroutines.flow.map

  class FakeRecipeRepository : RecipeRepository {
      private val recipesFlow = MutableStateFlow<List<Recipe>>(emptyList())
      private val authFlow = MutableStateFlow<AuthState>(AuthState.LoggedOut)
      var loginResult: Result<Unit> = Result.success(Unit)
      var syncResult: Result<Unit> = Result.success(Unit)

      override fun getRecipes(order: RecipeOrder): Flow<List<Recipe>> =
          recipesFlow.map { recipes ->
              val active = recipes.filter { !it.deleted }
              when (order) {
                  is RecipeOrder.Title -> if (order.ascending) active.sortedBy { it.title }
                                         else active.sortedByDescending { it.title }
                  is RecipeOrder.Date  -> if (order.ascending) active.sortedBy { it.timestamp }
                                         else active.sortedByDescending { it.timestamp }
              }
          }

      override suspend fun getRecipeById(id: String): Recipe? =
          recipesFlow.value.find { it.id == id }

      override suspend fun insertRecipe(recipe: Recipe) {
          val current = recipesFlow.value.toMutableList()
          current.removeIf { it.id == recipe.id }
          current.add(recipe)
          recipesFlow.value = current
      }

      override suspend fun deleteRecipe(id: String) {
          recipesFlow.value = recipesFlow.value.map {
              if (it.id == id) it.copy(deleted = true) else it
          }
      }

      override suspend fun syncRecipes(): Result<Unit> = syncResult

      override suspend fun login(email: String, password: String, serverUrl: String): Result<Unit> {
          if (loginResult.isSuccess) {
              authFlow.value = AuthState.LoggedIn(
                  com.ultraviolince.mykitchen.domain.model.User(email, serverUrl, "fake-token")
              )
          }
          return loginResult
      }

      override suspend fun logout() {
          authFlow.value = AuthState.LoggedOut
      }

      override fun getAuthState(): Flow<AuthState> = authFlow
  }
  ```
- [ ] Write a contract test in `commonTest/.../fake/FakeRecipeRepositoryTest.kt`:
  ```kotlin
  @Test fun `insertRecipe and getRecipeById round-trips`() = runTest {
      val repo = FakeRecipeRepository()
      val r = Recipe.create("Pasta", "Boil water")
      repo.insertRecipe(r)
      assertEquals(r, repo.getRecipeById(r.id))
  }
  @Test fun `deleteRecipe soft-deletes`() = runTest {
      val repo = FakeRecipeRepository()
      val r = Recipe.create("Pasta", "Boil water")
      repo.insertRecipe(r)
      repo.deleteRecipe(r.id)
      assertNull(repo.getRecipeById(r.id)?.let { if (it.deleted) null else it })
      // getRecipes flow should not include deleted
      val items = repo.getRecipes(RecipeOrder.Title()).first()
      assertTrue(items.isEmpty())
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `test(domain): add FakeRecipeRepository with contract tests`

---

## Task 5 — `GetRecipesUseCase` + tests

**Goal:** Use case that returns ordered non-deleted recipes.

- [ ] Write failing test `GetRecipesUseCaseTest.kt`:
  ```kotlin
  @Test fun `emits recipes sorted by title ascending`() = runTest {
      val repo = FakeRecipeRepository()
      val useCase = GetRecipesUseCase(repo)
      repo.insertRecipe(Recipe.create("Zucchini", "..."))
      repo.insertRecipe(Recipe.create("Apple pie", "..."))
      val result = useCase(RecipeOrder.Title(ascending = true)).first()
      assertEquals("Apple pie", result[0].title)
      assertEquals("Zucchini", result[1].title)
  }
  @Test fun `does not emit deleted recipes`() = runTest {
      val repo = FakeRecipeRepository()
      val useCase = GetRecipesUseCase(repo)
      val r = Recipe.create("Ghost", "")
      repo.insertRecipe(r)
      repo.deleteRecipe(r.id)
      val result = useCase(RecipeOrder.Title()).first()
      assertTrue(result.isEmpty())
  }
  ```
- [ ] Run test — expect FAIL (class doesn't exist)
- [ ] Create `shared/domain/src/commonMain/.../usecase/GetRecipesUseCase.kt`:
  ```kotlin
  package com.ultraviolince.mykitchen.domain.usecase

  import com.ultraviolince.mykitchen.domain.model.Recipe
  import com.ultraviolince.mykitchen.domain.model.RecipeOrder
  import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
  import kotlinx.coroutines.flow.Flow

  class GetRecipesUseCase(private val repository: RecipeRepository) {
      operator fun invoke(order: RecipeOrder = RecipeOrder.Date()): Flow<List<Recipe>> =
          repository.getRecipes(order)
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add GetRecipesUseCase`

---

## Task 6 — `GetRecipeUseCase` + tests

- [ ] Write failing test:
  ```kotlin
  @Test fun `returns recipe by id`() = runTest {
      val repo = FakeRecipeRepository()
      val r = Recipe.create("Pasta", "Boil water")
      repo.insertRecipe(r)
      val result = GetRecipeUseCase(repo)(r.id)
      assertEquals(r, result)
  }
  @Test fun `returns null for missing id`() = runTest {
      val result = GetRecipeUseCase(FakeRecipeRepository())("no-such-id")
      assertNull(result)
  }
  ```
- [ ] Run test — expect FAIL
- [ ] Create `GetRecipeUseCase.kt`:
  ```kotlin
  class GetRecipeUseCase(private val repository: RecipeRepository) {
      suspend operator fun invoke(id: String): Recipe? = repository.getRecipeById(id)
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add GetRecipeUseCase`

---

## Task 7 — `AddRecipeUseCase` + tests

- [ ] Write failing tests:
  ```kotlin
  @Test fun `inserts recipe with generated id`() = runTest {
      val repo = FakeRecipeRepository()
      val useCase = AddRecipeUseCase(repo)
      useCase("Pasta", "Boil water")
      val items = repo.getRecipes(RecipeOrder.Title()).first()
      assertEquals(1, items.size)
      assertEquals("Pasta", items[0].title)
      assertTrue(items[0].id.isNotBlank())
  }
  @Test fun `returns failure for blank title`() = runTest {
      val result = AddRecipeUseCase(FakeRecipeRepository())("  ", "content")
      assertTrue(result.isFailure)
  }
  @Test fun `returns failure for blank content`() = runTest {
      val result = AddRecipeUseCase(FakeRecipeRepository())("title", "")
      assertTrue(result.isFailure)
  }
  ```
- [ ] Run test — expect FAIL
- [ ] Create `AddRecipeUseCase.kt`:
  ```kotlin
  class AddRecipeUseCase(private val repository: RecipeRepository) {
      suspend operator fun invoke(title: String, content: String): Result<Unit> {
          if (title.isBlank()) return Result.failure(IllegalArgumentException("Title cannot be blank"))
          if (content.isBlank()) return Result.failure(IllegalArgumentException("Content cannot be blank"))
          repository.insertRecipe(Recipe.create(title.trim(), content.trim()))
          return Result.success(Unit)
      }
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add AddRecipeUseCase`

---

## Task 8 — `DeleteRecipeUseCase` + tests

- [ ] Write failing tests:
  ```kotlin
  @Test fun `soft-deletes recipe`() = runTest {
      val repo = FakeRecipeRepository()
      val r = Recipe.create("Pasta", "content")
      repo.insertRecipe(r)
      DeleteRecipeUseCase(repo)(r.id)
      val items = repo.getRecipes(RecipeOrder.Title()).first()
      assertTrue(items.isEmpty())
  }
  ```
- [ ] Run test — expect FAIL
- [ ] Create `DeleteRecipeUseCase.kt`:
  ```kotlin
  class DeleteRecipeUseCase(private val repository: RecipeRepository) {
      suspend operator fun invoke(id: String) = repository.deleteRecipe(id)
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add DeleteRecipeUseCase`

---

## Task 9 — `SyncRecipesUseCase` + tests

- [ ] Write failing tests:
  ```kotlin
  @Test fun `returns success when repo sync succeeds`() = runTest {
      val repo = FakeRecipeRepository()
      repo.syncResult = Result.success(Unit)
      val result = SyncRecipesUseCase(repo)()
      assertTrue(result.isSuccess)
  }
  @Test fun `returns failure when repo sync fails`() = runTest {
      val repo = FakeRecipeRepository()
      repo.syncResult = Result.failure(Exception("network error"))
      val result = SyncRecipesUseCase(repo)()
      assertTrue(result.isFailure)
  }
  ```
- [ ] Run test — expect FAIL
- [ ] Create `SyncRecipesUseCase.kt`:
  ```kotlin
  class SyncRecipesUseCase(private val repository: RecipeRepository) {
      suspend operator fun invoke(): Result<Unit> = repository.syncRecipes()
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add SyncRecipesUseCase`

---

## Task 10 — `LoginUseCase` + tests

- [ ] Write failing tests:
  ```kotlin
  @Test fun `successful login transitions to LoggedIn state`() = runTest {
      val repo = FakeRecipeRepository()
      LoginUseCase(repo)("user@test.com", "pass", "http://localhost:5000")
      val state = repo.getAuthState().first()
      assertTrue(state is AuthState.LoggedIn)
  }
  @Test fun `returns failure for blank email`() = runTest {
      val result = LoginUseCase(FakeRecipeRepository())("", "pass", "http://localhost:5000")
      assertTrue(result.isFailure)
  }
  @Test fun `returns failure for blank server url`() = runTest {
      val result = LoginUseCase(FakeRecipeRepository())("a@b.com", "pass", "")
      assertTrue(result.isFailure)
  }
  ```
- [ ] Run test — expect FAIL
- [ ] Create `LoginUseCase.kt`:
  ```kotlin
  class LoginUseCase(private val repository: RecipeRepository) {
      suspend operator fun invoke(email: String, password: String, serverUrl: String): Result<Unit> {
          if (email.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be blank"))
          if (serverUrl.isBlank()) return Result.failure(IllegalArgumentException("Server URL cannot be blank"))
          return repository.login(email, password, serverUrl)
      }
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add LoginUseCase`

---

## Task 11 — `LogoutUseCase` + tests

- [ ] Write failing test:
  ```kotlin
  @Test fun `logout transitions to LoggedOut state`() = runTest {
      val repo = FakeRecipeRepository()
      LoginUseCase(repo)("user@test.com", "pass", "http://localhost:5000")
      LogoutUseCase(repo)()
      val state = repo.getAuthState().first()
      assertEquals(AuthState.LoggedOut, state)
  }
  ```
- [ ] Run test — expect FAIL
- [ ] Create `LogoutUseCase.kt`:
  ```kotlin
  class LogoutUseCase(private val repository: RecipeRepository) {
      suspend operator fun invoke() = repository.logout()
  }
  ```
- [ ] Run `./gradlew :shared:domain:desktopTest` — expect PASS
- [ ] Commit: `feat(domain): add LogoutUseCase`

---

## Task 12 — Koin `DomainModule` + final build verification

**Goal:** Wire all use cases in a Koin module so they can be injected later. Verify all tests still pass and the full build is green.

- [ ] Create `shared/domain/src/commonMain/.../di/DomainModule.kt`:
  ```kotlin
  package com.ultraviolince.mykitchen.domain.di

  import com.ultraviolince.mykitchen.domain.usecase.*
  import org.koin.dsl.module

  val domainModule = module {
      factory { GetRecipesUseCase(get()) }
      factory { GetRecipeUseCase(get()) }
      factory { AddRecipeUseCase(get()) }
      factory { DeleteRecipeUseCase(get()) }
      factory { SyncRecipesUseCase(get()) }
      factory { LoginUseCase(get()) }
      factory { LogoutUseCase(get()) }
  }
  ```
- [ ] Run `./gradlew :shared:domain:compileKotlinDesktop` — expect PASS
- [ ] Run `./gradlew :shared:domain:desktopTest` — all tests green
- [ ] Run `./gradlew :androidApp:assembleDebug` — full Android build still passes
- [ ] Commit: `feat(domain): add Koin DomainModule`

---

## Acceptance Criteria

All of the following must be true before this phase is considered done:

- [ ] `./gradlew :shared:domain:desktopTest` passes (all use case and model tests green)
- [ ] `./gradlew :shared:domain:compileKotlinDesktop` passes
- [ ] `./gradlew :androidApp:assembleDebug` passes (no regressions)
- [ ] `./gradlew :server:compileKotlin` passes (no regressions)
- [ ] All use cases have at minimum: a happy-path test and at least one validation/edge-case test
- [ ] `FakeRecipeRepository` fully implements `RecipeRepository` interface
- [ ] No use of `MockK` or JVM-only test libraries (KMP compatibility)
- [ ] All new files end with a newline character

---

## Notes for Agents

- **Run one failing test at a time** — don't write all tests then all implementations.
- **Kotlin UUID**: Use `kotlin.uuid.Uuid.random().toString()` for ID generation (requires `@OptIn(ExperimentalUuidApi::class)` in Kotlin 2.0).
- **Timestamp**: Use `kotlinx.datetime.Clock.System.now().toEpochMilliseconds()` for `Long` timestamp in `Recipe.create()`.
- **`runTest`**: Import from `kotlinx.coroutines.test.runTest`.
- **`first()`**: Import from `kotlinx.coroutines.flow.first`.
- **Turbine**: For multi-emission flow tests use `flow.test { ... }` from the Turbine library — but for simple single-value checks, `.first()` is sufficient.
- **No `println` in production code** — use no logging in domain layer.
- **Verify compile before commit** — always run `compileKotlinDesktop` before committing.
