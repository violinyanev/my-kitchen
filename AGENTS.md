# My Kitchen — KMP Recipe Management Application

My Kitchen is a free and open source recipe management application built as a **Kotlin Multiplatform (KMP)** project. It consists of:

- **`androidApp/`** — Android application (Jetpack Compose)
- **`desktopApp/`** — JVM desktop application (Compose for Desktop)
- **`webApp/`** — Web application (Compose for Web / WasmJs)
- **`iosApp/`** — iOS application (SwiftUI + KMP framework)
- **`server/`** — Ktor backend server (self-hostable REST API)
- **`shared/domain/`** — Pure Kotlin domain layer (use cases, models, repository interfaces)
- **`shared/data/`** — KMP data layer (Room database, Ktor API client, Koin DI)
- **`shared/ui/`** — Shared Compose Multiplatform UI (screens, ViewModels, navigation)

---

## 🚨 CRITICAL: Validate Before Committing

All GitHub Actions checks must pass. Run the validation command before committing:

```bash
./gradlew :androidApp:assembleDebug :shared:domain:desktopTest :shared:data:desktopTest :server:test detekt
```

**After every code change**, run Detekt on the affected module before committing:

```bash
# Server changes
./gradlew :server:detekt

# Shared module changes
./gradlew :shared:domain:detekt :shared:data:detekt :shared:ui:detekt

# Android changes
./gradlew :androidApp:detekt
```

Detekt enforces `ArgumentListWrapping` — any `respond()`, `call()`, or function call with
arguments that push the line past the threshold must wrap each argument onto its own line:

```kotlin
// Wrong — triggers ArgumentListWrapping
call.respond(HttpStatusCode.BadRequest, ErrorDto("some long message here"))

// Correct
call.respond(
    HttpStatusCode.BadRequest,
    ErrorDto("some long message here"),
)
```

---

## Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 2.3.20 | All modules |
| AGP | 9.2.1 | Android + KMP library plugin |
| Compose Multiplatform | 1.11.1 | Shared UI |
| Ktor | 3.5.0 | HTTP client + server |
| Koin | 4.2.1 | Dependency injection |
| Room KMP | 2.8.4 | Local database (Android/Desktop/iOS) |
| KSP | 2.3.8 | Annotation processing for Room |
| Exposed | 0.61.0 | Server-side ORM |
| Detekt | 1.23.8 | Code quality |
| Kotlin Coroutines | 1.11.0 | Async |
| kotlinx.serialization | 1.11.0 | JSON |
| Kover | 0.9.8 | Code coverage |
| Java toolchain | 17 | Build + runtime |

---

## Project Structure

```
my-kitchen/
├── androidApp/                  # Android entry point
│   └── src/main/kotlin/…/
│       ├── MainActivity.kt      # Compose entry, Koin started in MyKitchenApp
│       └── MyKitchenApp.kt      # Application class, calls startKoin
├── desktopApp/                  # Desktop JVM entry point
│   └── src/main/kotlin/…/
│       └── Main.kt              # startKoin + application {}
├── webApp/                      # WasmJs entry point
│   └── src/wasmJsMain/kotlin/…/
│       └── Main.kt              # startKoin + CanvasBasedWindow
├── iosApp/                      # Swift Xcode project
│   └── iosApp/
│       └── App.swift            # SwiftUI entry — call IosKoinHelperKt.doInitKoin()
├── server/                      # Ktor server
│   └── src/main/kotlin/…/server/
│       ├── Application.kt       # main() entry point
│       ├── config/AppConfig.kt  # Environment variable config
│       ├── data/tables/         # Exposed ORM tables
│       ├── plugins/             # Database, Auth, CORS, Serialization, StatusPages
│       └── routes/              # HealthRoutes, AuthRoutes, RecipeRoutes
├── shared/
│   ├── domain/                  # Domain layer (pure Kotlin)
│   │   └── src/commonMain/…/
│   │       ├── model/           # Recipe, User, AuthState, RecipeOrder
│   │       ├── repository/      # RecipeRepository interface
│   │       ├── usecase/         # 8 use cases
│   │       └── di/DomainModule.kt
│   ├── data/                    # Data layer (KMP)
│   │   └── src/
│   │       ├── commonMain/…/
│   │       │   ├── local/       # RecipeEntity, RecipeDao, RecipeDatabase, mappers
│   │       │   ├── remote/      # RecipeApiClient, DTOs, mappers
│   │       │   ├── store/       # CredentialsStore, InMemoryCredentialsStore
│   │       │   ├── repository/  # RecipeRepositoryImpl
│   │       │   └── di/          # DataModule, PlatformDataModule (expect)
│   │       ├── androidMain/     # Room DatabaseFactory + actual PlatformDataModule
│   │       ├── desktopMain/     # Room DatabaseFactory + actual PlatformDataModule
│   │       ├── iosMain/         # Room DatabaseFactory + actual PlatformDataModule
│   │       └── wasmJsMain/      # InMemoryRecipeDao + actual PlatformDataModule
│   └── ui/                      # Shared Compose UI (KMP)
│       └── src/
│           ├── commonMain/…/ui/
│           │   ├── App.kt               # NavHost entry point
│           │   ├── theme/AppTheme.kt
│           │   ├── navigation/Route.kt  # Type-safe routes
│           │   ├── screens/recipelist/  # RecipeListScreen + ViewModel
│           │   ├── screens/addedit/     # AddEditScreen + ViewModel
│           │   ├── screens/login/       # LoginScreen + ViewModel
│           │   └── di/UiModule.kt
│           └── iosMain/…/ui/
│               ├── MainViewController.kt
│               └── IosKoinHelper.kt     # initKoin() called from Swift
├── build-logic/                         # Convention plugins
│   └── src/main/kotlin/
│       ├── kmp-library.gradle.kts       # Base KMP lib (all targets + Koin + coroutines)
│       ├── kmp-compose.gradle.kts       # Extends kmp-library + Compose + nav + lifecycle
│       └── server-app.gradle.kts        # Ktor server app convention
├── gradle/
│   ├── libs.versions.toml               # Version catalog
│   └── detekt.yml                       # Detekt config (formatting enabled)
└── .github/workflows/
    ├── test.yaml                        # Main CI: build, test, detekt, ios, web, backend
    └── …                               # Other workflows (security, backend-image, etc.)
```

