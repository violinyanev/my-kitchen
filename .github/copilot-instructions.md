# My Kitchen - Recipe Management Application

My Kitchen is a free and open source recipe management application featuring an Android mobile app and a self-hosted Python Flask backend server. Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the information here.

## Working Effectively

### Prerequisites and Setup
- Java 17+ (OpenJDK Temurin 17 recommended)
- Python 3.8+ (3.12+ tested and working)
- Docker (optional for backend containerization)
- Android SDK (for app development)

### Bootstrap and Build Commands
Execute these commands in sequence for a complete development setup:

```bash
# Verify prerequisites
java -version    # Should show Java 17+
python3 --version    # Should show Python 3.8+
docker --version     # Optional but recommended

# Install backend dependencies
python3 -m pip install -r backend/image/requirements.txt

# Build health check (first time setup)
./gradlew buildHealth    # NEVER CANCEL: Takes 3-4 minutes on first run, 10+ minute timeout recommended

# Build Android debug APK
./gradlew :app:assembleDebug    # NEVER CANCEL: Takes 7+ minutes on cold build, 15+ minute timeout recommended
```

### Main Development Commands

#### Android App Development
```bash
# Debug build (most common during development)
./gradlew :app:assembleDebug    # NEVER CANCEL: 7+ minutes cold, <10 seconds warm, timeout: 15+ minutes

# Release build (for testing production builds)
./gradlew :app:assembleRelease    # NEVER CANCEL: 2-3 minutes, timeout: 10+ minutes

# Run unit tests
./gradlew :app:testDebugUnitTest    # NEVER CANCEL: ~45 seconds, timeout: 5+ minutes

# Run code quality checks
./gradlew detekt    # NEVER CANCEL: ~1 minute, timeout: 5+ minutes

# Run screenshot tests (UI validation)
./gradlew :app:validateDebugScreenshotTest    # NEVER CANCEL: ~30 seconds, timeout: 5+ minutes

# Generate coverage report
./gradlew :app:koverXmlReportDebug    # Fast: ~2 seconds

# Complete CI pipeline (run all checks)
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:validateDebugScreenshotTest :app:koverXmlReportDebug detekt
# NEVER CANCEL: 7+ minutes cold, 2-5 seconds warm, timeout: 15+ minutes
```

#### Backend Development
```bash
# Start backend server for development (Flask directly)
./backend/scripts/dev.sh    # Starts immediately on localhost:5000

# Run backend unit tests
cd backend/image && python3 -m unittest discover    # Fast: <1 second

# Test backend health (while server is running)
curl http://localhost:5000/health    # Should return: OK
```

#### Docker Backend (Alternative)
```bash
# Note: Docker build requires git tags to be present
# Build backend Docker image
python3 ./backend/scripts/dev.py build    # Requires git tags, may fail in development

# Run backend with Docker
python3 ./backend/scripts/dev.py start    # Requires successful build
```

## Build Timing and Timeout Guidelines

**CRITICAL**: These builds may take significant time. NEVER CANCEL long-running commands.

| Command | Cold Build Time | Warm Build Time | Recommended Timeout |
|---------|----------------|-----------------|-------------------|
| `./gradlew buildHealth` | 3-4 minutes | 10 seconds | 10+ minutes |
| `./gradlew :app:assembleDebug` | 7+ minutes | 2-10 seconds | 15+ minutes |
| `./gradlew :app:assembleRelease` | 2-3 minutes | 30 seconds | 10+ minutes |
| `./gradlew :app:testDebugUnitTest` | 45 seconds | 5 seconds | 5+ minutes |
| `./gradlew detekt` | 1 minute | 10 seconds | 5+ minutes |
| Backend tests | <1 second | <1 second | 2 minutes |

**Important**: Gradle builds are much faster on subsequent runs due to incremental compilation and caching. The first build in a clean environment will take the longest time.

## Validation and Testing

### Always Run Before Committing
```bash
# Complete validation pipeline (ensure everything works)
./gradlew buildHealth
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:validateDebugScreenshotTest :app:koverXmlReportDebug detekt

# Start backend for integration testing
./backend/scripts/dev.sh &
curl http://localhost:5000/health    # Verify backend is running
```

