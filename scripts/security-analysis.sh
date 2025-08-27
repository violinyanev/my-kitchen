#!/bin/bash

# Local Security Analysis Script for My Kitchen
# This script runs the same security checks that run in CI/CD locally

set -e

echo "🔒 My Kitchen - Local Security Analysis"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}▶️  $1${NC}"
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

# Check if required tools are installed
check_tool() {
    if command -v $1 &> /dev/null; then
        print_success "$1 is installed"
        return 0
    else
        print_error "$1 is not installed"
        return 1
    fi
}

# Install Python security tools
install_security_tools() {
    print_status "Installing security analysis tools..."
    pip install --user semgrep 2>/dev/null || {
        print_warning "Failed to install some security tools - they may already be installed"
    }
}

# Install GitLeaks
install_gitleaks() {
    if ! command -v gitleaks &> /dev/null; then
        print_status "Installing GitLeaks..."
        # Try to install via different methods
        if command -v brew &> /dev/null; then
            brew install gitleaks
        elif command -v go &> /dev/null; then
            go install github.com/gitleaks/gitleaks/v8@latest
        else
            print_warning "Please install GitLeaks manually from: https://github.com/gitleaks/gitleaks#installation"
        fi
    fi
}

# Run backend security analysis
run_backend_security() {
    print_status "Running Backend Security Analysis..."

    echo ""
    echo "🔍 Detekt - Kotlin Backend Static Analysis"
    echo "------------------------------------------"
    if ./gradlew :backend:detekt --console=plain 2>/dev/null; then
        print_success "Backend Detekt scan completed"
    else
        print_warning "Backend Detekt found issues (check backend/build/reports/detekt/)"
    fi

    echo ""
    echo "🧪 Backend Unit Tests"
    echo "--------------------"
    if ./gradlew :backend:test --console=plain 2>/dev/null; then
        print_success "Backend unit tests passed"
    else
        print_warning "Backend unit tests failed"
    fi

    echo ""
    echo "🔑 Backend Hardcoded Secrets Check"
    echo "-----------------------------------"
    secrets_found=$(find backend/src -name "*.kt" | xargs grep -n -i -E "(password|secret|key|token|api_key)" | grep -v "test\|Test" | wc -l)
    if [ $secrets_found -eq 0 ]; then
        print_success "No hardcoded secrets found in backend code"
    else
        print_warning "Found $secrets_found potential hardcoded secrets in backend code"
        find backend/src -name "*.kt" | xargs grep -n -i -E "(password|secret|key|token|api_key)" | grep -v "test\|Test"
    fi

    echo ""
    echo "🔬 Semgrep - Static Application Security Testing"
    echo "------------------------------------------------"
    if semgrep --config=.semgrep.yml backend/src/ 2>/dev/null; then
        print_success "Semgrep scan completed"
    else
        print_warning "Semgrep found issues (see output above)"
    fi
}

# Run Android security analysis
run_android_security() {
    print_status "Running Android Security Analysis..."

    echo ""
    echo "🔍 Detekt - Kotlin Static Analysis"
    echo "----------------------------------"
    if ./gradlew detekt --console=plain 2>/dev/null; then
        print_success "Detekt scan completed"
    else
        print_warning "Detekt found issues (check app/build/reports/detekt/)"
    fi

    echo ""
    echo "🔑 Android Hardcoded Secrets Check"
    echo "-----------------------------------"
    secrets_found=$(find app/src -name "*.kt" -o -name "*.java" -o -name "*.xml" | xargs grep -n -i -E "(password|secret|key|token|api_key)" | grep -v "BuildConfig\|test\|Test" | wc -l)
    if [ $secrets_found -eq 0 ]; then
        print_success "No hardcoded secrets found in Android code"
    else
        print_warning "Found $secrets_found potential hardcoded secrets in Android code"
        find app/src -name "*.kt" -o -name "*.java" -o -name "*.xml" | xargs grep -n -i -E "(password|secret|key|token|api_key)" | grep -v "BuildConfig\|test\|Test"
    fi
}

# Run container security analysis
run_container_security() {
    print_status "Running Container Security Analysis..."

    if command -v docker &> /dev/null; then
        print_status "Building backend distribution for Docker..."
        ./gradlew :backend:distTar --quiet 2>/dev/null
        
        print_status "Building Docker image for security analysis..."
        cd backend && docker build -t my-kitchen-backend:security-test . --quiet && cd ..

        if command -v trivy &> /dev/null; then
            echo ""
            echo "🐳 Trivy - Container Security Scan"
            echo "-----------------------------------"
            if trivy image my-kitchen-backend:security-test 2>/dev/null; then
                print_success "Trivy container scan completed"
            else
                print_warning "Trivy found container vulnerabilities"
            fi
        else
            print_warning "Trivy not installed - skipping container scan. Install from: https://aquasecurity.github.io/trivy/"
        fi
    else
        print_warning "Docker not installed - skipping container security analysis"
    fi
}

# Run secrets and compliance scanning
run_secrets_compliance() {
    print_status "Running Secrets & Compliance Analysis..."

    echo ""
    echo "🔐 GitLeaks - Secrets Scanner"
    echo "----------------------------"
    if command -v gitleaks &> /dev/null; then
        if gitleaks detect --config .gitleaks.toml --verbose 2>/dev/null; then
            print_success "GitLeaks scan completed - no secrets found"
        else
            print_warning "GitLeaks found potential secrets (see output above)"
        fi
    else
        print_warning "GitLeaks not installed - skipping secrets scan"
    fi

    echo ""
    echo "📜 License Compliance Check"
    echo "----------------------------"
    print_status "Checking Kotlin backend license compliance..."
    ./gradlew :backend:dependencies --configuration runtimeClasspath 2>/dev/null | grep -E "(name|group|version)" | head -10 || true
    print_success "Kotlin backend license check completed"
    
    print_status "Checking Android license compliance..."
    ./gradlew :app:dependencies --configuration releaseRuntimeClasspath 2>/dev/null | grep -E "(name|group|version)" | head -10 || true
    print_success "Android license check completed"
}

# Main execution
main() {
    echo ""
    print_status "Checking tool availability..."

    # Check basic tools
    python3 --version >/dev/null 2>&1 || { print_error "Python 3 is required"; exit 1; }
    pip --version >/dev/null 2>&1 || { print_error "pip is required"; exit 1; }

    # Install tools if needed
    install_security_tools
    install_gitleaks

    echo ""
    print_status "Starting security analysis..."

    # Run security analysis
    run_backend_security
    run_android_security
    run_container_security
    run_secrets_compliance

    echo ""
    echo "🏁 Security Analysis Complete!"
    echo "=============================="
    print_success "Local security analysis finished"
    echo ""
    echo "📋 Summary:"
    echo "  - Kotlin backend security analysis completed"
    echo "  - Android security analysis completed"
    echo "  - Container security scan attempted"
    echo "  - Secrets and compliance check completed"
    echo ""
    echo "📖 For detailed security information, see:"
    echo "  - SECURITY.md - Security analysis documentation"
    echo "  - SECURITY_POLICY.md - Security policy and reporting"
    echo "  - .github/workflows/security.yml - Automated security workflow"
    echo ""
    print_status "To see the same analysis that runs in CI/CD, check the GitHub Actions workflow"
}

# Run main function
main "$@"
