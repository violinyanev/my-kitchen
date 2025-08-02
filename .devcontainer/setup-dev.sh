#!/bin/bash

# Helper script for development environment setup and validation
# This script demonstrates the typical development workflow

set -e

echo "🏠 My Kitchen Development Environment Helper"
echo "============================================="

echo "📋 Checking build health..."
./gradlew buildHealth

echo ""
echo "🔧 Installing Python dependencies..."
pip install --upgrade pip
pip install -r backend/image/requirements.txt

echo ""
echo "📱 Building Android debug APK..."
./gradlew :app:assembleDebug

echo ""
echo "🧪 Running unit tests..."
./gradlew :app:testDebugUnitTest

echo ""
echo "🚀 Starting Flask backend (in background)..."
export RECIPES_SECRET_KEY=TestKey
python3 backend/image/app.py backend/seed_data &
FLASK_PID=$!

echo "⏳ Waiting for Flask server to start..."
sleep 5

echo "🔍 Testing Flask server health..."
if curl --fail http://localhost:5000/health; then
    echo "✅ Flask server is running successfully!"
else
    echo "❌ Flask server failed to start"
    exit 1
fi

echo ""
echo "🛑 Stopping Flask server..."
kill $FLASK_PID 2>/dev/null || true

echo ""
echo "✅ Development environment validation complete!"
echo "🎉 Your environment is ready for GitHub Copilot development!"