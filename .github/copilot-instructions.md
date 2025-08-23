# My Kitchen - Recipe Management Application

My Kitchen is a free and open source recipe management application featuring an Android mobile app and a self-hosted Python Flask backend server. Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the information here.

## 🚨 CRITICAL: ALWAYS Validate Before Committing
**Every PR must pass ALL GitHub Actions checks. Use `./scripts/validate-pr.sh` before committing ANY changes to ensure green checks.**

**⚠️ INSTRUMENTED TESTS ARE MANDATORY**: Work is NOT complete until both unit tests AND instrumented tests pass. GitHub Actions runs instrumented tests on Android API levels 28, 31, 34, and 35. If you cannot run instrumented tests locally, ensure all other validations pass and let GitHub Actions verify instrumented tests automatically.

## Working Effectively

### Prerequisites and Setup
- Java 17+ (OpenJDK Temurin 17 recommended)
- Python 3.8+ (3.12+ tested and working)
- Docker (optional for backend containerization)
- Android SDK (for app development)

#### Automated Development Setup (Recommended)
```bash
# Run the automated setup script
./scripts/setup-dev.sh
```
This script will:
- Verify prerequisites (Java 17+, Python 3.8+)
- Install backend dependencies
- Set up Git hooks for automatic validation
- Run initial build health check
- Build debug APK
- Test backend startup

#### Manual Setup (Alternative)
If you prefer manual setup, follow the bootstrap commands below.

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
./gradlew verifyRoborazziDebug    # NEVER CANCEL: ~30 seconds, timeout: 5+ minutes

# Record new screenshot baselines (when UI changes are made)
./gradlew recordRoborazziDebug    # NEVER CANCEL: ~30 seconds, timeout: 5+ minutes

# Build instrumented test APK (for integration/UI tests)
./gradlew :app:assembleDebugAndroidTest    # NEVER CANCEL: ~1-2 minutes, timeout: 10+ minutes

# Run instrumented tests (requires Android emulator/device + backend running)
./gradlew connectedDebugAndroidTest    # NEVER CANCEL: ~2-5 minutes, timeout: 15+ minutes
# OR use the alias:
./gradlew connectedCheck    # NEVER CANCEL: ~2-5 minutes, timeout: 15+ minutes

# Generate coverage report
./gradlew :app:koverXmlReportDebug    # Fast: ~2 seconds

# Complete CI pipeline (run all checks)
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:koverXmlReportDebug detekt
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
| `./gradlew :app:assembleDebugAndroidTest` | 1-2 minutes | 10-30 seconds | 10+ minutes |
| `./gradlew connectedCheck` | 2-5 minutes | 2-5 minutes | 15+ minutes |
| Backend tests | <1 second | <1 second | 2 minutes |

**Important**: Gradle builds are much faster on subsequent runs due to incremental compilation and caching. The first build in a clean environment will take the longest time.

## Validation and Testing

### Critical Pre-Commit Validation Pipeline
**ALWAYS run these commands before committing ANY changes to ensure all GitHub Actions checks pass:**

#### Option 1: Use the Automated Validation Script (Recommended)
```bash
# Run the comprehensive validation script
./scripts/validate-pr.sh
```
This script automatically runs all required checks and fixes common issues.

#### Option 2: Manual Validation Steps
```bash
# 1. Complete validation pipeline (ensure everything works)
./gradlew buildHealth
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:verifyRoborazziDebug :app:koverXmlReportDebug detekt

# 2. Start backend for integration testing
./backend/scripts/dev.sh &
curl http://localhost:5000/health    # Verify backend is running

# 3. Run backend unit tests
cd backend/image && python3 -m unittest discover && cd ../..

# 4. Auto-fix code style issues
./gradlew detekt --auto-correct

# 5. Verify all files end with newlines (critical for CI)
find . \( -path "./app/build" -o -path "./build" -o -path "./.gradle" \) -prune -o \( -name "*.kt" -o -name "*.kts" -o -name "*.yaml" -o -name "*.yml" -o -name "*.py" -o -name "*.md" -o -name "*.json" \) -exec sh -c 'if [ "$(tail -c1 "{}" | wc -l)" -eq 0 ]; then echo "Missing newline: {}"; echo "" >> "{}"; fi' \;

# 6. Run instrumented tests (optional, requires Android emulator/device)
adb devices    # Check if emulator/device connected
./gradlew connectedCheck    # Run if device available (backend must be running)
```

