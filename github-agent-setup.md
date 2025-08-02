# GitHub Agent Setup for My Kitchen

This document provides comprehensive instructions for setting up a development environment for GitHub coding agents working on the My Kitchen project. The setup prepares both the Android application and Python backend components for development, testing, and automation.

## Prerequisites

### System Requirements
- Ubuntu 20.04+ or similar Linux distribution
- At least 8GB RAM (16GB recommended for Android emulator)
- 20GB+ free disk space
- Internet connection for downloading dependencies

## Quick Setup Script

```bash
#!/bin/bash
set -e

echo "🏠 Setting up My Kitchen development environment for GitHub agents..."

# 1. Check out the repository (if not already done)
if [ ! -d "my-kitchen" ]; then
    echo "📦 Cloning repository..."
    git clone https://github.com/violinyanev/my-kitchen.git
    cd my-kitchen
else
    echo "📦 Repository already exists, updating..."
    cd my-kitchen
    git pull
fi

# 2. Install Java 17 (Temurin distribution as used in CI)
echo "☕ Installing Java 17..."
sudo apt-get update
sudo apt-get install -y wget apt-transport-https
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt-get update
sudo apt-get install -y temurin-17-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
echo 'export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64' >> ~/.bashrc

# 3. Install Android SDK and tools
echo "🤖 Installing Android SDK..."
sudo apt-get install -y unzip
mkdir -p ~/android-sdk
cd ~/android-sdk

# Download Android command line tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip -q commandlinetools-*.zip
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# Set Android environment variables
export ANDROID_HOME=~/android-sdk
export ANDROID_SDK_ROOT=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
echo 'export ANDROID_HOME=~/android-sdk' >> ~/.bashrc
echo 'export ANDROID_SDK_ROOT=~/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator' >> ~/.bashrc

# Accept licenses and install required SDK components
yes | sdkmanager --licenses
sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34" "platforms;android-35" "platforms;android-31" "platforms;android-28"

# Install emulator and system images for testing
sdkmanager "emulator"
sdkmanager "system-images;android-28;default;x86"
sdkmanager "system-images;android-31;default;x86_64"
sdkmanager "system-images;android-34;default;x86_64"
sdkmanager "system-images;android-35;default;x86_64"

# 4. Install Python 3.13 for backend
echo "🐍 Installing Python 3.13..."
sudo apt-get install -y software-properties-common
sudo add-apt-repository -y ppa:deadsnakes/ppa
sudo apt-get update
sudo apt-get install -y python3.13 python3.13-pip python3.13-dev

# Create symlink for python3 command
sudo ln -sf /usr/bin/python3.13 /usr/bin/python3

# 5. Install backend dependencies
echo "🖥️ Installing backend dependencies..."
cd ~/my-kitchen
python3 -m pip install --upgrade pip
pip install -r backend/image/requirements.txt

# 6. Install additional development tools
echo "🛠️ Installing development tools..."
sudo apt-get install -y curl git build-essential

# 7. Enable KVM for Android emulator (if available)
echo "🚀 Configuring Android emulator..."
if [ -e /dev/kvm ]; then
    sudo usermod -a -G kvm $USER
    echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
    sudo udevadm control --reload-rules
    sudo udevadm trigger --name-match=kvm
    echo "✅ KVM acceleration enabled for Android emulator"
else
    echo "⚠️ KVM not available - emulator will run without hardware acceleration"
fi

echo "🎉 Setup complete! Environment is ready for My Kitchen development."
echo ""
echo "Next steps:"
echo "1. Source your bashrc: source ~/.bashrc"
echo "2. Navigate to project: cd ~/my-kitchen"
echo "3. Build the project: ./gradlew buildHealth"
echo "4. Run tests: ./gradlew test"
```

## Manual Setup Instructions

### 1. Repository Setup

```bash
# Clone the repository
git clone https://github.com/violinyanev/my-kitchen.git
cd my-kitchen
```