---

## Build Commands

### Prerequisites
- Java 17+ (OpenJDK Temurin 17 recommended)
- Python 3.8+ (for backend)
- Android SDK (for Android builds)
- Xcode (for iOS builds, macOS only)

### Android
```bash
# Debug APK
./gradlew :androidApp:assembleDebug
# → app/build/outputs/apk/ (no — androidApp/build/outputs/apk/debug/app-debug.apk)

# Release APK
./gradlew :androidApp:assembleRelease
```

### Desktop
```bash
# Compile check
./gradlew :desktopApp:compileKotlin

# Run locally
./gradlew :desktopApp:run

# Package native distribution
./gradlew :desktopApp:createDistributable
```

### Web (WasmJs)
```bash
# Development server
./gradlew :webApp:wasmJsBrowserRun --continuous

# Production build
./gradlew :webApp:wasmJsBrowserDistribution
# → webApp/build/dist/wasmJs/productionExecutable/
```

### iOS (macOS only)
```bash
# Build KMP framework
./gradlew :shared:ui:linkDebugFrameworkIosArm64 \
          :shared:ui:linkDebugFrameworkIosX64 \
          :shared:ui:linkDebugFrameworkIosSimulatorArm64
```
Then open `iosApp/iosApp.xcodeproj` in Xcode and call `IosKoinHelperKt.doInitKoin()` from `App.swift`.

### Server (Ktor)
```bash
# Development (direct)
./gradlew :server:run

# With custom config (env vars)
RECIPES_DB_URL=jdbc:postgresql://localhost/mykitchen \
RECIPES_SECRET_KEY=your-secret \
./gradlew :server:run

# Build fat JAR
./gradlew :server:shadowJar

# Docker (from repo root)
cd backend && docker-compose up
```

### Backend (Python legacy — kept for migration)
```bash
# Start backend server
./backend/scripts/dev.sh          # Flask on localhost:5000
curl http://localhost:5000/health  # Should return: OK

# Run backend tests
cd backend/image && python3 -m unittest discover
```

---

## Testing

### Unit Tests
```bash
# Domain layer (28 tests)
./gradlew :shared:domain:desktopTest

# Data layer (12 tests — API client + repository)
./gradlew :shared:data:desktopTest

# Server (7 integration tests — H2 in-memory)
./gradlew :server:test

# All at once
./gradlew :shared:domain:desktopTest :shared:data:desktopTest :server:test
```

### Code Quality
```bash
# Detekt (all modules)
./gradlew detekt

# Detekt with auto-correct (formatting)
./gradlew detekt --auto-correct
```

### Test Timing

| Command | Typical Time | Notes |
|---------|-------------|-------|
| `:androidApp:assembleDebug` | 7+ min cold / <10s warm | Never cancel |
| `:shared:domain:desktopTest` | ~5s | |
| `:shared:data:desktopTest` | ~15s | |
| `:server:test` | ~15s | H2 in-memory |
| `detekt` | ~10s warm | |

---

## Architecture

### Domain Layer (`shared/domain`)
- Pure Kotlin, no Android/platform imports
- **Models**: `Recipe`, `User`, `AuthState`, `RecipeOrder`
- **Repository interface**: `RecipeRepository` (8 methods)
- **Use Cases**: `GetRecipesUseCase`, `GetRecipeUseCase`, `AddRecipeUseCase(title, content)`, `DeleteRecipeUseCase`, `SyncRecipesUseCase`, `LoginUseCase`, `LogoutUseCase`, `GetAuthStateUseCase`
- **DI**: `domainModule` (Koin) — all use cases as `factory`

