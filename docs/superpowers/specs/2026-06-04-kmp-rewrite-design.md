# My Kitchen — KMP Rewrite Design Spec

**Date**: 2026-06-04
**Approach**: Clean Slate Rewrite
**Goal**: Modern Kotlin Multiplatform recipe management app supporting Android, iOS, Desktop, Web + Kotlin backend. Agent-ready repository.

---

## 1. Overview

Rewrite My Kitchen as a full Kotlin Multiplatform project using Compose Multiplatform for shared UI, Ktor Server for the backend, and modern KMP conventions. The app retains all current functionality (recipe CRUD, auth, sync, offline-first) while achieving full feature parity across all platforms.

---

## 2. Target Platforms

| Platform | Entry Point | UI Framework | Distribution |
|----------|-------------|--------------|--------------|
| Android | Activity | Compose Multiplatform | APK/AAB |
| iOS | SwiftUI host | Compose Multiplatform | Xcode/IPA |
| Desktop | JVM main() | Compose Multiplatform | JAR/native package |
| Web | WasmJs | Compose Multiplatform | Static site |
| Server | Ktor Server | N/A | Docker container / JAR |

---

## 3. Module Structure

```
my-kitchen/
├── gradle/
│   ├── libs.versions.toml          # Version catalog
│   └── wrapper/
├── build.gradle.kts                 # Root build (convention plugins)
├── settings.gradle.kts              # Module includes
│
├── shared/
│   ├── domain/                      # :shared:domain
│   │   └── src/commonMain/          # Models, use cases, repository interfaces
│   ├── data/                        # :shared:data
│   │   └── src/
│   │       ├── commonMain/          # Ktor client, Room DAOs, DataStore
│   │       ├── androidMain/         # Android Room driver, CIO engine
│   │       ├── iosMain/            # iOS Room driver, Darwin engine
│   │       ├── desktopMain/        # JVM Room driver, CIO engine
│   │       └── wasmJsMain/         # JS engine, IndexedDB fallback
│   └── ui/                          # :shared:ui
│       └── src/
│           ├── commonMain/          # Compose screens, navigation, theme
│           ├── androidMain/         # Android-specific UI (permissions, etc.)
│           └── desktopMain/         # Desktop window adaptations
│
├── androidApp/                      # :androidApp (thin shell)
│   └── src/main/
├── iosApp/                          # :iosApp (Xcode project)
│   └── iosApp/
├── desktopApp/                      # :desktopApp (JVM entry)
│   └── src/main/
├── webApp/                          # :webApp (WasmJs entry)
│   └── src/wasmJsMain/
│
├── server/                          # :server (Ktor backend)
│   └── src/
│       ├── main/                    # Routes, auth, DB, config
│       └── test/                    # Server integration tests
│
├── scripts/                         # Dev scripts, CI helpers
├── docs/                            # Documentation
└── .github/workflows/               # CI/CD pipelines
```

---

## 4. Shared Domain Layer (`:shared:domain`)

Pure Kotlin module with zero platform dependencies.

### Models
```kotlin
@Serializable
data class Recipe(
    val id: String,              // UUID
    val title: String,
    val content: String,
    val timestamp: Instant,
    val synced: Boolean = false,
    val deleted: Boolean = false
)

@Serializable
data class User(
    val email: String,
    val token: String?
)

sealed class RecipeOrder {
    data class Title(val ascending: Boolean) : RecipeOrder()
    data class Date(val ascending: Boolean) : RecipeOrder()
}
```

### Repository Interface
```kotlin
interface RecipeRepository {
    fun getRecipes(order: RecipeOrder): Flow<List<Recipe>>
    suspend fun getRecipeById(id: String): Recipe?
    suspend fun insertRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipe: Recipe)
    suspend fun syncRecipes(): Result<Unit>
}
```

### Use Cases
- `GetRecipesUseCase` — fetch recipes with ordering
- `GetRecipeUseCase` — fetch single recipe by ID
- `AddRecipeUseCase` — validate and insert recipe
- `DeleteRecipeUseCase` — soft-delete + queue for sync
- `SyncRecipesUseCase` — push/pull with backend
- `LoginUseCase` — authenticate and store token
- `LogoutUseCase` — clear credentials

---

## 5. Shared Data Layer (`:shared:data`)

