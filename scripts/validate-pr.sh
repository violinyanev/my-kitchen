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
done < <(find . -name "*.kt" -o -name "*.kts" -o -name "*.yaml" -o -name "*.yml" -o -name "*.py" -o -name "*.md" -o -name "*.json" -print0)

if [ $MISSING_NEWLINES -eq 0 ]; then
    print_success "All files have proper final newlines"
else
    print_warning "Added final newlines to $MISSING_NEWLINES files"
fi

# Step 4: Run main Android checks
print_step "Running Android build, tests, coverage, and quality checks..."
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:verifyRoborazziDebug :app:koverXmlReportDebug detekt
print_success "Android checks passed"

# Step 5: Backend tests
print_step "Running backend unit tests..."
if [ -d "backend/image" ]; then
    (cd backend/image && python3 -m unittest discover)
    print_success "Backend tests passed"
else
    print_warning "Backend directory not found, skipping backend tests"
fi

# Step 6: Check if backend can start (optional - for integration tests)
print_step "Checking if backend can start (for integration tests)..."
if [ -f "backend/scripts/dev.sh" ]; then
    # Start backend in background
    ./backend/scripts/dev.sh &
    BACKEND_PID=$!

    # Wait a moment for startup
    sleep 5

    # Check health endpoint
    if curl -f --silent http://localhost:5000/health > /dev/null; then
        print_success "Backend health check passed"
    else
        print_warning "Backend health check failed - integration tests may fail"
    fi

    # Kill backend
    kill $BACKEND_PID 2>/dev/null || true
    wait $BACKEND_PID 2>/dev/null || true
else
    print_warning "Backend startup script not found"
fi

# Step 7: Check commit message format (if this is a git commit)
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
