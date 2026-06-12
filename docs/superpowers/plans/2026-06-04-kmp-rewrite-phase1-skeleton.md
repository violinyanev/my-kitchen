# Phase 1: Project Skeleton — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Set up the new Kotlin Multiplatform multi-module project structure with all targets compiling successfully (empty modules with minimal code).

**Architecture:** Gradle multi-module project with convention plugins in `build-logic/`, shared modules (`domain`, `data`, `ui`), platform entry points (`androidApp`, `desktopApp`, `webApp`, `iosApp`), and a `server` module. All modules compile but contain only minimal placeholder code.

**Tech Stack:** Kotlin 2.3.20, Compose Multiplatform 1.10.6, Ktor 3.5.0, Koin 4.2.1, Gradle 9.5.1, AGP 9.2.1

---

## File Structure

This plan creates the following new structure (replacing existing modules):

```
my-kitchen/
├── build-logic/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── kmp-library.gradle.kts          # Convention: KMP library
│       ├── kmp-compose.gradle.kts          # Convention: Compose Multiplatform
│       └── server-app.gradle.kts           # Convention: Ktor server
├── shared/
│   ├── domain/
│   │   ├── build.gradle.kts
│   │   └── src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/
│   │       └── Placeholder.kt
│   ├── data/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── commonMain/kotlin/com/ultraviolince/mykitchen/data/
│   │       │   └── Placeholder.kt
│   │       ├── androidMain/kotlin/
│   │       ├── iosMain/kotlin/
│   │       ├── desktopMain/kotlin/
│   │       └── wasmJsMain/kotlin/
│   └── ui/
│       ├── build.gradle.kts
│       └── src/
│           ├── commonMain/kotlin/com/ultraviolince/mykitchen/ui/
│           │   └── App.kt
│           ├── androidMain/kotlin/
│           ├── iosMain/kotlin/
│           └── desktopMain/kotlin/
├── androidApp/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/com/ultraviolince/mykitchen/
│           └── MainActivity.kt
├── desktopApp/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/ultraviolince/mykitchen/
│       └── Main.kt
├── webApp/
│   ├── build.gradle.kts
│   └── src/wasmJsMain/kotlin/com/ultraviolince/mykitchen/
│       └── Main.kt
├── iosApp/
│   ├── iosApp/
│   │   ├── App.swift
│   │   └── Info.plist
│   └── iosApp.xcodeproj/ (keep existing, update framework reference)
├── server/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/ultraviolince/mykitchen/server/
│       │   └── Application.kt
│       └── main/resources/
│           └── application.conf
├── gradle/
│   └── libs.versions.toml               # Updated version catalog
├── settings.gradle.kts                    # New module includes
├── build.gradle.kts                       # Updated root build
└── gradle.properties                      # Updated properties
```

---

## Task 1: Clean Up — Remove Old Modules

**Files:**
- Delete: `app/` directory (entire old Android module)
- Delete: `shared/` directory (old shared module)
- Delete: `backend/` directory (Python Flask backend)
- Delete: `iosApp/` directory (old iOS stub)
- Delete: `kotlin-js-store/` directory
- Delete: `commitlint.config.js`
- Delete: `package.json`
- Delete: `build/js/` directory
- Keep: `gradle/`, `scripts/`, `docs/`, `.github/`, root build files, `gradlew*`

- [ ] **Step 1: Delete old source directories**

```bash
rm -rf app/ shared/ backend/ iosApp/ kotlin-js-store/ build/js/
rm -f commitlint.config.js package.json pyproject.toml
```

- [ ] **Step 2: Commit the cleanup**

```bash
git add -A
git commit -m "chore: remove old project structure for KMP rewrite"
```

---

## Task 2: Update Version Catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Replace version catalog with new multiplatform-focused versions**

