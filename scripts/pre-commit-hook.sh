#!/bin/bash

# Pre-commit hook for My Kitchen
# This hook runs essential checks before allowing a commit
# Install by copying to .git/hooks/pre-commit and making executable

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${BLUE}🔧 $1${NC}"
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

echo "🔍 Running pre-commit checks for My Kitchen..."
echo "=============================================="

# Check if we're in the right directory
if [ ! -f "gradlew" ]; then
    print_error "Git hook must be run from project root"
    exit 1
fi

# Auto-fix code style issues
print_step "Auto-fixing code style issues..."
./gradlew detekt --auto-correct --quiet || {
    print_error "Detekt auto-correct failed"
    exit 1
}

# Add missing final newlines to staged files
print_step "Checking final newlines in staged files..."
MISSING_NEWLINES=0
git diff --cached --name-only --diff-filter=ACM | while read file; do
    if [[ "$file" =~ \.(kt|kts|yaml|yml|py|md|json)$ ]]; then
        if [ "$(tail -c1 "$file" 2>/dev/null | wc -l)" -eq 0 ]; then
            echo "Adding newline to: $file"
            echo "" >> "$file"
            git add "$file"
            ((MISSING_NEWLINES++))
        fi
    fi
done

# Run quick unit tests on changed files
print_step "Running unit tests..."
./gradlew :app:testDebugUnitTest --quiet || {
    print_error "Unit tests failed"
    echo ""
    echo "Fix failing tests before committing."
    echo "Run './gradlew :app:testDebugUnitTest' for detailed output."
    exit 1
}

# Check commit message format
if [ -n "$1" ]; then  # $1 is the commit message file when called as commit-msg hook
    COMMIT_MSG=$(cat "$1")
else
    COMMIT_MSG="dummy message"  # For manual testing
fi

if ! echo "$COMMIT_MSG" | grep -qE "^(feat|fix|docs|style|refactor|test|chore|ci|build|perf|revert)(\(.+\))?: "; then
    print_error "Commit message must follow conventional commit format"
    echo ""
    echo "Format: type(scope): description"
    echo "Examples:"
    echo "  feat: add user authentication"
    echo "  fix(ui): resolve button alignment issue"
    echo "  docs: update README with setup instructions"
    echo ""
    echo "Valid types: feat, fix, docs, style, refactor, test, chore, ci, build, perf, revert"
    exit 1
fi

print_success "Pre-commit checks passed!"
echo ""
echo "Note: Full validation (including build health, coverage, and screenshot tests)"
echo "will run in CI. For complete local validation, run: ./scripts/validate-pr.sh"
echo ""