### GitHub Actions Validation Requirements
The following checks MUST pass for every PR (these match the GitHub Actions workflows exactly):

#### Main Test Workflow (`test.yaml`)
1. **Build Health**: `./gradlew buildHealth`
2. **Debug Build + Tests**: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:koverXmlReportDebug detekt`
3. **Code Coverage**: Minimum 10% overall, 80% for changed files
4. **Instrumented Tests**: `./gradlew connectedCheck` - Requires Android emulator/device + backend on localhost:5000
5. **Backend Tests**: `cd backend/image && python3 -m unittest discover`

#### Code Quality Requirements
1. **Detekt**: No code quality violations (use `--auto-correct` to fix)
2. **Final Newlines**: ALL files must end with a newline character
3. **Screenshot Tests**: `./gradlew :app:verifyRoborazziDebug` must pass
4. **Build Warnings**: Treated as errors (`allWarningsAsErrors = true`)
5. **Commit Format**: Must follow conventional commit format (feat:, fix:, docs:, etc.)

### Pre-Code-Generation Checklist
**Before generating any code, ALWAYS:**

1. **Read existing code structure** and follow established patterns
2. **Check test coverage requirements** - aim for 80%+ on new/changed files
3. **Follow naming conventions** per detekt configuration
4. **Ensure imports are organized** (detekt will auto-fix)
5. **Add unit tests** for all new functionality
6. **Update screenshot tests** if UI changes are made
7. **Verify backend endpoints** exist if integrating with API

### Mandatory Coding Practices for GitHub Actions Success

#### Code Quality Requirements (Detekt)
1. **Follow Kotlin style guide** - Use conventional naming (camelCase for functions/variables, PascalCase for classes)
2. **Line length limit**: Maximum 180 characters per line
3. **Function length**: Maximum 90 lines per function (excluding `@Composable` functions)
4. **Class size**: Maximum 600 lines per class
5. **Method parameters**: Maximum 8 parameters (excluding `@Composable` functions)
6. **Complexity**: Maximum 4 conditions in complex conditional statements
7. **No unused imports** - Detekt will auto-remove them
8. **Final newlines required** - All files must end with a newline character

#### Test Coverage Requirements
1. **Minimum 10% overall coverage** - Project must maintain at least 10% code coverage
2. **Minimum 80% coverage for changed files** - Any file you modify must have 80% coverage
3. **Unit tests required** for all new business logic, data classes, and utility functions
4. **Mock external dependencies** using MockK in tests
5. **Test naming convention**: `should_expectedBehavior_when_condition()`

#### UI Testing Requirements (Screenshot Tests)
1. **Update screenshots** when UI components change: `./gradlew :app:recordRoborazziDebug`
2. **Verify screenshots** before committing: `./gradlew :app:verifyRoborazziDebug`
3. **Composable previews** must be properly annotated with `@Preview`
4. **Private previews excluded** from screenshot generation

#### Instrumented Testing Requirements (Integration/UI Tests)
1. **Build instrumented test APK**: `./gradlew :app:assembleDebugAndroidTest`
2. **Run instrumented tests**: `./gradlew connectedCheck` or `./gradlew connectedDebugAndroidTest`
3. **Prerequisites for instrumented tests**:
   - **Android emulator running** OR **physical Android device connected via ADB**
   - **Backend server running** on localhost:5000 (`./backend/scripts/dev.sh`)
   - **Valid test credentials** (automatically configured in debug builds)
4. **Instrumented test locations**: `app/src/androidTest/java/`
5. **Key instrumented tests**:
   - `SmokeTest.kt`: Tests recipe creation with and without backend sync
   - `RecipeServiceWrapperTest.kt`: Tests backend integration
6. **Emulator setup for local testing**:
   ```bash
   # Create and start an emulator (example)
   $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd -n test_emulator -k "system-images;android-28;default;x86_64"
   $ANDROID_HOME/emulator/emulator -avd test_emulator -no-window -no-audio &
   
   # Wait for emulator to boot, then run tests
   adb wait-for-device
   ./gradlew connectedCheck
   ```

#### File and Commit Requirements
1. **File endings**: All `.kt`, `.kts`, `.yaml`, `.yml`, `.py`, `.md`, `.json` files must end with newline
2. **Conventional commits**: Use format `type(scope): description` where type is one of:
   - `feat`: New feature
   - `fix`: Bug fix
   - `docs`: Documentation changes
   - `style`: Code style changes
   - `refactor`: Code refactoring
   - `test`: Adding tests
   - `chore`: Maintenance tasks
   - `ci`: CI/CD changes
   - `build`: Build system changes
   - `perf`: Performance improvements
   - `revert`: Reverting changes

#### Build Requirements
1. **No build warnings** - All warnings are treated as errors (`allWarningsAsErrors = true`)
2. **Build health check** must pass - `./gradlew buildHealth`
3. **Gradle daemon** - Build system must be healthy and free of configuration issues

### Post-Code-Generation Validation
**After generating code, ALWAYS:**

1. **Run validation script**: `./scripts/validate-pr.sh` (recommended)
2. **OR run manual checks**:
   - **Run detekt auto-correct**: `./gradlew detekt --auto-correct`
   - **Add missing final newlines**: Use the find command above
   - **Run unit tests**: `./gradlew :app:testDebugUnitTest`
   - **Update screenshots if needed**: `./gradlew :app:recordRoborazziDebug` (if UI changed)
   - **Verify coverage**: `./gradlew :app:koverXmlReportDebug`
   - **Check build**: `./gradlew :app:assembleDebug`
3. **Verify backend integration** if applicable:
   - **Start backend**: `./backend/scripts/dev.sh`
   - **Test health endpoint**: `curl http://localhost:5000/health`
   - **Run backend tests**: `cd backend/image && python3 -m unittest discover`
