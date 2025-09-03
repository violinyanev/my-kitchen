# My Kitchen Web App

The My Kitchen web app is a browser-based application built with Kotlin/JS that shares the same business logic as the Android and iOS apps through Kotlin Multiplatform.

## Features

- 🌐 **Browser-based**: Runs in any modern web browser
- 🔗 **Shared Logic**: Uses the same Recipe domain models and business logic as mobile apps
- 📱 **Responsive Design**: Works on desktop and mobile browsers
- 🎨 **Modern UI**: Clean, intuitive interface with CSS styling
- ⚡ **Fast**: Compiled to optimized JavaScript with webpack

## Architecture

The web app uses:
- **Kotlin/JS**: Compiles Kotlin code to JavaScript
- **kotlinx-html**: Type-safe HTML DSL for UI generation
- **Shared Module**: Recipe models and business logic from `shared/src/commonMain`
- **Webpack**: Bundling and optimization

## Getting Started

### Prerequisites

- JDK 17+
- Node.js (automatically downloaded by Gradle)
- Modern web browser

### Building the Web App

```bash
# Build the web app distribution
./gradlew :shared:jsBrowserDistribution

# Output location:
# shared/build/dist/js/productionExecutable/
#   ├── index.html          # Main HTML file
#   ├── shared.js           # Compiled JavaScript bundle
#   └── shared.js.map       # Source map for debugging
```

### Running the Web App

#### Option 1: Simple HTTP Server

```bash
# Navigate to the build output
cd shared/build/dist/js/productionExecutable

# Start a simple HTTP server
python3 -m http.server 8080

# Open browser to http://localhost:8080
```

#### Option 2: Development Server (with live reload)

```bash
# Use Gradle's built-in development server
./gradlew :shared:jsBrowserDevelopmentRun

# Opens automatically at http://localhost:8080
# Auto-reloads when source files change
```

### Running with Backend Integration

To test the full application with backend integration:

```bash
# Terminal 1: Start the backend server
./backend/scripts/dev.sh
# Backend runs at http://localhost:5000

# Terminal 2: Build and serve the web app
./gradlew :shared:jsBrowserDistribution
cd shared/build/dist/js/productionExecutable
python3 -m http.server 8080
# Web app runs at http://localhost:8080

# Open http://localhost:8080 in your browser
```

## Web App Features

### Current Functionality

- ✅ **Recipe Creation**: Create sample recipes using shared Recipe model
- ✅ **Recipe Display**: List and view recipes in a clean interface
- ✅ **Responsive UI**: Works on desktop and mobile browsers
- ✅ **Shared Logic**: Uses the same domain models as mobile apps

### Development Roadmap

- 🔄 **Backend Integration**: Connect to Flask API for full CRUD operations
- 🔄 **User Authentication**: Login and user management
- 🔄 **Recipe Management**: Full recipe editing, deletion, and search
- 🔄 **Recipe Sharing**: Share recipes between users
- 🔄 **Offline Support**: PWA capabilities for offline usage

## Development

### Project Structure

```
shared/src/jsMain/
├── kotlin/
│   └── main.kt                 # Main web app entry point
└── resources/
    └── index.html              # HTML template
```

### Building for Development

```bash
# Build development version (faster, with source maps)
./gradlew :shared:jsBrowserDevelopmentDistribution

# Build production version (optimized, minified)
./gradlew :shared:jsBrowserDistribution
```

### Development Server

```bash
# Start development server with live reload
./gradlew :shared:jsBrowserDevelopmentRun

# The server will automatically:
# - Compile Kotlin to JavaScript
# - Start development server
# - Open browser
# - Reload on file changes
```

### Available Gradle Tasks

```bash
# List all JS-related tasks
./gradlew tasks --group="js"

# Key tasks:
./gradlew :shared:jsBrowserDistribution          # Production build
./gradlew :shared:jsBrowserDevelopmentDistribution  # Development build
./gradlew :shared:jsBrowserDevelopmentRun        # Development server
./gradlew :shared:compileKotlinJs                # Compile only
```

## Browser Compatibility

The web app is compatible with:
- ✅ Chrome 80+
- ✅ Firefox 75+
- ✅ Safari 13+
- ✅ Edge 80+

## Troubleshooting

### Common Issues

#### Build Fails with "Name contains illegal characters"
- **Cause**: NPM package names cannot contain spaces
- **Solution**: Ensure `rootProject.name` in `settings.gradle.kts` has no spaces

#### "Unresolved reference" errors in Kotlin/JS
- **Cause**: Missing dependencies or incorrect API usage
- **Solution**: Check that all required dependencies are in `jsMain.dependencies`

#### Web app loads but shows blank page
- **Cause**: JavaScript bundle not loading or runtime errors
- **Solution**: Check browser console for errors, verify `shared.js` is accessible

### Debug Mode

To debug the web app:

1. Build with development configuration for source maps
2. Open browser developer tools
3. Set breakpoints in the Kotlin source files
4. Use console.log for debugging output

```kotlin
// Add debug logging in Kotlin code
console.log("Debug message from Kotlin!")
```

## Contributing

When contributing to the web app:

1. Ensure Kotlin/JS builds successfully: `./gradlew :shared:jsBrowserDistribution`
2. Test in multiple browsers
3. Follow existing code patterns in `main.kt`
4. Update this documentation for new features

## Resources

- [Kotlin/JS Documentation](https://kotlinlang.org/docs/js-overview.html)
- [kotlinx-html Documentation](https://github.com/Kotlin/kotlinx.html)
- [Kotlin Multiplatform Guide](https://kotlinlang.org/docs/multiplatform.html)