Write this content to `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.3.20"
agp = "9.2.1"
ksp = "2.3.8"

# Compose
composeMultiplatform = "1.10.6"
composeMaterial3Multiplatform = "1.4.0"

# AndroidX
androidxActivity = "1.10.1"
androidxLifecycle = "2.9.1"
navigationCompose = "2.9.8"

# Networking
ktor = "3.5.0"

# DI
koin = "4.2.1"

# Database
room = "2.8.4"
exposed = "0.61.0"
postgresql = "42.7.5"
hikari = "6.3.0"
h2 = "2.4.232"

# Serialization
kotlinxSerialization = "1.11.0"
kotlinxDatetime = "0.8.0"
kotlinxCoroutines = "1.11.0"

# Security
bcrypt = "0.10.2"

# Testing
junit = "4.13.2"
turbine = "1.2.1"
truth = "1.4.5"
mockk = "1.14.11"

# Code Quality
detekt = "1.23.8"
kover = "0.9.8"
roborazzi = "1.63.0"
robolectric = "4.16.1"

# Android SDK
# @keep
compileSdk = "36"
# @keep
minSdk = "28"
# @keep
targetSdk = "36"

# Logging
logback = "1.5.18"

[libraries]
# Kotlin
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinxDatetime" }

# Compose
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "composeMaterial3Multiplatform" }

# AndroidX
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidxActivity" }
androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androidxLifecycle" }
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigationCompose" }

# Room (KMP)
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

# Ktor Client
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }

# Ktor Server
ktor-server-core = { module = "io.ktor:ktor-server-core", version.ref = "ktor" }
ktor-server-netty = { module = "io.ktor:ktor-server-netty", version.ref = "ktor" }
ktor-server-content-negotiation = { module = "io.ktor:ktor-server-content-negotiation", version.ref = "ktor" }
ktor-server-auth = { module = "io.ktor:ktor-server-auth", version.ref = "ktor" }
ktor-server-auth-jwt = { module = "io.ktor:ktor-server-auth-jwt", version.ref = "ktor" }
ktor-server-cors = { module = "io.ktor:ktor-server-cors", version.ref = "ktor" }
ktor-server-status-pages = { module = "io.ktor:ktor-server-status-pages", version.ref = "ktor" }
ktor-server-test = { module = "io.ktor:ktor-server-test-host", version.ref = "ktor" }
ktor-serialization-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }

# DI
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
koin-ktor = { module = "io.insert-koin:koin-ktor", version.ref = "koin" }
koin-test = { module = "io.insert-koin:koin-test", version.ref = "koin" }

# Server Database
exposed-core = { module = "org.jetbrains.exposed:exposed-core", version.ref = "exposed" }
exposed-dao = { module = "org.jetbrains.exposed:exposed-dao", version.ref = "exposed" }
exposed-jdbc = { module = "org.jetbrains.exposed:exposed-jdbc", version.ref = "exposed" }
exposed-kotlin-datetime = { module = "org.jetbrains.exposed:exposed-kotlin-datetime", version.ref = "exposed" }
postgresql = { module = "org.postgresql:postgresql", version.ref = "postgresql" }
hikari = { module = "com.zaxxer:HikariCP", version.ref = "hikari" }
h2 = { module = "com.h2database:h2", version.ref = "h2" }

# Security
bcrypt = { module = "at.favre.lib:bcrypt", version.ref = "bcrypt" }

# Logging
logback = { module = "ch.qos.logback:logback-classic", version.ref = "logback" }

# Testing
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
truth = { module = "com.google.truth:truth", version.ref = "truth" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
junit = { module = "junit:junit", version.ref = "junit" }

# Code Quality
detekt-formatting = { module = "io.gitlab.arturbosch.detekt:detekt-formatting", version.ref = "detekt" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
ktor = { id = "io.ktor.plugin", version.ref = "ktor" }
```