4. **Run instrumented tests** (for UI/integration changes):
   - **Ensure Android emulator/device is connected**: `adb devices`
   - **Start backend**: `./backend/scripts/dev.sh` (must be running for tests)
   - **Run instrumented tests**: `./gradlew connectedCheck`

### Critical: Instrumented Tests Must Pass
**IMPORTANT**: Work is NOT complete until both unit tests AND instrumented tests pass. The GitHub Actions CI runs instrumented tests on multiple Android API levels (28, 31, 34, 35) and they must all pass for the PR to be approved.

**When you don't have local Android emulator access:**
1. ✅ Ensure all unit tests pass completely
2. ✅ Verify backend integration is working (backend starts and responds to health checks)
3. ✅ Confirm no UI regressions (screenshot tests pass)
4. ✅ Run `./scripts/validate-pr.sh` and address all issues
5. ✅ The PR will be validated by GitHub Actions instrumented tests automatically
6. ❌ **DO NOT** mark work as complete until GitHub Actions shows green checkmarks for instrumented tests

**When you have local Android emulator access:**
1. Set up and start an Android emulator or connect a physical device
2. Run `./scripts/validate-pr.sh` and choose "Yes" when prompted for instrumented tests
3. Alternatively, run manually: `./gradlew connectedCheck` (with backend running)
4. All tests (unit + instrumented) must pass before work is considered complete

### Manual Testing Scenarios
After making changes, always test these scenarios:
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
4. **Run full instrumented test suite**:
   ```bash
   # Start backend
   ./backend/scripts/dev.sh &
   
   # Verify backend is healthy
   curl http://localhost:5000/health
   
   # Ensure emulator/device is connected
   adb devices
   
   # Run instrumented tests
   ./gradlew connectedCheck
   ```
5. **Instrumented tests validate**:
   - App startup and basic navigation
   - Recipe creation without backend sync
   - Backend login and authentication
   - Recipe creation with backend sync
   - Data persistence and synchronization