### 2. Java 17 Installation

The project requires Java 17 (Temurin distribution recommended):

```bash
# Install Temurin repository
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -sc) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

# Install Java 17
sudo apt-get update
sudo apt-get install temurin-17-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
```

### 3. Android SDK Setup

Install Android SDK and required components:

```bash
# Create Android SDK directory
mkdir -p ~/android-sdk
cd ~/android-sdk

# Download command line tools
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-*.zip
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/

# Set environment variables
export ANDROID_HOME=~/android-sdk
export ANDROID_SDK_ROOT=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Install required SDK components
sdkmanager --licenses
sdkmanager "platform-tools" "build-tools;34.0.0"
sdkmanager "platforms;android-28" "platforms;android-31" "platforms;android-34" "platforms;android-35"
```

### 4. Android Emulator Setup (for Instrumentation Tests)

```bash
# Install emulator
sdkmanager "emulator"

# Install system images for different API levels
sdkmanager "system-images;android-28;default;x86"
sdkmanager "system-images;android-31;default;x86_64"
sdkmanager "system-images;android-34;default;x86_64"
sdkmanager "system-images;android-35;default;x86_64"

# Create AVDs for testing
avdmanager create avd -n test_api28 -k "system-images;android-28;default;x86"
avdmanager create avd -n test_api31 -k "system-images;android-31;default;x86_64"
avdmanager create avd -n test_api34 -k "system-images;android-34;default;x86_64"
avdmanager create avd -n test_api35 -k "system-images;android-35;default;x86_64"
```

### 5. Python Backend Setup

Install Python 3.13 and backend dependencies:

```bash
# Install Python 3.13
sudo apt-get install software-properties-common
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt-get update
sudo apt-get install python3.13 python3.13-pip

# Install backend dependencies
cd my-kitchen
pip install -r backend/image/requirements.txt
```

### 6. Build and Test Commands

#### Android Application

```bash
# Build health check
./gradlew buildHealth

# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run code quality checks
./gradlew detekt

# Generate code coverage report
./gradlew :app:koverXmlReportDebug

# Validate screenshot tests
./gradlew :app:validateDebugScreenshotTest
```

#### Instrumentation Tests

```bash
# Start backend server first
export RECIPES_SECRET_KEY=TestKey
python3 backend/image/app.py backend/seed_data &

# Wait for server to start
sleep 5
curl --fail http://localhost:5000/health

# Run instrumentation tests (requires emulator)
./gradlew connectedCheck
```

#### Backend Tests

```bash
cd backend
# Run backend tests (if available)
python3 -m pytest
```

## Environment Variables

Key environment variables that should be set:

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
export ANDROID_HOME=~/android-sdk
export ANDROID_SDK_ROOT=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
export RECIPES_SECRET_KEY=TestKey  # For backend testing
```

## Troubleshooting

### Common Issues

1. **Gradle build fails**: Ensure Java 17 is installed and JAVA_HOME is set correctly
2. **Android SDK not found**: Verify ANDROID_HOME and PATH are set correctly
3. **Emulator won't start**: Check KVM is enabled and system has enough resources
4. **Backend tests fail**: Ensure Python dependencies are installed and Flask server is running

### Performance Optimization

- Use `--parallel` flag with Gradle for faster builds
- Increase Gradle daemon heap size: `org.gradle.jvmargs=-Xmx4g`
- Use local.properties for Android SDK path instead of environment variables

## Continuous Integration

This setup mirrors the configuration used in the GitHub Actions workflows:
- `.github/workflows/test.yaml` - Main test workflow
- `.github/workflows/release.yaml` - Release workflow
- `.github/actions/get-avd-info/` - Custom action for Android emulator setup

The agent can reference these workflows for additional context and ensure consistency with the CI environment.

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Gradle Build Tool](https://gradle.org/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Flask Documentation](https://flask.palletsprojects.com/)
