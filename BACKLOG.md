# Tech Debt Backlog

Generated: 2026-07-11. Covers shared code, server, androidApp, desktopApp, webApp, iosApp.

---

## HIGH — Fix First

### H1 · Unsafe `!!` operators in RecipeRepositoryImpl
**File:** `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/repository/RecipeRepositoryImpl.kt:49,51,66`
`exceptionOrNull()!!` and `getOrNull()!!` crash if their assumed preconditions are violated.
Replace with `.getOrElse { handleError() }` / early return patterns.

### H2 · No unit tests for ViewModels
**Files:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/addedit/AddEditViewModel.kt`, `LoginViewModel.kt`, `RecipeListViewModel.kt`
~275 lines of business logic with zero test coverage. The `shared:ui` module is explicitly excluded from Kover.
Add 20–30 tests covering state transitions, error paths, and side effects.

### H3 · No rate limiting on auth endpoints
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/routes/AuthRoutes.kt:28,70`
Login and register routes accept unlimited attempts — brute force is trivially easy.
Add a Ktor rate-limiting plugin or per-IP/per-email throttle middleware.

### H4 · Silent exception suppression in EnrichmentService
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/data/services/EnrichmentService.kt:142,186`
`catch (_: Exception)` swallows all errors and returns null with no logging.
At minimum log the exception; ideally propagate typed errors so callers can distinguish failures.

### H5 · CORS allows any origin when ENV var is absent
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/plugins/Cors.kt:21-22`
If `origins` ENV var is unset in production, `anyHost()` is silently applied.
Fail-safe: require the ENV var in production; throw on startup if missing and not dev mode.

### H6 · No error feedback when deleting a recipe fails
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/recipelist/RecipeListViewModel.kt:91-93`
`delete(id)` launches a coroutine with no error capture; failures are silently ignored.
Wrap in try/catch and emit an error state the UI can surface.

### H7 · Background AutoBeautifyWorker can die silently
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/Application.kt:30`
`launch { AutoBeautifyWorker(...).run() }` at root scope — an uncaught exception kills the worker with no restart.
Use a supervised scope with a restart loop and exponential backoff.

---

## MEDIUM — Address Soon

### M1 · Hardcoded `localhost:5000` default in LoginState
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/login/LoginViewModel.kt:20`
`val serverUrl: String = "http://localhost:5000"` leaks dev config into production builds.
Drive from BuildConfig or a per-platform default that is overridable.

### M2 · Hardcoded Ollama URL and model name
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/config/AppConfig.kt:37-38`
`ollamaBaseUrl` and `ollamaModel` are compile-time strings.
Make them ENV-backed with documented defaults.

### M3 · `allTags` / `visibleRecipes` recomputed on every access (O(n²))
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/recipelist/RecipeListViewModel.kt:35-36`
`flatten().distinct().sorted()` called inside a derived StateFlow with no memoisation.
Cache in a `val` computed once per upstream emission; move heavy derivation to the data layer if possible.

### M4 · Multiple `collectAsState()` calls cause independent recompositions
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/recipelist/RecipeListScreen.kt:67-68`
Two independent `collectAsState()` calls mean state changes to either flow independently recompose the screen.
Combine into a single `StateFlow` holding a screen-level UiState.

### M5 · RecipeListScreen exceeds 200-line guideline
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/recipelist/RecipeListScreen.kt` (247 lines)
`OrderToggle`, `TagFilterRow`, and `RecipeItem` are composables defined inline that should live in their own file.
Extract to `recipelist/components/` to reduce recomposition scope and improve reuse.

### M6 · Duplicated coroutine error-handling boilerplate across ViewModels
**Files:** `AddEditViewModel.kt`, `LoginViewModel.kt`, `RecipeListViewModel.kt`
Each ViewModel repeats the same `viewModelScope.launch { … }` + error-state-update block.
Extract to a shared `safelaunch { }` extension on `ViewModel` in a shared module.

### M7 · Two `RecipeEntity.kt` files — incomplete KMP abstraction
**Files:** `shared/data/src/commonMain/` + `nonWasmMain/`
Platform-specific Room entity definitions suggest the KMP abstraction boundary is blurry.
Consolidate via `expect`/`actual` or lift shared structure to a common sealed class.

### M8 · Missing content-length validation for recipe text
**Files:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/addedit/AddEditScreen.kt`, `AddRecipeUseCase.kt:9`
`OutlinedTextField` accepts unbounded input; `AddRecipeUseCase` only checks `isBlank()`.
Add a max-length check (e.g., 10 000 chars) in the use case and reflect the count in the UI.

### M9 · No URL format validation before login attempt
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/login/LoginScreen.kt`
Server URL field accepts any string; invalid URLs produce a confusing network error.
Validate URL format in `LoginUseCase` or ViewModel and emit a typed error state.

### M10 · Email validation only server-side
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/routes/AuthRoutes.kt:21-24`
Client `LoginUseCase` only checks `isBlank()`; email regex lives on the server.
Add lightweight email format check client-side for immediate UI feedback.