#### CI/CD Pipeline Validation
The GitHub Actions workflow runs these exact commands:
```bash
./gradlew buildHealth
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:verifyRoborazziDebug :app:koverXmlReportDebug detekt
./gradlew connectedCheck  # Runs on Android API levels 28, 31, 34, 35
```
Always run these locally before pushing to ensure CI will pass.

**Note**: Instrumented tests (`connectedCheck`) run automatically in GitHub Actions with Android emulators, but require manual setup for local testing.

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

### Validation Failures and Fixes

#### Code Quality (Detekt) Failures
```bash
# Auto-fix most issues
./gradlew detekt --auto-correct

# Common fixes:
# - Line too long: Break into multiple lines (max 180 chars)
# - Function too long: Split into smaller functions (max 90 lines, excluding @Composable)
# - Too many parameters: Use data classes or reduce parameters (max 8, excluding @Composable)
```

#### Test Coverage Failures
```bash
# Check current coverage
./gradlew :app:koverXmlReportDebug

# Add unit tests for:
# - All new business logic functions
# - Data classes
# - Utility functions
# Aim for 80% coverage on changed files, 10% minimum overall
```

#### Screenshot Test Failures
```bash
# Update screenshots after UI changes
./gradlew :app:recordRoborazziDebug

# Verify screenshots
./gradlew :app:verifyRoborazziDebug

# Note: Only @Preview composables in presentation packages are tested
```

#### Instrumented Test Failures
```bash
# Check if emulator/device is connected
adb devices

# Start backend (required for integration tests)
./backend/scripts/dev.sh &
curl http://localhost:5000/health  # Should return "OK"

# Build instrumented test APK first
./gradlew :app:assembleDebugAndroidTest

# Run specific instrumented test
./gradlew connectedDebugAndroidTest --tests="*SmokeTest*"

# Run all instrumented tests
./gradlew connectedCheck

# Common issues:
# - No emulator/device connected: Install Android Studio and create an AVD
# - Backend not running: Start with ./backend/scripts/dev.sh
# - Test timeout: Increase timeout or check emulator performance
# - Network issues: Ensure emulator can reach localhost:5000 (10.0.2.2:5000 from emulator)
```

#### Missing Final Newlines
```bash
# Auto-fix missing newlines (excluding build directories)
find . \( -path "./app/build" -o -path "./build" -o -path "./.gradle" \) -prune -o \( -name "*.kt" -o -name "*.kts" -o -name "*.yaml" -o -name "*.yml" -o -name "*.py" -o -name "*.md" -o -name "*.json" \) -exec sh -c 'if [ "$(tail -c1 "{}" | wc -l)" -eq 0 ]; then echo "" >> "{}"; fi' \;
```

#### Build Warnings (Treated as Errors)
- All warnings are treated as errors due to `allWarningsAsErrors = true`
- Fix any deprecation warnings immediately
- Use `@Suppress` annotations only as a last resort

#### Commit Message Format Errors
```bash
# Use conventional commit format:
feat: add user authentication
fix(ui): resolve button alignment issue
docs: update README with setup instructions

# Valid types: feat, fix, docs, style, refactor, test, chore, ci, build, perf, revert
```

### Development Workflow
- **Backend Server**: Use `./backend/scripts/dev.sh` for direct Flask development
- **API Testing**: Most endpoints require authentication; use `/health` for basic connectivity testing
- **Android Development**: Debug builds include test credentials for backend connectivity
- **Code Quality**: Always run `detekt` before committing; use auto-correct: `./gradlew detekt --auto-correct`
- **Commit Messages**: Follow conventional commit format for automated versioning

## Architecture Notes
- **Android App**: Built with Kotlin, Jetpack Compose, Room database, Ktor HTTP client
- **Backend**: Python Flask with YAML file storage, JWT authentication
- **Communication**: REST API between Android app and backend
- **Testing**: Unit tests, screenshot tests, instrumented tests requiring backend
- **Build System**: Gradle for Android, pip for Python, Docker for backend deployment
