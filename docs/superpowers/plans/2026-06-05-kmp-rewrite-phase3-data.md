# Phase 3: Shared Data Layer — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the complete shared data layer — Room KMP database, Ktor API client, credentials store, and `RecipeRepositoryImpl` — all covered by unit tests running on JVM (desktop target).

**Architecture:** `:shared:data` module depends on `:shared:domain` for the repository interface. Room KMP provides local persistence (Android/Desktop/iOS). Ktor provides the API client. Tests use `FakeRecipeDao` + Ktor `MockEngine` to avoid platform-specific Room setup in unit tests.

**Tech Stack:** Room 2.8.4 + KSP 2.3.8 (KMP), Ktor Client 3.5.0 + MockEngine, kotlinx.serialization, Koin 4.2.1, kotlin.test, kotlinx-coroutines-test, Turbine

---

## What Is Already Done

- `shared/data/build.gradle.kts` — `kmp-library` convention, Ktor client deps configured
- `shared/data/src/commonMain/.../data/Placeholder.kt` — stub to be deleted
- `shared/domain` — full domain layer (models, interfaces, use cases) from Phase 2

---

## File Structure

```
shared/data/
├── build.gradle.kts                          # MODIFIED: add KSP, Room plugins + Room deps
├── schemas/                                  # CREATED (auto): Room schema exports
└── src/
    ├── commonMain/kotlin/com/ultraviolince/mykitchen/data/
    │   ├── local/
    │   │   ├── RecipeEntity.kt               # Room @Entity
    │   │   ├── RecipeDao.kt                  # Room @Dao interface
    │   │   └── RecipeDatabase.kt             # Room @Database abstract class
    │   ├── remote/
    │   │   ├── dto/
    │   │   │   ├── RecipeDto.kt              # Serializable DTOs for API
    │   │   │   └── AuthDto.kt                # Login request/response
    │   │   └── RecipeApiClient.kt            # Ktor HTTP client
    │   ├── store/
    │   │   ├── CredentialsStore.kt           # Interface: token + serverUrl storage
    │   │   └── InMemoryCredentialsStore.kt   # In-memory implementation (default)
    │   ├── repository/
    │   │   └── RecipeRepositoryImpl.kt       # Implements RecipeRepository
    │   └── di/
    │       └── DataModule.kt                 # Koin module
    ├── androidMain/kotlin/.../data/local/
    │   └── DatabaseFactory.kt               # Android Room.databaseBuilder
    ├── desktopMain/kotlin/.../data/local/
    │   └── DatabaseFactory.kt               # Desktop Room.databaseBuilder
    ├── iosMain/kotlin/.../data/local/
    │   └── DatabaseFactory.kt               # iOS Room.databaseBuilder (native)
    ├── wasmJsMain/kotlin/.../data/local/
    │   └── DatabaseFactory.kt               # Stub (Room not supported on WasmJs)
    └── commonTest/kotlin/com/ultraviolince/mykitchen/data/
        ├── fake/
        │   ├── FakeRecipeDao.kt              # In-memory DAO for testing
        │   └── FakeRecipeApiClient.kt        # Configurable fake API client
        ├── remote/
        │   └── RecipeApiClientTest.kt        # Ktor MockEngine tests
        └── repository/
            └── RecipeRepositoryImplTest.kt   # Repository tests with fakes
```

---

## Background: Data Layer Concepts

### Room KMP
Room entities and DAOs are defined in `commonMain`. KSP generates platform-specific implementations for Android, JVM (desktop), and iOS. WasmJs is not supported — the `DatabaseFactory.kt` for wasmJs is a stub that throws `UnsupportedOperationException` (WasmJs uses an in-memory fallback for now, wired in Phase 6).

The Room flow:
- `RecipeEntity` — `@Entity` data class persisted in SQLite
- `RecipeDao` — `@Dao` interface with `@Query`, `@Insert` etc.
- `RecipeDatabase` — `@Database(entities=[RecipeEntity::class])` abstract class
- Platform `DatabaseFactory.kt` — calls `Room.databaseBuilder(...)` with platform-specific file path