### M11 · Keystore signing silently omitted if `keystore.properties` is absent
**File:** `androidApp/build.gradle.kts:85-99`
Release builds compile without signing when the file is missing; no error is thrown.
Fail the build explicitly when `isCI` is true and keystore config is absent.

### M12 · `PREVIEW_*` BuildConfig fields empty in local builds
**File:** `androidApp/build.gradle.kts:131-133`
CI injects credentials; local builds silently get empty strings.
Document this in CLAUDE.md and add a Gradle warning when building `preview` variant locally.

### M13 · No KDoc on domain use cases or repository interfaces
**Files:** `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/usecase/*.kt`, `domain/repository/*.kt`
~10 use case classes and 2 repository interfaces have no documentation on contracts, exceptions, or async behaviour.
Add minimal KDoc covering: what it does, expected errors, and threading contract.

### M14 · Server routes have no endpoint documentation
**Files:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/routes/*.kt`
No KDoc, no OpenAPI schema, no status-code documentation.
Consider generating OpenAPI docs via `ktor-openapi` or add inline KDoc as a minimum.

### M15 · No contract tests for platform CredentialsStore implementations
**Files:** `shared/data/src/androidMain/`, `desktopMain/`, `iosMain/`, `wasmJsMain/` — `PlatformDataModule.kt`
Four separate stores (SharedPreferences, keystore, Keychain, localStorage) share no test doubles.
Write a common contract test suite and run it on each platform in CI.

### M16 · Missing contentDescription on AsyncImage
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/addedit/BeautifiedRecipeView.kt:56`
Image credit string used inconsistently as content description.
Set an explicit `contentDescription` string resource.

### M17 · Login form fields lack semantic accessibility labels
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/login/LoginScreen.kt`
`testTag` is set but `semantics { contentDescription }` is not, so TalkBack reads placeholder text only.
Add explicit label semantics to all input fields.

### M18 · JWT secret not validated for minimum entropy
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/config/AppConfig.kt:22-23`
`JWT_SECRET` ENV var is accepted regardless of length; a 1-character secret is allowed.
Enforce minimum length (≥ 32 chars) on startup and fail fast if not met.

---

## LOW — Fix When Convenient

### L1 · Client data layer has no logging
**Files:** `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/`
API client and repositories are completely silent; remote failures are opaque in production.
Add structured logging (e.g., `co.touchlab:kermit`) at DEBUG level for network calls and errors.

### L2 · ViewModels have no logging
**Files:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/**ViewModel.kt`
Failed login, sync errors etc. are emitted to Snackbar but not persisted to logs.
Log key state transitions at INFO and errors at ERROR level.

### L3 · Inconsistent use case naming (noun vs. verb)
**Files:** `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/usecase/*.kt`
`LoginUseCase` / `LogoutUseCase` (noun) vs `AddRecipeUseCase` / `DeleteRecipeUseCase` (verb).
Pick one convention (recommend verb-noun: `AddRecipeUseCase`) and rename across the board.

### L4 · Inconsistent DTO serialisation style
**File:** `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/remote/dto/RecipeDto.kt:11-12`
`created_at` / `updated_at` use `@SerialName`; `id`, `title`, `content` do not.
Apply `@SerialName` consistently or configure the serializer to use snake_case globally.

### L5 · Localhost URL repeated ~12 times in tests
**Files:** `shared/data/src/commonTest/kotlin/com/ultraviolince/mykitchen/data/`
Hardcoded `"http://localhost:5000"` scattered across multiple test files.
Extract to a `TestConstants` object in `commonTest`.

### L6 · No explicit tab order in RecipeListScreen
**File:** `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/screens/recipelist/RecipeListScreen.kt`
Compose uses layout order by default; no explicit `Modifier.focusOrder` or semantic traversal.
Audit and define expected keyboard/switch-access navigation path.

### L7 · Desktop and Web build configs are stubs
**Files:** `desktopApp/build.gradle.kts`, `webApp/build.gradle.kts`
Both are minimal with no run configuration, resource management, or platform-specific settings.
Align with androidApp config structure and document any intentional omissions.

### L8 · Unsplash API key could leak in error traces
**File:** `server/src/main/kotlin/com/ultraviolince/mykitchen/server/config/AppConfig.kt:39`
`unsplashAccessKey` is read from ENV correctly but no sanitisation prevents it appearing in logs.
Add a Logback masking pattern or a custom `toString()` that redacts the key.

---

## Done / Not an Issue

| Item | Status |
|------|--------|
| TODO/FIXME/HACK/XXX comments | None found — clean |
| Deprecated API usage | None found — clean |
| Outdated dependencies | All current (Kotlin 2.3.20, Ktor 3.5.1, Compose 1.11.1, Koin 4.2.2, Room 3.0.0) |
| Renovate configured | Yes — `renovate.json` present |
| Server-side logging | Present via SLF4J/Logback — good |
| `key = { it.id }` in LazyColumn | Already in place — good |
| Input validation at server boundary | UUID parsing wrapped in exception handler — acceptable |
