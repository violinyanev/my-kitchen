# My Kitchen

![Tests](https://github.com/violinyanev/my-kitchen/actions/workflows/test.yaml/badge.svg) ![Release](https://github.com/violinyanev/my-kitchen/actions/workflows/release.yaml/badge.svg) ![Security](https://github.com/violinyanev/my-kitchen/actions/workflows/security.yml/badge.svg) [![GitHub License](https://img.shields.io/github/license/violinyanev/my-kitchen?label=License)](https://github.com/violinyanev/my-kitchen/blob/main/LICENSE)

My Kitchen is a free and open source application for storing and sharing recipes with your family. It features both an Android mobile app and a [self-hosted backend server](./backend/README.md) that allows you to back up your recipe data on your own premises, ensuring complete control over your culinary collection.

## Features

- 📱 **Android Mobile App**: Native Android application built with Kotlin and Jetpack Compose
- 🖥️ **Self-hosted Backend**: Python Flask server that you can run on your own infrastructure
- 🔒 **Privacy First**: Keep your recipes on your own server, no third-party data collection
- 👨‍👩‍👧‍👦 **Family Sharing**: Share your favorite recipes with family members
- 📝 **Recipe Management**: Store, organize, and manage your recipe collection
- 🐳 **Docker Support**: Easy deployment with Docker containers

## Project Structure

```
my-kitchen/
├── app/              # Android mobile application (Kotlin)
├── backend/          # Self-hosted server (Python Flask)
│   └── scripts/      # Backend development scripts
├── .github/          # CI/CD workflows and actions
└── scripts/          # General development scripts
```

## Installation

### Android App

The Android app can be built from source or installed from releases:

1. **From source**: Clone this repository and build using Android Studio or Gradle
2. **From releases**: Download the APK from the [Releases page](https://github.com/violinyanev/my-kitchen/releases)

**Requirements:**
- Android 7.0 (API level 24) or higher
- ~50MB storage space

### Backend Server

See the [backend README](./backend/README.md) for detailed installation and setup instructions.

**Quick start with Docker:**
```bash
python3 ./backend/scripts/dev.py start
```

## Development

### Quick Setup (Recommended)
```bash
# Clone the repository
git clone https://github.com/violinyanev/my-kitchen.git
cd my-kitchen

# Run automated setup (installs dependencies, sets up hooks, runs initial build)
./scripts/setup-dev.sh
```

### Prerequisites

- **For Android app**: JDK 17+, Android Studio, Android SDK
- **For backend**: Python 3.8+, Docker (optional)

### Building the Android App

```bash
# Clone the repository
git clone https://github.com/violinyanev/my-kitchen.git
cd my-kitchen

# Automated setup (recommended)
./scripts/setup-dev.sh

# Manual build (if needed)
./gradlew :app:assembleDebug

# Run tests
./gradlew :app:testDebugUnitTest
```

### Running the Backend

```bash
# Navigate to backend directory
cd backend

# Run development server
./scripts/dev.sh

# Or with Docker
python3 ./scripts/dev.py start
```

### Development Workflow

Before committing any changes, ensure all checks pass:

```bash
# Run comprehensive validation (recommended)
./scripts/validate-pr.sh

# Or run individual checks
./gradlew buildHealth
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:verifyRoborazziDebug :app:koverXmlReportDebug detekt
```

The project includes:
- **Pre-commit hooks**: Automatically validate code quality and format
- **Comprehensive testing**: Unit tests, screenshot tests, and coverage reporting  
- **Code quality checks**: Detekt linting with auto-correction
- **CI/CD validation**: All checks must pass for PR approval

## Contributing

We welcome contributions from everyone! Here's how you can help:

### Reporting Issues

- Found a bug? Please [open an issue](https://github.com/violinyanev/my-kitchen/issues) with:
  - Clear description of the problem
  - Steps to reproduce
  - Expected vs actual behavior
  - Device/platform information (for Android issues)

### Contributing Code

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run setup: `./scripts/setup-dev.sh` (sets up hooks and validation)
4. Make your changes
5. Add tests if applicable
5. Ensure all checks pass: `./scripts/validate-pr.sh`
6. Commit with conventional format: `git commit -m 'feat: add amazing feature'`
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

**Important**: All PRs must pass GitHub Actions checks including:
- Build health and compilation
- Unit tests with 80% coverage on changed files  
- Code quality checks (Detekt)
- Screenshot tests validation
- Backend tests (if applicable)

The setup script installs Git hooks that will catch common issues before commit.

### Development Areas

- 🐛 Bug fixes and improvements
- ✨ New features for recipe management
- 🎨 UI/UX improvements
- 📱 Android app enhancements
- 🖥️ Backend API improvements
- 📚 Documentation updates
- 🧪 Test coverage improvements

## Security

This project includes comprehensive automated security analysis:

- 🔒 **Static Security Analysis**: Automated scanning of Python and Kotlin code for security vulnerabilities
- 🛡️ **Dependency Vulnerability Scanning**: Regular checks for known vulnerabilities in project dependencies
- 🐳 **Container Security**: Docker image scanning for security issues
- 🔑 **Secrets Detection**: Automated detection of accidentally committed secrets and credentials
- 📜 **License Compliance**: Monitoring of dependency licenses for compliance

Security scans run automatically on every pull request and weekly on the main branch. For detailed information about the security analysis system, see [SECURITY.md](SECURITY.md).

## Support

- 📖 Check the [backend documentation](./backend/README.md) for server setup
- 🔒 Review [security documentation](SECURITY.md) for security practices
- 🐛 [Report issues](https://github.com/violinyanev/my-kitchen/issues) on GitHub
- 💬 Discuss ideas and ask questions in GitHub Discussions

## Roadmap

See the [backend README](./backend/README.md) for current development priorities and open TODOs.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

Made with ❤️ for home cooks and recipe enthusiasts