### Data Layer (`shared/data`)
- `RecipeRepositoryImpl` — Room + Ktor API + CredentialsStore
- `DataModule` — `HttpClient` (platform-default engine), `RecipeApiClient`, `CredentialsStore`, `RecipeRepository`
- `platformDataModule` (expect/actual) — platform-specific Room database + `RecipeDao` binding
  - Android: `getDatabaseBuilder(get<Context>())` (Koin Android context)
  - Desktop: `getDatabaseBuilder()` → `~/.mykitchen/recipe.db`
  - iOS: `getDatabaseBuilder()` → NSDocumentDirectory
  - WasmJs: `InMemoryRecipeDao` (no Room on WasmJs)
- HTTP engine auto-detected by Ktor per platform (CIO for JVM/iOS, Js for WasmJs)

### UI Layer (`shared/ui`)
- Compose Multiplatform — works on Android, Desktop, Web, iOS
- Navigation: type-safe `Route.RecipeList`, `Route.EditRecipe(id?)`, `Route.Login`
- `RecipeListScreen` / `RecipeListViewModel` — sort, delete, sync, logout
- `AddEditScreen` / `AddEditViewModel` — create/edit recipe
- `LoginScreen` / `LoginViewModel` — server URL + email + password login
- `UiModule` — Koin `viewModelOf` + `parametersOf` for AddEditViewModel

### Server (`server`)
- Ktor + Netty
- JWT authentication (HMAC256)
- Exposed ORM with PostgreSQL (prod) / H2 (tests)
- Endpoints: `GET /health`, `GET /version`, `POST /users/login`, `GET/POST/PUT/DELETE /recipes`
- Config via env vars: `RECIPES_DB_URL`, `RECIPES_DB_USER`, `RECIPES_DB_PASSWORD`, `RECIPES_SECRET_KEY`, `RECIPES_JWT_ISSUER`, `RECIPES_JWT_AUDIENCE`

### Koin DI Startup

Each platform starts Koin with 4 modules:
```kotlin
startKoin {
    // Android only:
    androidContext(this@MyKitchenApp)
    modules(platformDataModule, dataModule, domainModule, uiModule)
}
```

---

## Code Conventions

### Kotlin / Detekt
- Max line length: 180 chars
- Max function length: 90 lines (exclude `@Composable`)
- Max class size: 600 lines
- Max method parameters: 8 (exclude `@Composable`)
- File must end with newline
- `formatting` rule set active with `autoCorrect: true`

### Commits
Follow conventional commits:
```
feat(ui): add recipe search
fix(server): handle empty title validation
test(data): add InMemoryRecipeDao coverage
chore(deps): bump Ktor to 3.5.1
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `ci`, `build`, `perf`, `revert`

### Adding a New Feature
1. Add model/interface to `shared/domain` if needed
2. Add data layer impl to `shared/data`
3. Add UI in `shared/ui` (ViewModel + Screen)
4. Wire Koin bindings in the appropriate module
5. Run tests: `./gradlew :shared:domain:desktopTest :shared:data:desktopTest`
6. Run detekt: `./gradlew detekt --auto-correct`
7. Build Android: `./gradlew :androidApp:assembleDebug`

---

## Common Issues

### Room KMP on WasmJs
Room is not available on WasmJs. The `wasmJsMain` has `InMemoryRecipeDao` — data is not persisted across page reloads. This is intentional for the web platform.

### `HttpClient {}` without engine
`DataModule` uses `HttpClient { }` (no explicit engine). Ktor auto-detects:
- JVM (Android/Desktop): CIO (available via `shared:data`'s platform deps)
- iOS: CIO
- WasmJs: Js

Do NOT manually inject an `HttpClientEngineFactory` — the auto-detection handles it.

### Android Context in Room
`platformDataModule` (androidMain) uses `get<Context>()` for the Room database. This works because `startKoin { androidContext(app) }` registers the application context as `Context`. Do NOT call it before `startKoin`.

### iOS Koin Setup
Call `IosKoinHelperKt.doInitKoin()` from your Swift entry point (before any Kotlin code runs):
```swift
@main
struct MyKitchenApp: App {
    init() {
        IosKoinHelperKt.doInitKoin()
    }
    var body: some Scene { ... }
}
```

### Build Warnings: "Unresolved platforms: [iosX64]"
These warnings appear for compose dependencies that don't resolve for all iOS targets. They are **non-fatal** — the build succeeds. They come from KMP dependency resolution checking before actual compilation.

---

## GitHub Actions CI

The `.github/workflows/test.yaml` runs on PRs/pushes to `main`:
1. **build** (ubuntu): Android APK + Desktop compile + unit tests + Detekt
2. **web** (ubuntu): WasmJs browser distribution
3. **ios** (macos): iOS KMP frameworks + Xcode build
4. **backend** (ubuntu): Python backend unit tests + health check
5. **copilot-retrigger-test** (ubuntu, PRs only): script dry-run

All jobs must pass before merging to `main`.

---

## Branch Strategy

- `main` — stable, production-ready
- `app-rewrite` — KMP rewrite (current)
- Feature branches from `app-rewrite` during development


