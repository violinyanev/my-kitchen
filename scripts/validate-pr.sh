#!/bin/bash

# Pre-commit validation script for My Kitchen
# This script runs all checks that GitHub Actions will run, ensuring PR will pass

set -e  # Exit on any error

echo "🔍 Starting My Kitchen Pre-Commit Validation..."
echo "==============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${BLUE}📋 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if we're in the right directory
if [ ! -f "gradlew" ]; then
    print_error "This script must be run from the project root directory"
    exit 1
fi

# Step 1: Build Health Check
print_step "Running build health check..."
./gradlew buildHealth
print_success "Build health check passed"

# Step 2: Auto-fix code style issues first
print_step "Auto-fixing code style issues..."
./gradlew detekt --auto-correct
print_success "Code style auto-fixes applied"

# Step 3: Add missing final newlines
print_step "Checking and adding missing final newlines..."
MISSING_NEWLINES=0
while IFS= read -r -d '' file; do
    if [ "$(tail -c1 "$file" | wc -l)" -eq 0 ]; then
        echo "Adding newline to: $file"
        echo "" >> "$file"
        ((MISSING_NEWLINES++))
    fi
done < <(find . \( -path "./app/build" -o -path "./build" -o -path "./.gradle" -o -path "./backend/.venv" \) -prune -o \( -name "*.kt" -o -name "*.kts" -o -name "*.yaml" -o -name "*.yml" -o -name "*.py" -o -name "*.md" -o -name "*.json" \) -print0)

if [ $MISSING_NEWLINES -eq 0 ]; then
    print_success "All files have proper final newlines"
else
    print_warning "Added final newlines to $MISSING_NEWLINES files"
fi

# Step 4: Run main Android checks
print_step "Running Android build, tests, coverage, and quality checks..."
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:verifyRoborazziDebug :app:koverXmlReportDebug detekt
print_success "Android checks passed"

# Step 4.5: Run iOS framework and tests (macOS only)
if [[ "$OSTYPE" == "darwin"* ]]; then
    print_step "Building iOS frameworks and running shared Kotlin/Native tests..."
    ./gradlew shared:linkDebugFrameworkIosX64 shared:linkDebugFrameworkIosSimulatorArm64 shared:iosX64Test shared:iosSimulatorArm64Test
    print_success "iOS framework building and Kotlin/Native tests passed"
else
    print_warning "Skipping iOS tests - only available on macOS"
    echo "           iOS tests will be validated by GitHub Actions macOS runner"
fi

# Step 5: Backend tests
print_step "Running backend unit tests..."
if [ -d "backend/image" ]; then
    (cd backend/image && python3 -m unittest discover)
    print_success "Backend tests passed"
else
    print_warning "Backend directory not found, skipping backend tests"
fi

# Step 6: Check if backend can start (required for integration tests)
print_step "Checking if backend can start (required for integration tests)..."
BACKEND_STARTED=false
if [ -f "backend/scripts/dev.sh" ]; then
    # Start backend in background
    ./backend/scripts/dev.sh &
    BACKEND_PID=$!

    # Wait a moment for startup
    sleep 5

    # Check health endpoint
    if curl -f --silent http://localhost:5000/health > /dev/null; then
        print_success "Backend health check passed - ready for integration tests"
        BACKEND_STARTED=true
    else
        print_warning "Backend health check failed - integration tests may fail"
    fi

    # Kill backend
    kill $BACKEND_PID 2>/dev/null || true
    wait $BACKEND_PID 2>/dev/null || true
else
    print_warning "Backend startup script not found"
fi

# Step 7: Optional instrumented tests (requires Android emulator/device)
print_step "Checking for instrumented tests (optional - requires emulator/device)..."
if command -v adb >/dev/null 2>&1; then
    # Check if any devices are connected
    CONNECTED_DEVICES=$(adb devices | grep -v "List of devices" | grep -c "device$" || true)
    
    if [ "$CONNECTED_DEVICES" -gt 0 ]; then
        print_step "Found $CONNECTED_DEVICES connected Android device(s)"
        
        # Ask user if they want to run instrumented tests
        echo ""
        echo "🤖 Instrumented tests can be run now (recommended for UI/integration changes)"
        echo "   This requires the backend to be running during test execution."
        echo ""
        read -p "   Run instrumented tests? [y/N]: " -n 1 -r
        echo ""
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            print_step "Starting backend and running instrumented tests..."
            
            # Start backend in background
            if [ "$BACKEND_STARTED" = false ] && [ -f "backend/scripts/dev.sh" ]; then
                ./backend/scripts/dev.sh &
                BACKEND_PID=$!
                sleep 5
                
                # Verify backend is running
                if ! curl -f --silent http://localhost:5000/health > /dev/null; then
                    print_error "Backend failed to start - cannot run instrumented tests"
                    kill $BACKEND_PID 2>/dev/null || true
                    exit 1
                fi
            fi
            
            # Build instrumented test APK
            print_step "Building instrumented test APK..."
            ./gradlew :app:assembleDebugAndroidTest
            
            # Run instrumented tests
            print_step "Running instrumented tests (this may take several minutes)..."
            ./gradlew connectedCheck
            print_success "Instrumented tests passed!"
            
            # Clean up backend
            if [ -n "${BACKEND_PID:-}" ]; then
                kill $BACKEND_PID 2>/dev/null || true
                wait $BACKEND_PID 2>/dev/null || true
            fi
        else
            print_warning "Skipping instrumented tests - will be validated by GitHub Actions CI"
            echo "           Note: GitHub Actions runs instrumented tests on API levels 28, 31, 34, 35"
        fi
    else
        print_warning "No Android devices/emulators connected - skipping instrumented tests"
        echo "           Instrumented tests will be validated by GitHub Actions CI"
        echo "           To run locally: start an emulator and run './gradlew connectedCheck'"
    fi
else
    print_warning "ADB not found - skipping instrumented tests"
    echo "           Install Android SDK to run instrumented tests locally"
    echo "           Instrumented tests will be validated by GitHub Actions CI"
fi

# Step 8: Check commit message format (if this is a git commit)
if git rev-parse --verify HEAD >/dev/null 2>&1; then
    print_step "Checking latest commit message format..."
    COMMIT_MSG=$(git log -1 --pretty=format:%s)
    if echo "$COMMIT_MSG" | grep -qE "^(feat|fix|docs|style|refactor|test|chore|ci|build|perf|revert)(\(.+\))?: "; then
        print_success "Commit message follows conventional format"
    else
        print_warning "Commit message should follow conventional commit format: type(scope): description"
        echo "Current message: $COMMIT_MSG"
        echo "Examples: feat: add user authentication, fix(ui): resolve button alignment issue"
    fi
fi

echo ""
echo "==============================================="
print_success "All validation checks completed!"
echo ""
echo "Your changes are ready for commit and push."
echo "GitHub Actions should pass with these changes."
echo ""
print_step "Next steps:"
echo "1. Review any warnings above"
echo "2. Commit your changes with a conventional commit message"
echo "3. Push to your branch"
echo ""
print_step "Note: GitHub Actions will run additional checks:"
echo "• Instrumented tests on Android API levels 28, 31, 34, 35"
echo "• Full build verification across multiple environments"
echo "• If you didn't run instrumented tests locally, they will be validated automatically"
echo ""
