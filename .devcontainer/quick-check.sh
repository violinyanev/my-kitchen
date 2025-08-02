#!/bin/bash

# Quick validation script for core development functionality
# This script validates the essential components that are working

echo "🚀 Quick Development Environment Check"
echo "===================================="

echo "🔧 Checking Python environment..."
python3 --version
echo "✅ Python is available!"

echo ""
echo "📦 Checking Python packages..."
if python3 -c "import flask, yaml, click, werkzeug" 2>/dev/null; then
    echo "✅ Flask and dependencies are available!"
else
    echo "❌ Missing Flask dependencies"
    exit 1
fi

echo ""
echo "☕ Checking Java environment..."
java -version
echo "✅ Java is available!"

echo ""
echo "🔨 Checking Gradle..."
./gradlew --version --quiet
echo "✅ Gradle is available!"

echo ""
echo "🌐 Testing Flask backend..."
export RECIPES_SECRET_KEY=TestKey
python3 backend/image/app.py backend/seed_data &
FLASK_PID=$!

sleep 3

if curl --fail --silent http://localhost:5000/health > /dev/null 2>&1; then
    echo "✅ Flask backend is operational!"
    kill $FLASK_PID 2>/dev/null || true
else
    echo "❌ Flask backend failed"
    kill $FLASK_PID 2>/dev/null || true
    exit 1
fi

echo ""
echo "🎉 Core development environment is ready!"
echo "💡 Ready for GitHub Copilot development workflows!"