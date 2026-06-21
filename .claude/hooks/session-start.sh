#!/bin/bash
# Session-start hook for my-kitchen (Claude Code on the web).
#
# Warms up the Gradle cache so tests and static analysis are fast.
# Requires the environment's network policy to allow:
#   dl.google.com          — Android Gradle Plugin (AGP), without which the
#                            root build script cannot be configured at all
#   storage.googleapis.com — Chrome / browser binaries for E2E tests
#   playwright.azureedge.net / cdn.playwright.dev  — Playwright fallback CDN
#
# Under a restricted policy that blocks dl.google.com this hook will fail fast
# with a clear error message explaining which host to add to the allow-list.

set -euo pipefail

echo '{"async": true, "asyncTimeout": 600000}'

# Only run in the remote Claude Code environment
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
    echo "[hook] Not a remote session – skipping setup"
    exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel)}"

# ── Helpers ────────────────────────────────────────────────────────────────
log()  { echo "[session-start] $*"; }
fail() { echo "[session-start] ERROR: $*" >&2; exit 1; }

can_reach() { timeout 10 curl -sS -o /dev/null -w "%{http_code}" "$1" 2>/dev/null; }

# ── Preflight network check ────────────────────────────────────────────────
log "Checking network policy..."
GOOGLE_MAVEN_STATUS=$(can_reach "https://dl.google.com/dl/android/maven2/")
if [ "$GOOGLE_MAVEN_STATUS" != "200" ]; then
    cat >&2 <<EOF

[session-start] SETUP INCOMPLETE — dl.google.com is blocked (HTTP $GOOGLE_MAVEN_STATUS).

  Android Gradle Plugin (AGP) is distributed exclusively from dl.google.com.
  Because the root build.gradle.kts references AGP, ALL ./gradlew tasks fail
  during configuration — including :server:test, detekt, and koverXmlReport.

  To fix:  add  dl.google.com  to your environment's network allow-list,
           then start a new session so this hook can fully warm the cache.
EOF
    exit 1
fi
log "dl.google.com is reachable (HTTP $GOOGLE_MAVEN_STATUS) — proceeding"

# ── 0. Ensure Java 17 is installed (required by server module toolchain) ───
if [ ! -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
    log "Java 17 not found — installing openjdk-17-jdk..."
    # --allow-unauthenticated tolerates blocked third-party PPAs in the sandbox
    apt-get update -qq --allow-unauthenticated 2>/dev/null || true
    apt-get install -y -qq --allow-unauthenticated openjdk-17-jdk
    log "Java 17 installed at /usr/lib/jvm/java-17-openjdk-amd64"
else
    log "Java 17 already available"
fi

# ── 1. Warm Gradle + resolve all dependencies ──────────────────────────────
log "Resolving Gradle dependencies for all modules..."
./gradlew dependencies --continue --quiet \
    -Porg.gradle.warning.mode=none \
    2>&1 | tail -5
log "Gradle dependencies resolved"

# ── 2. Run JVM checks (proves cache is warm) ─────────────────────────────
log "Running Detekt..."
./gradlew detekt --quiet 2>&1 | tail -5

log "Running JVM unit tests (domain + data + server)..."
./gradlew :shared:domain:desktopTest \
          :shared:data:desktopTest \
          :server:test \
          --continue --quiet 2>&1 | tail -10
log "JVM tests passed"

# ── 3. Browser tooling (for E2E / Playwright tests) ───────────────────────
log "Checking browser-binary CDN reachability..."
PLAYWRIGHT_STATUS=$(can_reach "https://cdn.playwright.dev/")
GOOG_STORAGE_STATUS=$(can_reach "https://storage.googleapis.com/")

if [ "$PLAYWRIGHT_STATUS" != "200" ] && [ "$GOOG_STORAGE_STATUS" != "200" ]; then
    log "WARNING: Playwright/Chrome CDNs are blocked — E2E tests need:"
    log "         cdn.playwright.dev  OR  storage.googleapis.com"
    log "         Add them to the network allow-list for browser-based tests"
else
    log "Browser CDN is reachable — installing/updating Playwright..."
    # Install playwright deps (npx downloads the browser if not cached)
    if [ -f "e2e/package.json" ]; then
        log "Installing Playwright npm dependencies..."
        (cd e2e && npm install --prefer-offline --silent)
        log "Installing Playwright Chromium browser..."
        (cd e2e && npx playwright install --with-deps chromium 2>&1 | tail -10)
        log "Playwright + Chromium ready"
        log "To run E2E tests locally:"
        log "  1. ./gradlew :webApp:wasmJsBrowserDistribution"
        log "  2. cd e2e && npx playwright test"
    else
        log "No e2e/package.json found yet — skipping Playwright install"
    fi
fi

log "Session-start hook complete — environment is ready"
