# GitHub Agent Quick Reference

This is a quick reference guide for GitHub coding agents working on the My Kitchen project.

## One-Line Setup

```bash
curl -fsSL https://raw.githubusercontent.com/violinyanev/my-kitchen/main/setup-github-agent.sh | bash
```

## Manual Setup

1. **Clone and navigate to repository:**
   ```bash
   git clone https://github.com/violinyanev/my-kitchen.git
   cd my-kitchen
   ./setup-github-agent.sh
   ```

## Key Commands

### Build and Test
```bash
# Health check
./gradlew buildHealth

# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Code quality check
./gradlew detekt

# Code coverage
./gradlew :app:koverXmlReportDebug

# Screenshot tests
./gradlew :app:validateDebugScreenshotTest
```

### Instrumentation Tests
```bash
# Start backend server
export RECIPES_SECRET_KEY=TestKey
python3 backend/image/app.py backend/seed_data &

# Wait and verify backend
sleep 5 && curl --fail http://localhost:5000/health

# Run instrumentation tests
./gradlew connectedCheck
```

## Project Structure
```
my-kitchen/
├── app/                    # Android Kotlin app
│   ├── src/
│   └── build.gradle.kts
├── backend/                # Python Flask server
│   ├── image/
│   └── scripts/
├── .github/workflows/      # CI/CD workflows
└── gradle/                 # Gradle configuration
```

## Environment Requirements
- **Java**: 17 (Temurin)
- **Android**: API 28-35, Build Tools 34.0.0
- **Python**: 3.13
- **Gradle**: 8.14.3 (via wrapper)

## Common Issues
1. **Build fails**: Check Java 17 and ANDROID_HOME
2. **Emulator issues**: Verify KVM and system resources
3. **Backend tests fail**: Ensure Flask server is running
4. **Permission errors**: Check Android SDK permissions

## Files Created by Setup
- `github-agent-setup.md` - Detailed setup documentation
- `setup-github-agent.sh` - Automated setup script
- `agent-quick-reference.md` - This file

For complete documentation, see `github-agent-setup.md`.