### Local Database (Room KMP)
Room 2.8+ supports KMP (Android, iOS, Desktop/JVM). Shared DAOs and entities in `commonMain`, platform-specific database builders in `*Main` source sets. For WasmJs, use an in-memory cache with localStorage persistence (Room does not yet target WasmJs).

```kotlin
// commonMain
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val synced: Boolean,
    val deleted: Boolean
)

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE deleted = 0 ORDER BY title ASC")
    fun getRecipesByTitle(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getRecipesByDate(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity)

    @Query("UPDATE recipes SET deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)
}
```

### Network Client (Ktor)
```kotlin
// commonMain
class RecipeApiClient(private val httpClient: HttpClient) {
    suspend fun login(email: String, password: String): Result<TokenResponse>
    suspend fun getRecipes(token: String): Result<List<RecipeDto>>
    suspend fun createRecipe(token: String, recipe: RecipeDto): Result<RecipeDto>
    suspend fun deleteRecipe(token: String, id: String): Result<Unit>
}
```

Platform-specific engines:
- Android/Desktop/iOS: CIO
- WasmJs: Js engine

### Repository Implementation
```kotlin
class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val api: RecipeApiClient,
    private val credentials: CredentialsStore
) : RecipeRepository {
    // Offline-first: read from local DB, sync in background
}
```

### Credentials Store
`DataStore` (KMP) for token and server URL persistence. Falls back to in-memory for web.

---

## 6. Shared UI Layer (`:shared:ui`)

### Compose Multiplatform Screens
All UI is shared across platforms using Compose Multiplatform:

1. **RecipeListScreen** — list with sort/order, swipe-to-delete, FAB
2. **AddEditRecipeScreen** — title + content fields, save/update
3. **LoginScreen** — server URL + email + password
4. **SettingsScreen** (new) — server config, sync status

### Navigation
Use `androidx.navigation:navigation-compose` (KMP-compatible since 2.8+) with type-safe routes:

```kotlin
@Serializable sealed class Route {
    @Serializable data object RecipeList : Route()
    @Serializable data class EditRecipe(val id: String?) : Route()
    @Serializable data object Login : Route()
    @Serializable data object Settings : Route()
}
```

### ViewModel
Use KMP-compatible ViewModel (from `lifecycle-viewmodel-compose`):

```kotlin
class RecipeListViewModel(
    private val getRecipes: GetRecipesUseCase,
    private val deleteRecipe: DeleteRecipeUseCase
) : ViewModel() {
    // StateFlow-based state management
}
```

### Theme
Material 3 with dynamic color on Android, static theme on other platforms.

---

## 7. Server (`:server`)

### Technology Stack
- **Framework**: Ktor Server (Netty engine)
- **ORM**: Exposed (JetBrains)
- **Database**: PostgreSQL (production), H2 (testing)
- **Auth**: JWT (ktor-server-auth-jwt)
- **Serialization**: kotlinx.serialization
- **Config**: HOCON (application.conf)
- **Containerization**: Docker + docker-compose

### API Endpoints (backward-compatible with current Flask API)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /health | No | Health check |
| GET | /version | No | API version |
| POST | /users/login | No | Authenticate, return JWT |
| GET | /users | Yes | List users |
| GET | /user | Yes | Current user info |
| GET | /recipes | Yes | User's recipes |
| POST | /recipes | Yes | Create recipe |
| PUT | /recipes/{id} | Yes | Update recipe |
| DELETE | /recipes/{id} | Yes | Delete recipe |

### Database Schema
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE recipes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### Exposed Table Definitions
```kotlin
object Users : UUIDTable() {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object Recipes : UUIDTable() {
    val userId = reference("user_id", Users)
    val title = varchar("title", 500)
    val content = text("content")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
```

### Server Module Structure
```
server/src/main/kotlin/com/ultraviolince/mykitchen/
├── Application.kt           # Ktor application entry point
├── plugins/
│   ├── Routing.kt           # Route registration
│   ├── Serialization.kt     # Content negotiation
│   ├── Authentication.kt    # JWT config
│   └── Database.kt          # Exposed + HikariCP setup
├── routes/
│   ├── HealthRoutes.kt
│   ├── AuthRoutes.kt
│   └── RecipeRoutes.kt
├── data/
│   ├── tables/              # Exposed table objects
│   └── repositories/        # DB access layer
└── config/
    └── AppConfig.kt         # Environment-based config
```

