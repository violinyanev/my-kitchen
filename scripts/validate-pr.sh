#!/bin/bash

# Pre-push validation script for My Kitchen
# Runs all checks that can be executed in the current environment.
#
# IMPORTANT — sandbox constraint: Google Maven is blocked in the Claude Code
# remote environment, so any Gradle task that touches an Android module (AGP)
# fails immediately with "Plugin not found" before a single line of build
# script is evaluated. All Android-dependent checks must be validated by CI.
#
# What this script checks (pure JVM / no AGP dependency):
#   • Detekt static analysis
#   • Unit tests for shared:domain, shared:data, server (desktop/JVM targets)
#   • Kover XML coverage report (per-module: server + shared:domain)
#   • Server fat-JAR compilation
#
# Note: root-level koverXmlReport and per-module KMP reports aggregate Android
# sources and require the Android SDK — those are CI-only. Only the pure-JVM
# :server:koverXmlReport is safe to run here.
#
# What CI checks that cannot run here:
#   • androidApp: assemble, unit tests, screenshot tests (Roborazzi)
#   • iOS framework linking
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
# Root-level koverXmlReport aggregates Android modules and requires the
# Android SDK. Run per-module reports for the pure-JVM modules instead;
# the aggregated report is validated by CI.
print_step "Generating Kover XML coverage report (server)..."
./gradlew :server:koverXmlReport
print_success "Coverage report generated"

# ── 4. Server fat-JAR compilation ─────────────────────────────────────────
print_step "Building server fat JAR (validates server compilation)..."
./gradlew :server:buildFatJar
print_success "Server fat JAR built"

echo ""
echo "==========================================="
print_success "All local checks passed."
echo ""
print_warning "The following require CI to validate (cannot run locally):"
echo "  • androidApp build + unit tests + Roborazzi screenshot tests"
echo "  • iOS framework linking"
echo "  • Web (WasmJs) build"
echo "  • Docker backend image build"
echo "  • Instrumented tests (Android emulator)"
echo ""
echo "Push your branch and wait for all GitHub Actions jobs to go green"
echo "before declaring the task complete."
echo ""