- [ ] **Step 2: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build: update version catalog for KMP multiplatform rewrite"
```

---

## Task 3: Create Convention Plugins (`build-logic/`)

**Files:**
- Create: `build-logic/settings.gradle.kts`
- Create: `build-logic/build.gradle.kts`
- Create: `build-logic/src/main/kotlin/kmp-library.gradle.kts`
- Create: `build-logic/src/main/kotlin/kmp-compose.gradle.kts`
- Create: `build-logic/src/main/kotlin/server-app.gradle.kts`

- [ ] **Step 1: Create build-logic settings**

Create `build-logic/settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
```

- [ ] **Step 2: Create build-logic build file**

Create `build-logic/build.gradle.kts`:

```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.plugins.kotlin.multiplatform.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    compileOnly(libs.plugins.compose.multiplatform.toDep())
    compileOnly(libs.plugins.compose.compiler.toDep())
    compileOnly(libs.plugins.kotlin.jvm.toDep())
    compileOnly(libs.plugins.ktor.toDep())
    compileOnly(libs.plugins.kotlin.serialization.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
```

- [ ] **Step 3: Create KMP library convention plugin**

Create `build-logic/src/main/kotlin/kmp-library.gradle.kts`:

```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvm("desktop")

    androidTarget {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("kotlinx-serialization-json").get())
            implementation(libs.findLibrary("kotlinx-datetime").get())
            implementation(libs.findLibrary("koin-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            implementation(libs.findLibrary("turbine").get())
        }
    }
}
```

- [ ] **Step 4: Create KMP Compose convention plugin**

Create `build-logic/src/main/kotlin/kmp-compose.gradle.kts`:

```kotlin
plugins {
    id("kmp-library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
            implementation(libs.findLibrary("androidx-navigation-compose").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
        }
    }
}
```

- [ ] **Step 5: Create Server convention plugin**

Create `build-logic/src/main/kotlin/server-app.gradle.kts`:

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.ktor.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add build-logic/
git commit -m "build: add convention plugins for KMP library, compose, and server"
```

---

## Task 4: Update Root Build Files

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `build.gradle.kts`
- Modify: `gradle.properties`

- [ ] **Step 1: Write new settings.gradle.kts**

Replace `settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "my-kitchen"

include(":shared:domain")
include(":shared:data")
include(":shared:ui")
include(":androidApp")
include(":desktopApp")
include(":webApp")
include(":server")
```

- [ ] **Step 2: Write new root build.gradle.kts**

Replace `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktor) apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        config.setFrom(rootProject.files("gradle/detekt.yml"))
        buildUponDefaultConfig = true
    }
}
```

- [ ] **Step 3: Update gradle.properties**

Replace `gradle.properties`:

```properties
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx12g -Dfile.encoding=UTF-8 -XX:+UseParallelGC -Xshare:off

# Android
android.useAndroidX=true
android.nonTransitiveRClass=true

# Kotlin
kotlin.code.style=official
kotlin.mpp.androidSourceSetLayoutV2AndroidStyleDirs.nowarn=true
kotlin.native.ignoreDisabledTargets=true

# Compose
org.jetbrains.compose.experimental.wasm.enabled=true

# Gradle optimizations
org.gradle.configuration-cache=true
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.vfs.watch=true
```

- [ ] **Step 4: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties
git commit -m "build: configure root build for multi-module KMP project"
```

---

## Task 5: Create `:shared:domain` Module

**Files:**
- Create: `shared/domain/build.gradle.kts`
- Create: `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/model/Recipe.kt`

- [ ] **Step 1: Create shared/domain build file**

Create `shared/domain/build.gradle.kts`:

```kotlin
plugins {
    id("kmp-library")
}

kotlin {
    // Domain is pure Kotlin — no Android-specific config needed beyond what convention provides
    sourceSets {
        androidMain.dependencies {
            // No android-specific deps for domain
        }
    }
}

