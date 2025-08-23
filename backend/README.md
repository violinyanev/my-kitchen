# Backend implementations

This repository contains backend implementations for the application which can be self-hosted on your own server.

## Python Flask Backend (Original)

The original Python Flask implementation located in the `image/` directory.

### Running the Python backend

```bash
# From docker (emulates release mode)
python3 ./scripts/dev.py start

# From flask directly (for debugging)
./scripts/dev.sh
```

### Building the docker image locally

```bash
python3 ./scripts/dev.py build
```

## Kotlin Ktor Backend (New)

A new Kotlin implementation using the Ktor framework, providing identical API compatibility.

**Location**: `../backend-kt/`

### Features
- **Drop-in replacement** for Python backend
- **Same REST API endpoints** and response format
- **JWT token compatibility** with Python backend
- **Same YAML file storage** format
- **Comprehensive unit tests** (25+ tests)
- **Docker support** for containerized deployment
- **Better performance** and lower memory usage

### Running the Ktor backend

```bash
# From the backend-kt directory
./scripts/dev.sh

# Or using Gradle directly from project root
RECIPES_SECRET_KEY="Test" ./gradlew :backend-kt:run --args="../backend/seed_data"
```

### Testing the Ktor backend

```bash
# Run comprehensive unit tests
./gradlew :backend-kt:test

# Build distribution
./gradlew :backend-kt:distTar
```

See `../backend-kt/README.md` for detailed documentation.

## API Compatibility

Both backends provide identical REST APIs:

- `GET /health` - Health check
- `POST /users/login` - User authentication
- `GET /users` - List users (authenticated)
- `GET /user` - Get current user (authenticated)
- `GET /recipes` - Get recipes (authenticated)
- `POST /recipes` - Create recipe (authenticated)
- `DELETE /recipes/<id>` - Delete recipe (authenticated)

## Migration Notes

The Ktor backend can use the same data files as the Python backend:
- JWT tokens are cross-compatible
- YAML file format is identical
- No data migration required

## Open TODOs

* Add installation instructions
* Add proper logging
* Find a better database than yaml files
* Add a check/tests for backwards compatibility
* Provide swagger
* Search for a better library for REST APIs

## Author Note

> Use this backend understanding that it's primarily a learning exercise. Contributions, code reviews, and suggestions for improvements are especially welcome! 🚀