### Manual Testing Scenarios
After making changes, always test these scenarios:

#### Backend Testing
1. Start the backend server: `./backend/scripts/dev.sh`
2. Verify health endpoint: `curl http://localhost:5000/health` should return "OK"
3. Test API endpoints return proper JSON responses (note: most require authentication)
4. Backend runs on localhost:5000 with seed data from `backend/seed_data/`

#### Android App Testing
1. Build and verify APK generation: `./gradlew :app:assembleDebug`
2. Confirm APK exists: `app/build/outputs/apk/debug/app-debug.apk`
3. Run unit tests to verify no regressions: `./gradlew :app:testDebugUnitTest`
4. Validate UI components: `./gradlew :app:validateDebugScreenshotTest`

#### Integration Testing
1. Start backend server first: `./backend/scripts/dev.sh`
2. Build Android app with backend running (app expects backend on 10.0.2.2:5000 for emulator)
3. For instrumented tests, backend must be running on localhost:5000

### CI/CD Pipeline Validation
The GitHub Actions workflow runs these exact commands:
```bash
./gradlew buildHealth
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:validateDebugScreenshotTest :app:koverXmlReportDebug detekt
```
Always run these locally before pushing to ensure CI will pass.

## Project Structure and Navigation

```
my-kitchen/
├── app/                     # Android mobile application (Kotlin/Jetpack Compose)
│   ├── src/main/           # Main Android app source code
│   ├── src/test/           # Unit tests
│   ├── src/androidTest/    # Instrumented tests
│   ├── build.gradle.kts    # Android app build configuration
│   └── build/outputs/apk/  # Generated APK files
├── backend/                # Self-hosted Python Flask server
│   ├── image/              # Flask application code and Docker setup
│   │   ├── app.py         # Main Flask application
│   │   └── requirements.txt # Python dependencies
│   ├── scripts/           # Development scripts
│   │   ├── dev.sh         # Start Flask server directly
│   │   └── dev.py         # Docker-based development commands
│   └── seed_data/         # Test data (users.yaml, recipes.yaml)
├── .github/workflows/     # CI/CD pipeline definitions
├── gradle/               # Gradle configuration and version catalog
└── scripts/              # General development utilities
```

### Frequently Used Files
- `app/build.gradle.kts` - Android app build configuration
- `backend/image/app.py` - Main backend application
- `backend/image/requirements.txt` - Python dependencies
- `gradle/libs.versions.toml` - Dependency version catalog
- `.github/workflows/test.yaml` - Main CI pipeline

## Build Artifacts and Reports

After building, find generated files in:
- **APKs**: `app/build/outputs/apk/debug/app-debug.apk` and `app/build/outputs/apk/release/app-release.apk`
- **Test Reports**: `app/build/reports/tests/`
- **Coverage Reports**: `app/build/reports/kover/`
- **Lint Reports**: `app/build/reports/detekt/`
- **Screenshot Test Reports**: `app/build/reports/screenshotTest/`

## Common Issues and Solutions

### Build Issues
- **AGP Version Warning**: "Dependency Analysis plugin" warning is non-blocking
- **Screenshot Test Warning**: Experimental feature warning is non-blocking
- **Docker Build Fails**: Backend Docker script requires git tags; use Flask directly instead
- **Slow First Build**: Initial Gradle builds download dependencies; subsequent builds are much faster

### Development Workflow
- **Backend Server**: Use `./backend/scripts/dev.sh` for direct Flask development
- **API Testing**: Most endpoints require authentication; use `/health` for basic connectivity testing
- **Android Development**: Debug builds include test credentials for backend connectivity
- **Code Quality**: Always run `detekt` before committing; use auto-correct: `./gradlew detekt --auto-correct`

## Architecture Notes
- **Android App**: Built with Kotlin, Jetpack Compose, Room database, Ktor HTTP client
- **Backend**: Python Flask with YAML file storage, JWT authentication
- **Communication**: REST API between Android app and backend
- **Testing**: Unit tests, screenshot tests, instrumented tests requiring backend
- **Build System**: Gradle for Android, pip for Python, Docker for backend deployment