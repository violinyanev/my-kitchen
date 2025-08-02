#!/bin/bash
# GitHub Agent Setup Script for My Kitchen
# This script prepares a complete development environment for GitHub coding agents
# working on the My Kitchen Android app and Python backend project.

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to detect OS
detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command_exists lsb_release; then
            OS=$(lsb_release -si)
            VERSION=$(lsb_release -sr)
        elif [ -f /etc/os-release ]; then
            . /etc/os-release
            OS=$NAME
            VERSION=$VERSION_ID
        else
            OS="Linux"
            VERSION="Unknown"
        fi
    else
        log_error "This script is designed for Linux systems only"
        exit 1
    fi
}

# Function to install Java 17
install_java() {
    log_info "Installing Java 17 (Temurin distribution)..."
    
    if command_exists java; then
        JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [[ "$JAVA_VERSION" == "17" ]]; then
            log_success "Java 17 already installed"
            return 0
        fi
    fi

    # Install Java 17 Temurin
    sudo apt-get update -qq
    sudo apt-get install -y wget apt-transport-https gnupg

    # Add Adoptium repository
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/adoptium.gpg
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list > /dev/null

    sudo apt-get update -qq
    sudo apt-get install -y temurin-17-jdk

    # Set JAVA_HOME
    export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
    echo "export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64" >> ~/.bashrc
    
    log_success "Java 17 installed successfully"
}

# Function to install Android SDK
install_android_sdk() {
    log_info "Installing Android SDK and tools..."
    
    # Create Android SDK directory
    ANDROID_HOME="$HOME/android-sdk"
    mkdir -p "$ANDROID_HOME"
    
    # Download command line tools if not already present
    if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
        log_info "Downloading Android command line tools..."
        cd "$ANDROID_HOME"
        wget -q "https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"
        unzip -q commandlinetools-*.zip
        mkdir -p cmdline-tools/latest
        mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
        rm -f commandlinetools-*.zip
    fi

    # Set Android environment variables
    export ANDROID_HOME="$HOME/android-sdk"
    export ANDROID_SDK_ROOT="$HOME/android-sdk"
    export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
    
    # Add to bashrc
    cat >> ~/.bashrc << EOF
export ANDROID_HOME=\$HOME/android-sdk
export ANDROID_SDK_ROOT=\$HOME/android-sdk
export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/emulator
EOF

    # Accept licenses
    log_info "Accepting Android SDK licenses..."
    yes | sdkmanager --licenses > /dev/null 2>&1

    # Install required SDK components
    log_info "Installing Android SDK components..."
    sdkmanager --install "platform-tools" > /dev/null
    sdkmanager --install "build-tools;34.0.0" > /dev/null
    sdkmanager --install "platforms;android-28" > /dev/null
    sdkmanager --install "platforms;android-31" > /dev/null
    sdkmanager --install "platforms;android-34" > /dev/null
    sdkmanager --install "platforms;android-35" > /dev/null
    
    log_success "Android SDK installed successfully"
}

# Function to install Android emulator and system images
install_android_emulator() {
    log_info "Installing Android emulator and system images..."
    
    # Install emulator
    sdkmanager --install "emulator" > /dev/null
    
    # Install system images for different API levels (matching CI configuration)
    log_info "Installing system images for API levels 28, 31, 34, 35..."
    sdkmanager --install "system-images;android-28;default;x86" > /dev/null
    sdkmanager --install "system-images;android-31;default;x86_64" > /dev/null
    sdkmanager --install "system-images;android-34;default;x86_64" > /dev/null
    sdkmanager --install "system-images;android-35;default;x86_64" > /dev/null
    
    # Create AVDs
    log_info "Creating Android Virtual Devices..."
    echo "no" | avdmanager create avd -n test_api28 -k "system-images;android-28;default;x86" -f > /dev/null 2>&1
    echo "no" | avdmanager create avd -n test_api31 -k "system-images;android-31;default;x86_64" -f > /dev/null 2>&1
    echo "no" | avdmanager create avd -n test_api34 -k "system-images;android-34;default;x86_64" -f > /dev/null 2>&1
    echo "no" | avdmanager create avd -n test_api35 -k "system-images;android-35;default;x86_64" -f > /dev/null 2>&1
    
    log_success "Android emulator and system images installed successfully"
}

# Function to setup KVM for emulator acceleration
setup_kvm() {
    log_info "Setting up KVM for Android emulator acceleration..."
    
    if [ -e /dev/kvm ]; then
        # Add user to kvm group
        sudo usermod -a -G kvm $USER
        
        # Set up KVM permissions
        echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules > /dev/null
        sudo udevadm control --reload-rules
        sudo udevadm trigger --name-match=kvm
        
        log_success "KVM acceleration enabled for Android emulator"
    else
        log_warning "KVM not available - emulator will run without hardware acceleration"
    fi
}

