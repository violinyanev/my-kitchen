#!/bin/bash
# My Kitchen GitHub Agent Setup - Simplified YAML-like syntax
# Similar to GitHub Actions workflow structure

set -e

# Configuration (like GitHub Actions 'env' section)
export JAVA_VERSION=17
export ANDROID_API_LEVELS="28 31 34 35"
export PYTHON_VERSION=3.13
export ANDROID_BUILD_TOOLS=34.0.0

# Steps (like GitHub Actions 'steps' section)
steps=(
  "setup_environment"
  "setup_java"
  "setup_android_sdk"
  "setup_python"
  "install_dependencies"
  "verify_setup"
)

# Step definitions (like GitHub Actions step 'run' commands)
setup_environment() {
  echo "🔧 Setting up environment..."
  sudo apt-get update -qq
  sudo apt-get install -y curl git unzip build-essential
}

setup_java() {
  echo "☕ Setting up Java ${JAVA_VERSION}..."
  if ! command -v java >/dev/null || [[ "$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)" != "${JAVA_VERSION}" ]]; then
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/adoptium.gpg
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
    sudo apt-get update -qq
    sudo apt-get install -y temurin-${JAVA_VERSION}-jdk
    export JAVA_HOME=/usr/lib/jvm/temurin-${JAVA_VERSION}-jdk-amd64
    echo "export JAVA_HOME=/usr/lib/jvm/temurin-${JAVA_VERSION}-jdk-amd64" >> ~/.bashrc
  fi
}

setup_android_sdk() {
  echo "🤖 Setting up Android SDK..."
  ANDROID_HOME="$HOME/android-sdk"
  if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    mkdir -p "$ANDROID_HOME"
    cd "$ANDROID_HOME"
    wget -q "https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"
    unzip -q commandlinetools-*.zip && rm commandlinetools-*.zip
    mkdir -p cmdline-tools/latest
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
  fi
  
  export ANDROID_HOME="$HOME/android-sdk"
  export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"
  echo "export ANDROID_HOME=\$HOME/android-sdk" >> ~/.bashrc
  echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools" >> ~/.bashrc
  
  yes | sdkmanager --licenses >/dev/null 2>&1
  sdkmanager "platform-tools" "build-tools;${ANDROID_BUILD_TOOLS}" >/dev/null
  for api in $ANDROID_API_LEVELS; do
    sdkmanager "platforms;android-${api}" >/dev/null
  done
}

setup_python() {
  echo "🐍 Setting up Python ${PYTHON_VERSION}..."
  if ! command -v python${PYTHON_VERSION} >/dev/null; then
    sudo apt-get install -y software-properties-common
    sudo add-apt-repository -y ppa:deadsnakes/ppa
    sudo apt-get update -qq
    sudo apt-get install -y python${PYTHON_VERSION} python${PYTHON_VERSION}-pip
    sudo ln -sf /usr/bin/python${PYTHON_VERSION} /usr/local/bin/python3
  fi
}

install_dependencies() {
  echo "📦 Installing project dependencies..."
  if [ -f "backend/image/requirements.txt" ]; then
    python3 -m pip install --upgrade pip >/dev/null
    pip install -r backend/image/requirements.txt >/dev/null
  fi
}

verify_setup() {
  echo "✅ Verifying setup..."
  java -version
  python3 --version
  [ -d "$HOME/android-sdk" ] && echo "Android SDK: OK" || echo "Android SDK: MISSING"
  [ -f "./gradlew" ] && echo "Gradle wrapper: OK" || echo "Gradle wrapper: MISSING"
}

# Main execution (like GitHub Actions job)
main() {
  echo "🏠 My Kitchen Setup (GitHub Actions style)"
  echo "=========================================="
  
  if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ Run from project root directory"
    exit 1
  fi
  
  for step in "${steps[@]}"; do
    echo ""
    $step
  done
  
  echo ""
  echo "🎉 Setup complete!"
  echo "Next: source ~/.bashrc && ./gradlew buildHealth"
}

main "$@"