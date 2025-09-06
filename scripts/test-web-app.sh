#!/bin/bash

# Test script for My Kitchen Web App
set -e

echo "🧪 Testing My Kitchen Web App"

# Check if we're in the right directory
if [ ! -f "shared/build.gradle.kts" ]; then
    echo "❌ Please run this script from the project root directory"
    exit 1
fi

# Build the web app
echo "📦 Building web app..."
./gradlew :shared:jsBrowserDistribution

# Check if build was successful
if [ ! -d "shared/build/dist/js/productionExecutable" ]; then
    echo "❌ Web app build failed"
    exit 1
fi

echo "✅ Web app built successfully"

# Install Playwright if not already installed
if [ ! -d "node_modules/@playwright" ]; then
    echo "📥 Installing Playwright..."
    npm install
    npx playwright install
fi

# Check if backend is running
echo "🔍 Checking if backend is running..."
if curl -s http://localhost:5000/health > /dev/null 2>&1; then
    echo "✅ Backend is running"
    BACKEND_RUNNING=true
else
    echo "⚠️  Backend is not running - some tests will be skipped"
    BACKEND_RUNNING=false
fi

# Start the web server in the background
echo "🌐 Starting web server..."
cd shared/build/dist/js/productionExecutable
python3 -m http.server 8080 &
WEB_SERVER_PID=$!
cd ../../../../..

# Wait for server to start
sleep 2

# Check if web server is running
if curl -s http://localhost:8080 > /dev/null 2>&1; then
    echo "✅ Web server is running on http://localhost:8080"
else
    echo "❌ Failed to start web server"
    kill $WEB_SERVER_PID 2>/dev/null || true
    exit 1
fi

# Run Playwright tests
echo "🧪 Running Playwright tests..."
if [ "$BACKEND_RUNNING" = true ]; then
    echo "Running full test suite (with backend integration)..."
    npx playwright test
else
    echo "Running tests without backend integration..."
    npx playwright test --grep-invert "backend|login.*success"
fi

TEST_EXIT_CODE=$?

# Stop the web server
echo "🛑 Stopping web server..."
kill $WEB_SERVER_PID 2>/dev/null || true

# Show test results
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ All tests passed!"
    echo "📊 Test report available at: playwright-report/index.html"
    echo "Run 'npx playwright show-report' to view the report"
else
    echo "❌ Some tests failed"
    echo "📊 Test report available at: playwright-report/index.html"
    echo "Run 'npx playwright show-report' to view the report"
fi

exit $TEST_EXIT_CODE