android {
    namespace = "com.ultraviolince.mykitchen.shared.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
```

- [ ] **Step 2: Create placeholder domain model**

Create `shared/domain/src/commonMain/kotlin/com/ultraviolince/mykitchen/domain/model/Recipe.kt`:

```kotlin
package com.ultraviolince.mykitchen.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Instant,
    val synced: Boolean = false,
    val deleted: Boolean = false,
)
```

- [ ] **Step 3: Commit**

```bash
git add shared/domain/
git commit -m "feat: add :shared:domain module with Recipe model"
```

---

## Task 6: Create `:shared:data` Module

**Files:**
- Create: `shared/data/build.gradle.kts`
- Create: `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/Placeholder.kt`

- [ ] **Step 1: Create shared/data build file**

Create `shared/data/build.gradle.kts`:

```kotlin
plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
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

android {
    namespace = "com.ultraviolince.mykitchen.shared.data"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
```

- [ ] **Step 2: Create placeholder data file**

Create `shared/data/src/commonMain/kotlin/com/ultraviolince/mykitchen/data/Placeholder.kt`:

```kotlin
package com.ultraviolince.mykitchen.data

/**
 * Placeholder — will contain repository implementations, API client, local DB.
 */
internal object DataModule
```

- [ ] **Step 3: Commit**

```bash
git add shared/data/
git commit -m "feat: add :shared:data module with Ktor client dependencies"
```

---

## Task 7: Create `:shared:ui` Module

**Files:**
- Create: `shared/ui/build.gradle.kts`
- Create: `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/App.kt`

- [ ] **Step 1: Create shared/ui build file**

Create `shared/ui/build.gradle.kts`:

```kotlin
plugins {
    id("kmp-compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
            implementation(project(":shared:data"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "com.ultraviolince.mykitchen.shared.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
```

- [ ] **Step 2: Create the shared App composable**

Create `shared/ui/src/commonMain/kotlin/com/ultraviolince/mykitchen/ui/App.kt`:

```kotlin
package com.ultraviolince.mykitchen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Text("My Kitchen")
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/ui/
git commit -m "feat: add :shared:ui module with App composable placeholder"
```

---

## Task 8: Create `:androidApp` Module

**Files:**
- Create: `androidApp/build.gradle.kts`
- Create: `androidApp/src/main/AndroidManifest.xml`
- Create: `androidApp/src/main/kotlin/com/ultraviolince/mykitchen/MainActivity.kt`

- [ ] **Step 1: Create androidApp build file**

Create `androidApp/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ultraviolince.mykitchen"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ultraviolince.mykitchen"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "2.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared:ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
}
```

- [ ] **Step 2: Create AndroidManifest.xml**

Create `androidApp/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="My Kitchen"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyKitchen">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MyKitchen">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 3: Create MainActivity**

Create `androidApp/src/main/kotlin/com/ultraviolince/mykitchen/MainActivity.kt`:

```kotlin
package com.ultraviolince.mykitchen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ultraviolince.mykitchen.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
```

- [ ] **Step 4: Create minimal Android resources**

Create `androidApp/src/main/res/values/styles.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.MyKitchen" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

Create `androidApp/src/main/res/mipmap-hdpi/ic_launcher.xml` — skip this, use a placeholder:

Create `androidApp/src/main/res/values/strings.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">My Kitchen</string>
</resources>
```

- [ ] **Step 5: Commit**

```bash
git add androidApp/
git commit -m "feat: add :androidApp module (thin Android shell)"
```

---

## Task 9: Create `:desktopApp` Module

**Files:**
- Create: `desktopApp/build.gradle.kts`
- Create: `desktopApp/src/main/kotlin/com/ultraviolince/mykitchen/Main.kt`

- [ ] **Step 1: Create desktopApp build file**

Create `desktopApp/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":shared:ui"))
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}

compose.desktop {
    application {
        mainClass = "com.ultraviolince.mykitchen.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "My Kitchen"
            packageVersion = "2.0.0"
        }
    }
}
```

- [ ] **Step 2: Create Desktop main entry point**

Create `desktopApp/src/main/kotlin/com/ultraviolince/mykitchen/Main.kt`:

```kotlin
package com.ultraviolince.mykitchen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ultraviolince.mykitchen.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "My Kitchen",
    ) {
        App()
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add desktopApp/
git commit -m "feat: add :desktopApp module (JVM desktop entry point)"
```

---

## Task 10: Create `:webApp` Module

**Files:**
- Create: `webApp/build.gradle.kts`
- Create: `webApp/src/wasmJsMain/kotlin/com/ultraviolince/mykitchen/Main.kt`

- [ ] **Step 1: Create webApp build file**

Create `webApp/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "mykitchen.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared:ui"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}
```

- [ ] **Step 2: Create Web main entry point**

Create `webApp/src/wasmJsMain/kotlin/com/ultraviolince/mykitchen/Main.kt`:

```kotlin
package com.ultraviolince.mykitchen

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.ultraviolince.mykitchen.ui.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(title = "My Kitchen", canvasElementId = "ComposeTarget") {
        App()
    }
}
```

- [ ] **Step 3: Create index.html**

Create `webApp/src/wasmJsMain/resources/index.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Kitchen</title>
    <style>
        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            overflow: hidden;
        }
        #ComposeTarget {
            width: 100%;
            height: 100%;
        }
    </style>