---

## 8. Platform Entry Points

### Android (`:androidApp`)
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }  // App() from :shared:ui
    }
}
```

### iOS (`:iosApp`)
```swift
@main
struct MyKitchenApp: SwiftUI.App {
    var body: some Scene {
        WindowGroup {
            ComposeView()  // UIKit interop with Compose
        }
    }
}
```

### Desktop (`:desktopApp`)
```kotlin
fun main() = application {
    Window(
        title = "My Kitchen",
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
```

### Web (`:webApp`)
```kotlin
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(title = "My Kitchen") {
        App()
    }
}
```

---

## 9. Dependency Injection (Koin KMP)

```kotlin
// Shared DI modules
val domainModule = module {
    factory { GetRecipesUseCase(get()) }
    factory { AddRecipeUseCase(get()) }
    factory { DeleteRecipeUseCase(get()) }
    factory { LoginUseCase(get()) }
}

val dataModule = module {
    single { RecipeApiClient(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get(), get(), get()) }
}

// Platform-specific
val androidModule = module {
    single { createRoomDatabase(get()) }
    single { CIO.create() }  // Ktor engine
}
```

---

## 10. Testing Strategy

### Unit Tests (`:shared:domain`, `:shared:data`)
- **Framework**: kotlin.test + kotlinx-coroutines-test + Turbine
- **Scope**: Use cases, repository logic, model transformations
- **Mocking**: Use fakes (interface-based) rather than MockK for KMP compatibility
- **Run**: `./gradlew :shared:domain:allTests :shared:data:allTests`

### UI Tests (`:shared:ui`)
- **Framework**: Compose UI Testing (multiplatform)
- **Scope**: Screen state transitions, user interactions
- **Run**: `./gradlew :shared:ui:desktopTest` (fastest for CI)

### Server Tests (`:server`)
- **Framework**: Ktor test framework + kotlin.test
- **Database**: H2 in-memory for fast integration tests
- **Scope**: Route handlers, auth flow, CRUD operations
- **Run**: `./gradlew :server:test`

### Screenshot Tests
- **Framework**: Roborazzi (Android-specific, for visual regression)
- **Scope**: Key screens with various states
- **Run**: `./gradlew :androidApp:verifyRoborazziDebug`

### Integration Tests
- **Scope**: Client ↔ Server communication
- **Setup**: Start server with H2, test client operations
- **Run**: `./gradlew :shared:data:integrationTest`

### Coverage
- Kover for Kotlin coverage across all modules
- Minimum 80% on domain/data, 10% overall

---

## 11. CI/CD Pipeline

### GitHub Actions Workflows

**`test.yaml`** (on every PR):
1. Build health check
2. Server tests (`:server:test`)
3. Shared module tests (`:shared:domain:allTests`, `:shared:data:allTests`)
4. Android build + unit tests
5. Desktop UI tests
6. Screenshot verification
7. Detekt lint
8. Coverage report

**`integration.yaml`** (on PR merge to main):
1. Start server (Docker)
2. Run instrumented tests on Android emulator (API 28, 34, 35)
3. Run integration tests

**`release.yaml`** (on tag):
1. Build all platform artifacts
2. Publish Docker image for server
3. Build Android APK/AAB
4. Build Desktop packages (macOS, Linux, Windows)
5. Deploy web app

---

## 12. Agent-Ready Repository

### AGENTS.md Structure
The repository AGENTS.md will provide complete instructions for AI agents:

```markdown
# Quick Start
- Prerequisites (Java 17+, Docker for backend)
- Single command to build everything
- Single command to run all tests

# Module Map
- What each module does
- Where to find things
- How modules depend on each other

# Development Commands
- Build commands per module
- Test commands per module
- Run commands (start server, launch apps)

# Testing
- How to run each test type
- How to add new tests
- Coverage requirements

# Architecture Decisions
- Why KMP? Why Ktor Server? Why Exposed?
- Pattern: offline-first sync
- Pattern: shared ViewModel

# Troubleshooting
- Common build issues
- Platform-specific gotchas
- CI failures and fixes
```

### Key Agent Commands
```bash
# Build everything
./gradlew build

# Run all tests
./gradlew allTests :server:test

# Start server locally
./gradlew :server:run

# Build Android APK
./gradlew :androidApp:assembleDebug

# Run desktop app
./gradlew :desktopApp:run

# Run web app (dev server)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Lint
./gradlew detekt

# Coverage
./gradlew koverXmlReport

# Full validation
./scripts/validate-pr.sh
```

### Convention Plugins
Shared build logic via `build-logic/` convention plugins:
- `kmp-library-convention` — common KMP library setup
- `kmp-compose-convention` — Compose Multiplatform config
- `server-convention` — Ktor server setup
- `android-app-convention` — Android app configuration

---

## 13. Key Dependencies (Version Catalog)

| Category | Library | Version |
|----------|---------|---------|
| Language | Kotlin | 2.3.20 |
| Build | AGP | 9.2.1 |
| UI | Compose Multiplatform | 1.10.6 |
| UI | Material 3 | (from Compose BOM) |
| Navigation | Navigation Compose | 2.9.8 |
| Network | Ktor (client + server) | 3.5.0 |
| DI | Koin | 4.2.1 |
| Database (client) | Room KMP | 2.8.4 |
| Database (server) | Exposed | 0.61.0 |
| Database (server) | PostgreSQL driver | 42.7.x |
| Database (server) | HikariCP | 6.x |
| Serialization | kotlinx.serialization | 1.11.0 |
| DateTime | kotlinx.datetime | 0.8.0 |
| Coroutines | kotlinx.coroutines | 1.11.0 |
| Auth | Ktor JWT | 3.5.0 |
| Testing | kotlin.test | (bundled) |
| Testing | Turbine | 1.2.1 |
| Testing | Ktor test | 3.5.0 |
| Lint | Detekt | 1.23.8 |
| Coverage | Kover | 0.9.8 |

---

## 14. Migration Plan (High-Level Phases)

### Phase 1: Project Skeleton
- Set up new Gradle multi-module structure
- Configure convention plugins
- Set up version catalog
- Verify all targets compile (empty modules)

### Phase 2: Shared Domain
- Port models from existing shared module
- Port use cases from existing Android app
- Write unit tests

### Phase 3: Shared Data
- Implement Room KMP database
- Port Ktor client from existing shared module
- Implement repository
- Write unit tests

### Phase 4: Server
- Implement Ktor server with Exposed
- Port API from Python Flask (same endpoints)
- Write server tests
- Docker setup

### Phase 5: Shared UI
- Port Compose screens from Android app to shared
- Implement navigation
- Implement ViewModels with Koin

### Phase 6: Platform Entry Points
- Android thin shell
- Desktop entry point
- Web entry point
- iOS entry point (SwiftUI host)

### Phase 7: Testing & CI
- Set up all test types
- Configure GitHub Actions
- Screenshot baselines
- Integration tests

### Phase 8: Agent Readiness
- Write comprehensive AGENTS.md
- Validation scripts
- Development setup scripts
- Troubleshooting documentation

---

## 15. What Gets Removed

- `backend/` (Python Flask) — replaced by `:server`
- `app/` (Android-only app) — replaced by `:androidApp` + `:shared:ui`
- `iosApp/` (SwiftUI stub) — replaced by new `:iosApp` with Compose
- Current `shared/` — replaced by new `:shared:domain`, `:shared:data`, `:shared:ui`
- `commitlint.config.js`, `package.json` — replaced by Gradle-based commit validation

---

## 16. Non-Goals (Explicit Exclusions)

- No user registration (admin creates users, like current system)
- No recipe images/media (text-only, like current system)
- No recipe sharing between users
- No recipe categories/tags (can be added later)
- No real-time sync (pull-based, like current system)
- No native iOS UI (Compose Multiplatform for all)

---

## 17. Success Criteria

1. All four client platforms (Android, iOS, Desktop, Web) can create, list, edit, delete recipes
2. All four client platforms can authenticate with the Kotlin backend
3. Offline-first: apps work without backend, sync when available
4. Server passes all integration tests with PostgreSQL
5. An AI agent can clone the repo, read AGENTS.md, and successfully build/test/modify the project
6. CI pipeline validates all platforms on every PR
7. Code coverage ≥80% on domain/data layers