### Mapping
Domain `Recipe` (pure model) ↔ `RecipeEntity` (database row) ↔ `RecipeDto` (API JSON).
Mapping functions are extension functions defined on the entity/DTO.

### Credentials Store
Stores `token: String?` and `serverUrl: String`. The `InMemoryCredentialsStore` uses a `MutableStateFlow` — it works for all platforms and is the default. Platform-specific persistent stores (DataStore, NSUserDefaults, etc.) will be added in Phase 6.

### Repository Implementation (offline-first)
- **Read**: always from local DB
- **Sync**: pull from API → upsert local DB; push local unsync'd items → mark synced
- **Auth**: login sets credentials; logout clears them and wipes local DB
- **getAuthState**: derived from `CredentialsStore`

---

## Task 1 — Update `shared/data/build.gradle.kts`

**Goal:** Add KSP + Room plugins and Room runtime dependency, keeping existing Ktor deps.

- [ ] Read the current `shared/data/build.gradle.kts`
- [ ] Replace its contents with:

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kmp-library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    android {
        namespace = "com.ultraviolince.mykitchen.shared.data"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
            implementation(libs.room.runtime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}

// Room KSP — only for platforms Room supports (not wasmJs)
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
```

- [ ] Delete `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/Placeholder.kt`
- [ ] Run `./gradlew :shared:data:compileKotlinDesktop` — expect PASS (empty sources)
- [ ] Commit: `build(data): add Room KMP + KSP to shared:data module`

---

## Task 2 — Room entities and DAO

**Goal:** Define `RecipeEntity` and `RecipeDao` in `commonMain`. These are pure Room annotations — no implementation needed.

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/local/RecipeEntity.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val synced: Boolean,
    val deleted: Boolean,
)
```

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/local/RecipeDao.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE deleted = 0 ORDER BY title ASC")
    fun getRecipesByTitle(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE deleted = 0 ORDER BY timestamp DESC")
    fun getRecipesByDate(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE deleted = 0")
    suspend fun getAllActive(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE deleted = 1 AND synced = 0")
    suspend fun getUnsynced(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Query("UPDATE recipes SET deleted = 1, synced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE recipes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM recipes WHERE deleted = 1 AND synced = 1")
    suspend fun clearSyncedDeleted()
}
```

- [ ] Create mapping extension functions in `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/local/RecipeEntityMapper.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

import com.ultraviolince.mykitchen.domain.model.Recipe

fun RecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    synced = synced,
    deleted = deleted,
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    synced = synced,
    deleted = deleted,
)
```

- [ ] Run `./gradlew :shared:data:compileKotlinDesktop` — expect PASS
- [ ] Commit: `feat(data): add RecipeEntity, RecipeDao, and entity mapper`

---

## Task 3 — `RecipeDatabase` and platform `DatabaseFactory`

**Goal:** Define the Room database abstract class and provide platform-specific builders.

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/local/RecipeDatabase.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RecipeEntity::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}
```

- [ ] Create `shared/data/src/androidMain/kotlin/com/ultraviolince/mykitchen/data/local/DatabaseFactory.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<RecipeDatabase> {
    val dbFile = context.getDatabasePath("recipe.db")
    return Room.databaseBuilder<RecipeDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}
```

- [ ] Create `shared/data/src/desktopMain/kotlin/com/ultraviolince/mykitchen/data/local/DatabaseFactory.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<RecipeDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".mykitchen/recipe.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<RecipeDatabase>(
        name = dbFile.absolutePath,
    )
}
```

- [ ] Create `shared/data/src/iosMain/kotlin/com/ultraviolince/mykitchen/data/local/DatabaseFactory.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<RecipeDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbPath = requireNotNull(documentDirectory).path + "/recipe.db"
    return Room.databaseBuilder<RecipeDatabase>(name = dbPath)
}
```

- [ ] Create `shared/data/src/wasmJsMain/kotlin/com/ultraviolince/mykitchen/data/local/DatabaseFactory.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.local

// Room does not support WasmJs. Database access on web uses in-memory storage.
// This stub exists to satisfy compilation; it is never called at runtime.
fun getDatabaseBuilderStub(): Nothing =
    throw UnsupportedOperationException("Room database not supported on WasmJs")
```

- [ ] Run `./gradlew :shared:data:compileKotlinDesktop` — expect PASS
- [ ] Commit: `feat(data): add RecipeDatabase and platform-specific database factories`

> **Note on KSP:** The KSP compilation for Room happens automatically when `compileKotlinDesktop` runs. If KSP reports errors about missing `@Database` or Room imports, verify that `room-runtime` is in `commonMain.dependencies` in `build.gradle.kts`.

---

## Task 4 — Network DTOs

**Goal:** Define serializable data transfer objects for the API.

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/remote/dto/RecipeDto.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)
```

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/remote/dto/AuthDto.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)
```

- [ ] Create mapping functions in `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/remote/dto/RecipeDtoMapper.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.remote.dto

import com.ultraviolince.mykitchen.data.local.RecipeEntity

fun RecipeDto.toEntity(userId: String? = null): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    content = content,
    timestamp = updatedAt,
    synced = true,
    deleted = false,
)

fun RecipeEntity.toDto(): RecipeDto = RecipeDto(
    id = id,
    title = title,
    content = content,
    createdAt = timestamp,
    updatedAt = timestamp,
)
```

- [ ] Run `./gradlew :shared:data:compileKotlinDesktop` — expect PASS
- [ ] Commit: `feat(data): add RecipeDto, AuthDto, and DTO mappers`

---

## Task 5 — `RecipeApiClient`

**Goal:** Implement the Ktor HTTP client that calls the backend API.

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/remote/RecipeApiClient.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.remote

import com.ultraviolince.mykitchen.data.remote.dto.AuthDto
import com.ultraviolince.mykitchen.data.remote.dto.LoginRequest
import com.ultraviolince.mykitchen.data.remote.dto.LoginResponse
import com.ultraviolince.mykitchen.data.remote.dto.RecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RecipeApiClient(private val httpClient: HttpClient) {

    suspend fun login(serverUrl: String, email: String, password: String): Result<LoginResponse> =
        runCatching {
            httpClient.post("$serverUrl/users/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body<LoginResponse>()
        }

    suspend fun getRecipes(serverUrl: String, token: String): Result<List<RecipeDto>> =
        runCatching {
            httpClient.get("$serverUrl/recipes") {
                bearerAuth(token)
            }.body<List<RecipeDto>>()
        }

    suspend fun createRecipe(serverUrl: String, token: String, recipe: RecipeDto): Result<RecipeDto> =
        runCatching {
            httpClient.post("$serverUrl/recipes") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(recipe)
            }.body<RecipeDto>()
        }

    suspend fun updateRecipe(serverUrl: String, token: String, recipe: RecipeDto): Result<RecipeDto> =
        runCatching {
            httpClient.put("$serverUrl/recipes/${recipe.id}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(recipe)
            }.body<RecipeDto>()
        }

    suspend fun deleteRecipe(serverUrl: String, token: String, id: String): Result<Unit> =
        runCatching {
            httpClient.delete("$serverUrl/recipes/$id") {
                bearerAuth(token)
            }
            Unit
        }
}
```

- [ ] Write a failing test in `shared/data/src/commonTest/kotlin/com/ultraviolince/mykitchen/data/remote/RecipeApiClientTest.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.remote

import com.ultraviolince.mykitchen.data.remote.dto.RecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeApiClientTest {

    private fun buildClient(mockEngine: MockEngine): HttpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test
    fun `login returns token on success`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"token":"jwt-abc"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.login("http://localhost:5000", "user@test.com", "pass")
        assertTrue(result.isSuccess)
        assertEquals("jwt-abc", result.getOrNull()?.token)
    }

    @Test
    fun `login returns failure on 401`() = runTest {
        val engine = MockEngine { _ ->
            respond(content = """{"error":"unauthorized"}""", status = HttpStatusCode.Unauthorized)
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.login("http://localhost:5000", "bad@user.com", "wrong")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getRecipes returns list on success`() = runTest {
        val recipesJson = """[{"id":"1","title":"Pasta","content":"Boil","created_at":1000,"updated_at":1000}]"""
        val engine = MockEngine { _ ->
            respond(content = recipesJson, status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.getRecipes("http://localhost:5000", "tok")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Pasta", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `deleteRecipe returns success on 200`() = runTest {
        val engine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.OK)
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.deleteRecipe("http://localhost:5000", "tok", "recipe-id")
        assertTrue(result.isSuccess)
    }
}
```

- [ ] Run `./gradlew :shared:data:desktopTest` — **first run may fail** if `RecipeApiClient.kt` doesn't exist yet. If test is written first and fails with compile error, then add the implementation.
- [ ] After implementation, run again — expect PASS
- [ ] Commit: `feat(data): add RecipeApiClient with Ktor MockEngine tests`

---

## Task 6 — `CredentialsStore`

**Goal:** Define a credentials store interface and an in-memory implementation.

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/store/CredentialsStore.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.store

import kotlinx.coroutines.flow.Flow

interface CredentialsStore {
    fun observeToken(): Flow<String?>
    fun observeServerUrl(): Flow<String?>
    suspend fun getToken(): String?
    suspend fun getServerUrl(): String?
    suspend fun saveCredentials(token: String, serverUrl: String)
    suspend fun clearCredentials()
}
```

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/store/InMemoryCredentialsStore.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryCredentialsStore : CredentialsStore {
    private data class Credentials(val token: String, val serverUrl: String)
    private val state = MutableStateFlow<Credentials?>(null)

    override fun observeToken(): Flow<String?> = state.map { it?.token }
    override fun observeServerUrl(): Flow<String?> = state.map { it?.serverUrl }
    override suspend fun getToken(): String? = state.value?.token
    override suspend fun getServerUrl(): String? = state.value?.serverUrl

    override suspend fun saveCredentials(token: String, serverUrl: String) {
        state.value = Credentials(token, serverUrl)
    }

    override suspend fun clearCredentials() {
        state.value = null
    }
}
```

- [ ] Run `./gradlew :shared:data:compileKotlinDesktop` — expect PASS
- [ ] Commit: `feat(data): add CredentialsStore interface and InMemoryCredentialsStore`

---

## Task 7 — `RecipeRepositoryImpl`

**Goal:** Implement the domain `RecipeRepository` interface using `RecipeDao` + `RecipeApiClient` + `CredentialsStore`.

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/repository/RecipeRepositoryImpl.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.repository

import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.local.toEntity
import com.ultraviolince.mykitchen.data.local.toDomain
import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.remote.dto.toDto
import com.ultraviolince.mykitchen.data.remote.dto.toEntity
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.domain.model.User
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val api: RecipeApiClient,
    private val credentials: CredentialsStore,
) : RecipeRepository {

    override fun getRecipes(order: RecipeOrder): Flow<List<Recipe>> {
        val flow = when (order) {
            is RecipeOrder.Title -> dao.getRecipesByTitle()
            is RecipeOrder.Date -> dao.getRecipesByDate()
        }
        return flow.map { entities ->
            val mapped = entities.map { it.toDomain() }
            // Apply ascending/descending since Room only returns one direction
            if (order.ascending) mapped else mapped.reversed()
        }
    }

    override suspend fun getRecipeById(id: String): Recipe? =
        dao.getById(id)?.toDomain()

    override suspend fun insertRecipe(recipe: Recipe) {
        dao.insert(recipe.toEntity())
    }

    override suspend fun deleteRecipe(id: String) {
        dao.softDelete(id)
    }

    override suspend fun syncRecipes(): Result<Unit> {
        val token = credentials.getToken() ?: return Result.failure(IllegalStateException("Not logged in"))
        val serverUrl = credentials.getServerUrl() ?: return Result.failure(IllegalStateException("No server URL"))

        // Pull from server
        val remoteResult = api.getRecipes(serverUrl, token)
        if (remoteResult.isFailure) return Result.failure(remoteResult.exceptionOrNull()!!)

        val remoteRecipes = remoteResult.getOrNull()!!
        remoteRecipes.forEach { dto -> dao.insert(dto.toEntity()) }

        // Push unsynced local recipes
        val unsynced = dao.getUnsynced()
        for (entity in unsynced) {
            if (entity.deleted) {
                val result = api.deleteRecipe(serverUrl, token, entity.id)
                if (result.isSuccess) dao.markSynced(entity.id)
            } else {
                val result = api.createRecipe(serverUrl, token, entity.toDto())
                if (result.isSuccess) dao.markSynced(entity.id)
            }
        }
        dao.clearSyncedDeleted()
        return Result.success(Unit)
    }

    override suspend fun login(email: String, password: String, serverUrl: String): Result<Unit> {
        val result = api.login(serverUrl, email, password)
        if (result.isSuccess) {
            credentials.saveCredentials(result.getOrNull()!!.token, serverUrl)
        }
        return result.map { Unit }
    }

    override suspend fun logout() {
        credentials.clearCredentials()
    }

    override fun getAuthState(): Flow<AuthState> =
        combine(credentials.observeToken(), credentials.observeServerUrl()) { token, serverUrl ->
            if (token != null && serverUrl != null) {
                AuthState.LoggedIn(User(email = "", serverUrl = serverUrl, token = token))
            } else {
                AuthState.LoggedOut
            }
        }
}
```

- [ ] Run `./gradlew :shared:data:compileKotlinDesktop` — expect PASS
- [ ] Commit: `feat(data): add RecipeRepositoryImpl`

---

## Task 8 — `FakeRecipeDao` and `RecipeRepositoryImplTest`

**Goal:** Test `RecipeRepositoryImpl` with a fake DAO (no Room needed) and fake API client.

- [ ] Create `shared/data/src/commonTest/kotlin/com/ultraviolince/mykitchen/data/fake/FakeRecipeDao.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.fake

import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.local.RecipeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeDao : RecipeDao {
    private val recipes = MutableStateFlow<List<RecipeEntity>>(emptyList())

    override fun getRecipesByTitle(): Flow<List<RecipeEntity>> =
        recipes.map { it.filter { e -> !e.deleted }.sortedBy { e -> e.title } }

    override fun getRecipesByDate(): Flow<List<RecipeEntity>> =
        recipes.map { it.filter { e -> !e.deleted }.sortedByDescending { e -> e.timestamp } }

    override suspend fun getAllActive(): List<RecipeEntity> =
        recipes.value.filter { !it.deleted }

    override suspend fun getUnsynced(): List<RecipeEntity> =
        recipes.value.filter { it.deleted && !it.synced }

    override suspend fun getById(id: String): RecipeEntity? =
        recipes.value.find { it.id == id }

    override suspend fun insert(recipe: RecipeEntity) {
        val current = recipes.value.toMutableList()
        current.removeIf { it.id == recipe.id }
        current.add(recipe)
        recipes.value = current
    }

    override suspend fun insertAll(entities: List<RecipeEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun softDelete(id: String) {
        recipes.value = recipes.value.map {
            if (it.id == id) it.copy(deleted = true, synced = false) else it
        }
    }

    override suspend fun markSynced(id: String) {
        recipes.value = recipes.value.map {
            if (it.id == id) it.copy(synced = true) else it
        }
    }

    override suspend fun clearSyncedDeleted() {
        recipes.value = recipes.value.filter { !(it.deleted && it.synced) }
    }
}
```

- [ ] Create `shared/data/src/commonTest/kotlin/com/ultraviolince/mykitchen/data/fake/FakeRecipeApiClient.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.fake

import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.remote.dto.LoginResponse
import com.ultraviolince.mykitchen.data.remote.dto.RecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Creates a RecipeApiClient backed by a configurable MockEngine. */
fun buildMockApiClient(
    loginToken: String = "test-token",
    remoteRecipes: List<RecipeDto> = emptyList(),
    loginSuccess: Boolean = true,
): RecipeApiClient {
    val engine = MockEngine { request ->
        val path = request.url.encodedPath
        when {
            path.endsWith("/users/login") -> {
                if (loginSuccess) {
                    respond(
                        content = """{"token":"$loginToken"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else {
                    respond(content = """{"error":"unauthorized"}""", status = HttpStatusCode.Unauthorized)
                }
            }
            path.endsWith("/recipes") && request.method.value == "GET" -> {
                val json = Json.encodeToString(kotlinx.serialization.serializer<List<RecipeDto>>(), remoteRecipes)
                respond(content = json, status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
            path.contains("/recipes/") || path.endsWith("/recipes") -> {
                respond(content = """{"id":"new","title":"","content":"","created_at":0,"updated_at":0}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
            else -> respond(content = "", status = HttpStatusCode.NotFound)
        }
    }
    val client = HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    return RecipeApiClient(client)
}
```

- [ ] Write tests in `shared/data/src/commonTest/kotlin/com/ultraviolince/mykitchen/data/repository/RecipeRepositoryImplTest.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.repository

import com.ultraviolince.mykitchen.data.fake.FakeRecipeDao
import com.ultraviolince.mykitchen.data.fake.buildMockApiClient
import com.ultraviolince.mykitchen.data.store.InMemoryCredentialsStore
import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeRepositoryImplTest {

    private fun buildRepo(
        loginToken: String = "test-token",
        loginSuccess: Boolean = true,
    ): Pair<RecipeRepositoryImpl, FakeRecipeDao> {
        val dao = FakeRecipeDao()
        val api = buildMockApiClient(loginToken = loginToken, loginSuccess = loginSuccess)
        val credentials = InMemoryCredentialsStore()
        return RecipeRepositoryImpl(dao, api, credentials) to dao
    }

    @Test
    fun `insertRecipe and getRecipeById round-trips`() = runTest {
        val (repo, _) = buildRepo()
        val r = Recipe.create("Pasta", "Boil water")
        repo.insertRecipe(r)
        assertEquals(r.title, repo.getRecipeById(r.id)?.title)
    }

    @Test
    fun `deleteRecipe soft-deletes so recipe disappears from getRecipes`() = runTest {
        val (repo, _) = buildRepo()
        val r = Recipe.create("Pasta", "Boil water")
        repo.insertRecipe(r)
        repo.deleteRecipe(r.id)
        val items = repo.getRecipes(RecipeOrder.Title()).first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun `getRecipeById returns null for missing id`() = runTest {
        val (repo, _) = buildRepo()
        assertNull(repo.getRecipeById("no-such-id"))
    }

    @Test
    fun `login sets LoggedIn auth state`() = runTest {
        val (repo, _) = buildRepo(loginToken = "my-jwt")
        repo.login("user@test.com", "pass", "http://localhost:5000")
        assertTrue(repo.getAuthState().first() is AuthState.LoggedIn)
    }

    @Test
    fun `login failure leaves auth state as LoggedOut`() = runTest {
        val (repo, _) = buildRepo(loginSuccess = false)
        repo.login("user@test.com", "wrong", "http://localhost:5000")
        assertEquals(AuthState.LoggedOut, repo.getAuthState().first())
    }

    @Test
    fun `logout clears auth state`() = runTest {
        val (repo, _) = buildRepo()
        repo.login("user@test.com", "pass", "http://localhost:5000")
        repo.logout()
        assertEquals(AuthState.LoggedOut, repo.getAuthState().first())
    }

    @Test
    fun `syncRecipes pulls remote recipes into local DB`() = runTest {
        val (repo, dao) = buildRepo()
        repo.login("user@test.com", "pass", "http://localhost:5000")
        // Rebuild with remote recipes
        val dao2 = FakeRecipeDao()
        val api2 = buildMockApiClient(
            remoteRecipes = listOf(
                com.ultraviolince.mykitchen.data.remote.dto.RecipeDto(
                    id = "remote-1", title = "Remote Pasta", content = "Remote",
                    createdAt = 1000L, updatedAt = 1000L,
                )
            )
        )
        val creds2 = com.ultraviolince.mykitchen.data.store.InMemoryCredentialsStore()
        val repo2 = RecipeRepositoryImpl(dao2, api2, creds2)
        repo2.login("user@test.com", "pass", "http://localhost:5000")
        repo2.syncRecipes()
        val items = repo2.getRecipes(RecipeOrder.Title()).first()
        assertEquals(1, items.size)
        assertEquals("Remote Pasta", items.first().title)
    }

    @Test
    fun `syncRecipes returns failure when not logged in`() = runTest {
        val dao = FakeRecipeDao()
        val api = buildMockApiClient()
        val credentials = InMemoryCredentialsStore()
        val repo = RecipeRepositoryImpl(dao, api, credentials)
        val result = repo.syncRecipes()
        assertTrue(result.isFailure)
    }
}
```

- [ ] Run `./gradlew :shared:data:desktopTest` — expect PASS
- [ ] Commit: `test(data): add FakeRecipeDao and RecipeRepositoryImplTest`

---

## Task 9 — Koin `DataModule` + final build verification

**Goal:** Wire the data layer into Koin and verify the full project still compiles and tests pass.

- [ ] Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/di/DataModule.kt`:

```kotlin
package com.ultraviolince.mykitchen.data.di

import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.repository.RecipeRepositoryImpl
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.data.store.InMemoryCredentialsStore
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    // HTTP client — engine is provided by platform-specific module
    single {
        HttpClient(get()) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { RecipeApiClient(get()) }
    single<CredentialsStore> { InMemoryCredentialsStore() }
    single<RecipeRepository> { RecipeRepositoryImpl(get(), get(), get()) }
}
```

> **Note:** The Ktor `HttpEngine` (CIO for JVM/Android, Js for WasmJs) must be provided by a platform-specific Koin module. The `dataModule` above declares `get()` for the engine and expects it to be bound elsewhere. This is wired in Phase 6.

- [ ] Run `./gradlew :shared:data:compileKotlinDesktop` — expect PASS
- [ ] Run `./gradlew :shared:data:desktopTest` — all tests green
- [ ] Run `./gradlew :androidApp:assembleDebug` — must still compile
- [ ] Run `./gradlew :server:compileKotlin` — must still compile
- [ ] Commit: `feat(data): add Koin DataModule`

---

## Acceptance Criteria

All must be true before this phase is considered done:

- [ ] `./gradlew :shared:data:desktopTest` passes — all tests green
- [ ] `./gradlew :shared:data:compileKotlinDesktop` passes
- [ ] `./gradlew :androidApp:assembleDebug` passes — no regressions
- [ ] `./gradlew :server:compileKotlin` passes — no regressions
- [ ] `RecipeRepositoryImpl` implements all 8 methods of `RecipeRepository`
- [ ] `RecipeApiClientTest` has at least 4 tests (login success, login failure, getRecipes, deleteRecipe)
- [ ] `RecipeRepositoryImplTest` has at least 7 tests
- [ ] No MockK or JVM-only test libraries
- [ ] All new files end with newline

---

## Notes for Agents

- **KSP target names**: For `jvm("desktop")` the KSP config key is `"kspDesktop"` (not `"kspJvm"`).
- **Room `@Database(exportSchema = false)`**: Required to avoid needing a schema directory during compile.
- **Flow mapping for order direction**: Room `@Query` ORDER BY is fixed (ASC or DESC). The `getRecipes()` implementation maps to `getRecipesByTitle()` or `getRecipesByDate()` (both ASC from Room), then reverses for descending. This is simpler than dynamic SQL.
- **`FakeRecipeDao` in test sources**: Located in `commonTest` — it is NOT a Room implementation, just an in-memory list.
- **`buildMockApiClient` helper**: Lives in `commonTest` — creates a `RecipeApiClient` backed by `MockEngine`. Import from `com.ultraviolince.mykitchen.data.fake`.
- **`ContentNegotiation` import**: `io.ktor.client.plugins.contentnegotiation.ContentNegotiation` (client-side, not server-side).
- **`runCatching`**: Used in `RecipeApiClient` to wrap network calls in `Result<T>`. This is idiomatic and KMP-compatible.
- **WasmJs `DatabaseFactory.kt`**: Must compile but intentionally throws. It is never called — Phase 6 will wire WasmJs with an alternative strategy.
- Always run `compileKotlinDesktop` before committing — it's the fastest way to catch issues.