</head>
<body>
<canvas id="ComposeTarget"></canvas>
<script src="mykitchen.js"></script>
</body>
</html>
```

- [ ] **Step 4: Commit**

```bash
git add webApp/
git commit -m "feat: add :webApp module (WasmJs browser entry point)"
```

---

## Task 11: Create `:server` Module

**Files:**
- Create: `server/build.gradle.kts`
- Create: `server/src/main/kotlin/com/ultraviolince/mykitchen/server/Application.kt`
- Create: `server/src/main/resources/application.conf`
- Create: `server/src/main/resources/logback.xml`

- [ ] **Step 1: Create server build file**

Create `server/build.gradle.kts`:

```kotlin
plugins {
    id("server-app")
}

application {
    mainClass.set("com.ultraviolince.mykitchen.server.ApplicationKt")
}

dependencies {
    implementation(project(":shared:domain"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.json)
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgresql)
    implementation(libs.hikari)
    implementation(libs.bcrypt)
    implementation(libs.logback)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.ktor.server.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.h2)
    testImplementation(libs.koin.test)
}
```

- [ ] **Step 2: Create Application entry point**

Create `server/src/main/kotlin/com/ultraviolince/mykitchen/server/Application.kt`:

```kotlin
package com.ultraviolince.mykitchen.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 5000, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respondText("OK")
        }
        get("/version") {
            call.respondText("""{"version": "2.0.0"}""")
        }
    }
}
```

- [ ] **Step 3: Create application.conf**

Create `server/src/main/resources/application.conf`:

```hocon
ktor {
    deployment {
        port = 5000
        port = ${?PORT}
    }
    application {
        modules = [ com.ultraviolince.mykitchen.server.ApplicationKt.module ]
    }
}

database {
    url = "jdbc:postgresql://localhost:5432/mykitchen"
    url = ${?DATABASE_URL}
    driver = "org.postgresql.Driver"
    user = "mykitchen"
    user = ${?DATABASE_USER}
    password = "mykitchen"
    password = ${?DATABASE_PASSWORD}
}

jwt {
    secret = "dev-secret-change-in-production"
    secret = ${?JWT_SECRET}
    issuer = "mykitchen"
    audience = "mykitchen-users"
    realm = "My Kitchen"
}
```

- [ ] **Step 4: Create logback.xml**

Create `server/src/main/resources/logback.xml`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
    <logger name="io.ktor" level="INFO"/>
    <logger name="org.jetbrains.exposed" level="INFO"/>
</configuration>
```

- [ ] **Step 5: Commit**

```bash
git add server/
git commit -m "feat: add :server module with Ktor + health endpoint"
```

---

## Task 12: Create iOS App Structure

**Files:**
- Create: `iosApp/iosApp/App.swift`
- Create: `iosApp/iosApp/Info.plist`
- Create: `iosApp/iosApp/ContentView.swift`

