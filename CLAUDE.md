# Claude Code – Project Instructions

## Environment constraint: Google Maven is blocked

This project runs inside Anthropic's remote Claude Code sandbox. The sandbox
blocks outbound network access to `dl.google.com` (Google Maven). Android
Gradle Plugin (AGP) is distributed exclusively from Google Maven, so **any
`./gradlew` invocation that touches an Android module fails immediately** with
"Plugin not found" before a single build-script line is evaluated.

This means compilation errors, DSL mistakes, and misconfigurations inside
`androidApp/build.gradle.kts` or any module that AGP configures are completely
invisible locally. They only surface in CI where real internet access exists.

---

## What CAN be run locally

These tasks involve no AGP dependency and work in the sandbox:

```bash
./gradlew detekt                          # Static analysis (all modules)
./gradlew :shared:domain:desktopTest      # Domain unit tests (JVM)
./gradlew :shared:data:desktopTest        # Data unit tests (JVM)
./gradlew :server:test                    # Server unit tests (JVM)
./gradlew koverXmlReport                  # Coverage report (aggregated JVM)
./gradlew :server:buildFatJar             # Server compilation check
./gradlew :desktopApp:compileKotlin       # Desktop Kotlin compilation
```

Use `scripts/validate-pr.sh` to run all locally-executable checks at once.

---

## What CANNOT be run locally (CI-only)

| Check | Why |
|---|---|
| `./gradlew :androidApp:assembleDebug/Release` | Requires AGP from Google Maven |
| `./gradlew :androidApp:testDebugUnitTest` | AGP dependency |
| `./gradlew :androidApp:verifyRoborazziDebug` | AGP dependency |
| `./gradlew :androidApp:recordRoborazziDebug` | AGP dependency |
| `./gradlew :webApp:wasmJsBrowserDistribution` | Requires AGP for multi-platform config |
| `./gradlew connectedCheck` | Requires emulator + AGP |
| iOS framework builds | Requires macOS + Xcode |
| Docker image builds | Requires AGP (runs `buildFatJar` which configures all subprojects including androidApp) |

---

## Pre-push rules

1. **Always run `scripts/validate-pr.sh`** before committing. This runs all
   checks that are locally executable (detekt, JVM tests, server compilation).

2. **Changes touching these paths MUST have CI passing before the task is
   declared complete:**
   - `androidApp/` – any file
   - `shared/ui/` – any file (Android module)
   - `.github/workflows/` – CI workflow files
   - `Dockerfile` or `docker-compose*.yml`
   - `build-logic/` – convention plugins (affect Android compilation)
   - `gradle/libs.versions.toml` – version changes affect Android builds

3. **Do not declare success based on local checks alone** when the changed
   files fall into the CI-only category above. Push to the branch, wait for
   all GitHub Actions jobs (`build`, `web`, `ios`, `instrumentedtests`,
   `verify-screenshots`, `backend-image`, `web-image`) to turn green, and only then report
   the task as done.

4. **When editing `androidApp/build.gradle.kts`**, remember that Kotlin script
   files (`*.gradle.kts`) in Android modules use the full Gradle Kotlin DSL
   with AGP extensions. Mistakes in DSL property names (e.g., a nonexistent
   property on a plugin extension) will only be caught by CI.

5. **Roborazzi `generateComposePreviewRobolectricTests` is experimental** –
   the block requires `@file:OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)`
   at the top of the Gradle script file.

---

## Project structure

```
androidApp/          Android application (AGP, Compose)
desktopApp/          Desktop JVM application (Compose for Desktop)
webApp/              Web application (Kotlin/WasmJs)
shared/
  domain/            Pure Kotlin business logic + use cases (no Android)
  data/              Repository implementations, Room, Ktor client (KMP)
  ui/                Compose Multiplatform UI (Android + Desktop + iOS + Web)
server/              Ktor server (JVM)
build-logic/         Convention plugins (kmp-library, server-app, etc.)
scripts/             validate-pr.sh and pre-commit-hook.sh
.github/workflows/   CI: test.yaml (build/test/coverage), backend-image.yml (Docker)
```

---

## CI jobs overview

| Job | Runner | Validates |
|---|---|---|
| `build` | ubuntu | Android APK (debug+release), desktop compile, unit tests, Kover coverage, Detekt |
| `web` | ubuntu | WasmJs browser distribution |
| `ios` | macos | iOS framework linking (arm64 + simulator) |
| `instrumentedtests` | ubuntu (emulator) | Android instrumented tests, API 28/31/34/35/36 |
| `verify-screenshots` | ubuntu | Roborazzi screenshot regression tests |
| `backend-image` | ubuntu | Docker build + push (server) |
| `web-image` | ubuntu | Docker build + push (web) |

All jobs must be green before a PR is merged.
