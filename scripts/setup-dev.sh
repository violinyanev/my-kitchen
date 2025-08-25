#!/bin/bash

# Setup script for My Kitchen development environment
# This script sets up all necessary tools and hooks for development

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

echo "🚀 Setting up My Kitchen development environment..."
echo "=================================================="

# Check if we're in the right directory
if [ ! -f "gradlew" ]; then
    print_error "This script must be run from the project root directory"
    exit 1
fi

# Check prerequisites
print_step "Checking prerequisites..."

# Java version check
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        print_success "Java $JAVA_VERSION found"
    else
        print_error "Java 17+ required, found Java $JAVA_VERSION"
        echo "Install OpenJDK Temurin 17: https://adoptium.net/"
        exit 1
    fi
else
    print_error "Java not found. Install OpenJDK Temurin 17: https://adoptium.net/"
    exit 1
fi

# Python version check
if command -v python3 >/dev/null 2>&1; then
    PYTHON_VERSION=$(python3 --version | awk '{print $2}' | cut -d'.' -f1,2)
    if python3 -c "import sys; exit(0 if sys.version_info >= (3, 8) else 1)"; then
        print_success "Python $PYTHON_VERSION found"
    else
        print_error "Python 3.8+ required, found Python $PYTHON_VERSION"
        exit 1
    fi
else
    print_error "Python 3 not found. Install Python 3.8+: https://www.python.org/"
    exit 1
fi

# Install backend dependencies
print_step "Installing backend dependencies..."
if [ -f "backend/image/requirements.txt" ]; then
    python3 -m pip install -r backend/image/requirements.txt
    print_success "Backend dependencies installed"
else
    print_warning "Backend requirements.txt not found"
fi

# Setup Git hooks
print_step "Setting up Git hooks..."
if [ -d ".git/hooks" ]; then
    # Install pre-commit hook
    cp scripts/pre-commit-hook.sh .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    print_success "Pre-commit hook installed"

    # Install commit-msg hook for message validation
    cat > .git/hooks/commit-msg << 'EOF'
#!/bin/bash
# Validate commit message format
exec ./scripts/pre-commit-hook.sh "$1"
EOF
    chmod +x .git/hooks/commit-msg
    print_success "Commit message validation hook installed"
else
    print_warning "Not a Git repository - hooks not installed"
fi

# Run initial health check
print_step "Running initial build health check..."
print_warning "This may take 3-4 minutes on first run..."
./gradlew buildHealth
print_success "Build health check passed"

# Run initial build
print_step "Building debug APK (first build may take 7+ minutes)..."
print_warning "Please be patient - subsequent builds will be much faster..."
./gradlew :app:assembleDebug
print_success "Debug APK built successfully"

# Test backend
print_step "Testing backend startup..."
if [ -f "backend/scripts/dev.sh" ]; then
    # Start backend in background
    ./backend/scripts/dev.sh &
    BACKEND_PID=$!

    # Wait for startup
    sleep 5

    # Test health endpoint
    if curl -f --silent http://localhost:5000/health > /dev/null; then
        print_success "Backend health check passed"
    else
        print_warning "Backend health check failed"
    fi

    # Kill backend
    kill $BACKEND_PID 2>/dev/null || true
    wait $BACKEND_PID 2>/dev/null || true
else
    print_warning "Backend startup script not found"
fi

echo ""
echo "=================================================="
print_success "My Kitchen development environment setup complete!"
echo ""
print_step "Next steps:"
echo "1. Start developing with confidence - hooks will catch issues early"
echo "2. Before any commit, the pre-commit hook will run essential checks"
echo "3. For full validation before pushing, run: ./scripts/validate-pr.sh"
echo "4. Start the backend for development: ./backend/scripts/dev.sh"
echo "5. Build debug APK: ./gradlew :app:assembleDebug"
echo ""
print_step "Key commands:"
echo "• Full validation: ./scripts/validate-pr.sh"
echo "• Start backend: ./backend/scripts/dev.sh"
echo "• Build APK: ./gradlew :app:assembleDebug"
echo "• Run tests: ./gradlew :app:testDebugUnitTest"
echo "• Code quality: ./gradlew detekt --auto-correct"
echo ""
print_success "Happy coding! 🎉"
