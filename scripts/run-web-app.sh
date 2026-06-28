#!/bin/bash

# My Kitchen Web App Launcher
# This script builds the web app and starts both backend and web servers

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

# Check if we're in the right directory
if [ ! -f "gradlew" ]; then
    print_error "Must be run from project root directory"
    exit 1
fi

echo "🍳 My Kitchen Web App Launcher"
echo "================================"

# Build the web app
print_step "Building web app..."
./gradlew :shared:jsBrowserDistribution

if [ $? -ne 0 ]; then
    print_error "Web app build failed"
    exit 1
fi

print_success "Web app built successfully"

# Check if backend can start
print_step "Checking backend health..."
if [ ! -f "backend/scripts/dev.sh" ]; then
    print_error "Backend development script not found"
    exit 1
fi

# Start backend in background
print_step "Starting backend server..."
./backend/scripts/dev.sh &
BACKEND_PID=$!

# Wait a moment for backend to start
sleep 3

# Test backend health
if curl -s http://localhost:5000/health > /dev/null 2>&1; then
    print_success "Backend server started at http://localhost:5000"
else
    print_warning "Backend server may not be ready yet (this is normal)"
fi

# Start web server in background
WEB_DIR="shared/build/dist/js/productionExecutable"
print_step "Starting web server..."

if [ ! -d "$WEB_DIR" ]; then
    print_error "Web app build directory not found: $WEB_DIR"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

cd "$WEB_DIR"
python3 -m http.server 8080 &
WEB_PID=$!
cd - > /dev/null

# Wait a moment for web server to start
sleep 2

print_success "Web server started at http://localhost:8080"

echo ""
echo "🎉 My Kitchen is ready!"
echo "================================"
echo "📱 Web App:     http://localhost:8080"
echo "🖥️  Backend API: http://localhost:5000"
echo "📋 Backend Health: http://localhost:5000/health"
echo ""
echo "Press Ctrl+C to stop both servers"

# Function to cleanup on exit
cleanup() {
    echo ""
    print_step "Stopping servers..."
    kill $BACKEND_PID 2>/dev/null || true
    kill $WEB_PID 2>/dev/null || true
    print_success "Servers stopped"
    exit 0
}

# Trap Ctrl+C and call cleanup
trap cleanup INT

# Wait for user to stop
wait
