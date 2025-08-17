# My Kitchen Development Quick Reference

## 🚀 Quick Setup
```bash
./scripts/setup-dev.sh
```

## 🔄 Daily Workflow

### Before Making Changes
```bash
# Start backend for testing
./backend/scripts/dev.sh &

# Build current state
./gradlew :app:assembleDebug
```

### After Making Changes
```bash
# Comprehensive validation (ensures PR will pass)
./scripts/validate-pr.sh

# Or run individual checks:
./gradlew detekt --auto-correct    # Fix code style
./gradlew :app:testDebugUnitTest   # Unit tests
./gradlew :app:verifyRoborazziDebug # Screenshot tests
```

### UI Changes
```bash
# Update screenshots after UI changes
./gradlew :app:recordRoborazziDebug

# Verify screenshots
./gradlew :app:verifyRoborazziDebug
```

## 📝 Commit Format
```bash
# Use conventional commit format:
git commit -m "feat: add user authentication"
git commit -m "fix(ui): resolve button alignment issue"
git commit -m "docs: update README with setup instructions"

# Valid types: feat, fix, docs, style, refactor, test, chore, ci, build, perf, revert
```

## ✅ Required Checks (All Must Pass)
- ✅ Build health: `./gradlew buildHealth`
- ✅ Unit tests: `./gradlew :app:testDebugUnitTest`
- ✅ Code coverage: 80% on changed files, 10% overall minimum
- ✅ Code quality: `./gradlew detekt` (no violations)
- ✅ Screenshot tests: `./gradlew :app:verifyRoborazziDebug`
- ✅ Final newlines: All files must end with newline
- ✅ Backend tests: `cd backend/image && python3 -m unittest discover`
- ✅ Conventional commits: Proper format enforced

## 🛠️ Common Fixes

### Code Quality Issues
```bash
./gradlew detekt --auto-correct
```

### Missing Newlines
```bash
find . -name "*.kt" -o -name "*.kts" -o -name "*.yaml" -o -name "*.yml" -o -name "*.py" -o -name "*.md" -o -name "*.json" | \
xargs -I {} sh -c 'if [ "$(tail -c1 "{}" | wc -l)" -eq 0 ]; then echo "" >> "{}"; fi'
```

### Coverage Too Low
- Add unit tests for new functionality
- Aim for 80% coverage on changed files
- Use MockK for mocking external dependencies

## 🔍 Debugging
```bash
# Verbose test output
./gradlew :app:testDebugUnitTest --info

# Coverage report location
./gradlew :app:koverHtmlReportDebug
# Open: app/build/reports/kover/htmlDebug/index.html

# Detekt report location  
./gradlew detekt
# Open: build/reports/detekt/detekt.html
```

## 📱 Android Development
```bash
# Debug build (with test credentials)
./gradlew :app:assembleDebug

# Release build (requires keystore)
./gradlew :app:assembleRelease

# Install on device
./gradlew :app:installDebug
```

## 🖥️ Backend Development
```bash
# Start development server
./backend/scripts/dev.sh

# Test health endpoint
curl http://localhost:5000/health

# Run backend tests
cd backend/image && python3 -m unittest discover
```

## 🚨 CI/CD Troubleshooting

If GitHub Actions fail:
1. Run `./scripts/validate-pr.sh` locally
2. Fix any issues found
3. Commit and push again

Common issues:
- Missing final newlines in files
- Code quality violations (detekt)
- Low test coverage
- Screenshot test mismatches
- Backend integration test failures