Note: The iOS app uses a SwiftUI host that embeds the Compose Multiplatform UI via UIKit interop. The Xcode project file will need manual configuration — this task creates the Swift source files.

- [ ] **Step 1: Create iOS app directory and App.swift**

Create `iosApp/iosApp/App.swift`:

```swift
import SwiftUI

@main
struct MyKitchenApp: SwiftUI.App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

- [ ] **Step 2: Create ContentView.swift with Compose interop**

Create `iosApp/iosApp/ContentView.swift`:

```swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

- [ ] **Step 3: Create Info.plist**

Create `iosApp/iosApp/Info.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>My Kitchen</string>
    <key>CFBundleIdentifier</key>
    <string>com.ultraviolince.mykitchen</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>CFBundleShortVersionString</key>
    <string>2.0.0</string>
    <key>UILaunchScreen</key>
    <dict/>
</dict>
</plist>
```

- [ ] **Step 4: Create iOS MainViewController in shared:ui**

Create `shared/ui/src/iosMain/kotlin/com/ultraviolince/mykitchen/ui/MainViewController.kt`:

```kotlin
package com.ultraviolince.mykitchen.ui

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }
```

- [ ] **Step 5: Commit**

```bash
git add iosApp/ shared/ui/src/iosMain/
git commit -m "feat: add iOS app structure with Compose interop"
```

---

## Task 13: Build Verification

- [ ] **Step 1: Run Gradle sync and verify structure compiles**

```bash
./gradlew projects
```

Expected output should list all modules:
```
Root project 'my-kitchen'
+--- Project ':androidApp'
+--- Project ':desktopApp'
+--- Project ':server'
+--- Project ':shared'
|    +--- Project ':shared:data'
|    +--- Project ':shared:domain'
|    \--- Project ':shared:ui'
\--- Project ':webApp'
```

- [ ] **Step 2: Build shared:domain (fastest, validates convention plugin)**

```bash
./gradlew :shared:domain:compileKotlinDesktop
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Build server (validates JVM + Ktor setup)**

```bash
./gradlew :server:compileKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Build desktopApp (validates Compose Multiplatform)**

```bash
./gradlew :desktopApp:compileKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Build androidApp (validates Android + Compose)**

```bash
./gradlew :androidApp:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Build webApp (validates WasmJs target)**

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentWebpack
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Run server health check**

```bash
./gradlew :server:run &
sleep 5
curl http://localhost:5000/health
kill %1
```

Expected output: `OK`

- [ ] **Step 8: Final commit if any fixes were needed**

```bash
git add -A
git status
# Only commit if there are changes
git diff --cached --quiet || git commit -m "fix: build issues from skeleton verification"
```

---

## Task 14: Update .gitignore and Documentation

**Files:**
- Modify: `.gitignore` (add `.superpowers/`)
- Create: `AGENTS.md` (new agent-ready documentation — placeholder for Phase 8)

- [ ] **Step 1: Update .gitignore**

Append to `.gitignore`:

```
# Superpowers brainstorming artifacts
.superpowers/
```

- [ ] **Step 2: Commit**

```bash
git add .gitignore
git commit -m "chore: update .gitignore for new project structure"
```

---

## Summary

After completing all 14 tasks, the repository will have:
- ✅ Multi-module Gradle project with convention plugins
- ✅ `:shared:domain` compiling for all KMP targets
- ✅ `:shared:data` with Ktor client dependencies configured per-platform
- ✅ `:shared:ui` with Compose Multiplatform and a placeholder App
- ✅ `:androidApp` building a debug APK
- ✅ `:desktopApp` building a JVM desktop app
- ✅ `:webApp` building a WasmJs browser app
- ✅ `:server` with Ktor responding to /health
- ✅ iOS app structure with Compose interop
- ✅ All modules compile successfully

**Next:** Phase 2 (Shared Domain — port models + use cases) and Phase 3 (Shared Data — Room KMP + Ktor client) can proceed in parallel.
