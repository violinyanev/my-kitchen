#!/bin/bash

# Pre-push validation script for My Kitchen
# Runs all checks that can be executed in the current environment.
#
# Always-run checks (pure JVM / no AGP dependency):
#   • Detekt static analysis
#   • Unit tests for shared:domain, shared:data, server (desktop/JVM targets)
#   • Kover XML coverage report
#   • Server fat-JAR compilation
#
# Android checks — run automatically WHEN an Android SDK is available and
# Google Maven (AGP) is reachable:
#   • Release APK build (assembleRelease) — catches R8 / signing / variant regressions
#   • Robolectric unit tests (testDebugUnitTest)
#   • Roborazzi screenshot verification (verifyRoborazziDebug)
# To enable them locally, install an Android SDK and point ANDROID_HOME at it
# (or set sdk.dir in local.properties), e.g. compileSdk platform + build-tools.
#
# IMPORTANT — sandbox constraint: Google Maven is blocked in the Claude Code
# remote environment, so any Gradle task that touches an Android module (AGP)
# fails before the build script is evaluated. The Android section below is
# skipped automatically when no SDK is configured, so this script still works
# in the sandbox — but those checks must then be validated by CI instead.
#
# What only CI can validate (never run by this script):
#   • iOS framework linking (needs macOS + Xcode)
#   • Web (WasmJs) build
#   • Docker image build
#   • Instrumented tests (Android emulator, API 28/31/34/35/36)

set -e

echo "Starting My Kitchen pre-push validation..."
echo "==========================================="

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step()    { echo -e "${BLUE}  >> $1${NC}"; }
print_success() { echo -e "${GREEN}  OK $1${NC}"; }
print_warning() { echo -e "${YELLOW}  WARN $1${NC}"; }
print_error()   { echo -e "${RED}  FAIL $1${NC}"; }

if [ ! -f "gradlew" ]; then
    print_error "Must be run from the project root directory"
    exit 1
fi

# ── 1. Detekt ──────────────────────────────────────────────────────────────
print_step "Running Detekt static analysis..."
./gradlew detekt
print_success "Detekt passed"

# ── 2. JVM unit tests ─────────────────────────────────────────────────────
print_step "Running shared:domain unit tests (desktop/JVM)..."
./gradlew :shared:domain:desktopTest
print_success "shared:domain tests passed"

print_step "Running shared:data unit tests (desktop/JVM)..."
./gradlew :shared:data:desktopTest
print_success "shared:data tests passed"

print_step "Running server unit tests..."
./gradlew :server:test
print_success "server tests passed"

# ── 3. Coverage report ────────────────────────────────────────────────────
print_step "Generating Kover XML coverage report..."
./gradlew koverXmlReport
print_success "Coverage report generated"

# ── 4. Server fat-JAR compilation ─────────────────────────────────────────
print_step "Building server fat JAR (validates server compilation)..."
./gradlew :server:buildFatJar
print_success "Server fat JAR built"

# ── 5. Android checks (only when an Android SDK is configured) ─────────────
# Resolve the SDK location from the standard env vars, then fall back to
# sdk.dir in local.properties. The `|| true` guards keep `set -e` happy when
# the var is unset or local.properties has no sdk.dir line.
ANDROID_SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$ANDROID_SDK" ] && [ -f "local.properties" ]; then
    ANDROID_SDK="$(grep -E '^sdk\.dir=' local.properties | cut -d'=' -f2- || true)"
fi

ANDROID_RAN=false
if [ -n "$ANDROID_SDK" ] && [ -d "$ANDROID_SDK/platforms" ]; then
    ANDROID_RAN=true
    print_step "Android SDK detected ($ANDROID_SDK) — running Android checks..."

    print_step "Building release APK (assembleRelease)..."
    ./gradlew :androidApp:assembleRelease
    print_success "Release APK built"

    print_step "Running Robolectric unit tests (testDebugUnitTest)..."
    ./gradlew :androidApp:testDebugUnitTest
    print_success "Android unit tests passed"

    print_step "Verifying Roborazzi screenshots (verifyRoborazziDebug)..."
    ./gradlew :androidApp:verifyRoborazziDebug
    print_success "Screenshot verification passed"
else
    print_warning "No Android SDK configured — skipping Android checks."
    print_warning "Set ANDROID_HOME (or sdk.dir in local.properties) to enable them."
fi

echo ""
echo "==========================================="
print_success "All local checks passed."
echo ""
if [ "$ANDROID_RAN" = true ]; then
    print_warning "The following still require CI to validate (cannot run locally):"
    echo "  • iOS framework linking (needs macOS + Xcode)"
    echo "  • Web (WasmJs) build"
    echo "  • Docker backend image build"
    echo "  • Instrumented tests (Android emulator)"
else
    print_warning "The following require CI to validate (cannot run locally):"
    echo "  • androidApp build + unit tests + Roborazzi screenshot tests"
    echo "  • iOS framework linking"
    echo "  • Web (WasmJs) build"
    echo "  • Docker backend image build"
    echo "  • Instrumented tests (Android emulator)"
fi
echo ""
echo "Push your branch and wait for all GitHub Actions jobs to go green"
echo "before declaring the task complete."
echo ""