# Function to install Python 3.13
install_python() {
    log_info "Installing Python 3.13..."
    
    if command_exists python3.13; then
        log_success "Python 3.13 already installed"
        return 0
    fi

    # Install Python 3.13 from deadsnakes PPA
    sudo apt-get install -y software-properties-common
    sudo add-apt-repository -y ppa:deadsnakes/ppa
    sudo apt-get update -qq
    sudo apt-get install -y python3.13 python3.13-pip python3.13-dev python3.13-venv
    
    # Create symlink for python3 command
    sudo ln -sf /usr/bin/python3.13 /usr/local/bin/python3
    
    log_success "Python 3.13 installed successfully"
}

# Function to install backend dependencies
install_backend_dependencies() {
    log_info "Installing backend dependencies..."
    
    if [ -f "backend/image/requirements.txt" ]; then
        python3 -m pip install --upgrade pip > /dev/null
        pip install -r backend/image/requirements.txt > /dev/null
        log_success "Backend dependencies installed successfully"
    else
        log_warning "Backend requirements.txt not found - skipping backend dependency installation"
    fi
}

# Function to install development tools
install_dev_tools() {
    log_info "Installing development tools..."
    
    sudo apt-get update -qq
    sudo apt-get install -y \
        curl \
        git \
        build-essential \
        unzip \
        vim \
        htop > /dev/null
    
    log_success "Development tools installed successfully"
}

# Function to verify installation
verify_installation() {
    log_info "Verifying installation..."
    
    local errors=0
    
    # Check Java
    if ! command_exists java; then
        log_error "Java not found in PATH"
        errors=$((errors + 1))
    else
        log_success "Java: $(java -version 2>&1 | head -n1)"
    fi
    
    # Check Android SDK
    if [ ! -d "$HOME/android-sdk" ]; then
        log_error "Android SDK not found"
        errors=$((errors + 1))
    else
        log_success "Android SDK found at $HOME/android-sdk"
    fi
    
    # Check Python
    if ! command_exists python3.13; then
        log_error "Python 3.13 not found"
        errors=$((errors + 1))
    else
        log_success "Python: $(python3.13 --version)"
    fi
    
    # Check Gradle wrapper
    if [ -f "./gradlew" ]; then
        log_success "Gradle wrapper found"
    else
        log_error "Gradle wrapper not found - make sure you're in the project directory"
        errors=$((errors + 1))
    fi
    
    return $errors
}

# Function to test build
test_build() {
    log_info "Testing project build..."
    
    # Test Gradle build health
    if ./gradlew buildHealth > /dev/null 2>&1; then
        log_success "Gradle build health check passed"
    else
        log_warning "Gradle build health check failed - this may be expected in some environments"
    fi
    
    # Test backend server start (basic check)
    if [ -f "backend/image/app.py" ]; then
        log_info "Backend server found - ready for testing"
    else
        log_warning "Backend server not found"
    fi
}

# Main function
main() {
    echo "🏠 My Kitchen - GitHub Agent Setup"
    echo "=================================="
    echo ""
    
    # Detect OS
    detect_os
    log_info "Detected OS: $OS $VERSION"
    
    # Check if we're in the right directory
    if [ ! -f "settings.gradle.kts" ] || [ ! -f "build.gradle.kts" ]; then
        log_error "This script must be run from the My Kitchen project root directory"
        log_info "Please run: cd my-kitchen && ./setup-github-agent.sh"
        exit 1
    fi
    
    # Check for sudo access
    if ! sudo -n true 2>/dev/null; then
        log_info "This script requires sudo access for installing system packages"
        sudo echo "Sudo access confirmed"
    fi
    
    # Install components
    install_dev_tools
    install_java
    install_android_sdk
    install_android_emulator
    setup_kvm
    install_python
    install_backend_dependencies
    
    echo ""
    log_info "Running verification checks..."
    if verify_installation; then
        echo ""
        log_success "🎉 Setup completed successfully!"
        echo ""
        echo "Next steps:"
        echo "1. Restart your terminal or run: source ~/.bashrc"
        echo "2. Test the build: ./gradlew buildHealth"
        echo "3. Run unit tests: ./gradlew test"
        echo "4. For instrumentation tests, start the backend first:"
        echo "   export RECIPES_SECRET_KEY=TestKey"
        echo "   python3 backend/image/app.py backend/seed_data &"
        echo "   Then run: ./gradlew connectedCheck"
        echo ""
        echo "📚 For detailed documentation, see: github-agent-setup.md"
    else
        echo ""
        log_error "Setup completed with some issues. Please check the errors above."
        exit 1
    fi
    
    # Optional: Test build
    echo ""
    read -p "Would you like to test the build now? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        test_build
    fi
}

# Run main function
main "$@"
