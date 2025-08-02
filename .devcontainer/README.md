# My Kitchen Development Environment

This development container provides a complete environment for GitHub Copilot agent development with all dependencies cached for optimal performance.

## Features

- **Java 17** (Temurin distribution) - same as production workflow
- **Gradle 8.14+** with full caching support
- **Python 3.13** with pip caching
- **Android SDK** with API levels 28, 31, 34, 35
- **Flask backend** ready for local development
- **GitHub Copilot** extensions pre-installed

## Cached Dependencies

The container automatically caches:
- Gradle dependencies (`~/.gradle`)
- Android SDK components (`~/.android`) 
- Python pip packages (`~/.cache/pip`)
- Maven dependencies (`~/.m2`)

## Quick Start

1. **Open in VS Code**: The container will automatically build when you open the project
2. **Wait for setup**: Dependencies will install automatically via `postCreateCommand`
3. **Validate environment**: Run `.devcontainer/setup-dev.sh` to test all components

## Development Workflow

### Android App Development
```bash
# Build health check
./gradlew buildHealth

# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run all checks (lint, tests, coverage)
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:validateDebugScreenshotTest :app:koverXmlReportDebug detekt
```

### Backend Development
```bash
# Install Python dependencies
pip install -r backend/image/requirements.txt

# Start Flask server
export RECIPES_SECRET_KEY=TestKey
python3 backend/image/app.py backend/seed_data

# Test health endpoint
curl http://localhost:5000/health
```

## Environment Alignment

This development container mirrors the exact same setup as the GitHub Actions workflow (`.github/workflows/test.yaml`):
- Same Java version and distribution
- Same Python version  
- Same Android SDK API levels
- Same build commands and environment variables

## GitHub Copilot Integration

The container includes:
- GitHub Copilot extension
- GitHub Copilot Chat extension
- Optimized for AI-assisted development workflows

This enables consistent AI development experience with cached dependencies for faster iteration.