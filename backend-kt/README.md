# Ktor Backend Implementation

This is a Kotlin implementation of the backend using the Ktor framework. It provides the same REST API as the original Python Flask backend with identical behavior and API compatibility.

## Features

- **REST API Compatibility**: All endpoints match the Python backend exactly
- **JWT Authentication**: Using HS256 algorithm, compatible with existing tokens
- **YAML File Storage**: Same file format and backup capability as Python backend
- **JSON Serialization**: Compatible request/response format
- **Unit Tests**: Comprehensive test coverage for all components
- **Docker Support**: Can be containerized for deployment

## API Endpoints

- `GET /health` - Health check, returns "OK"
- `GET /version` - Returns API version info (requires auth)
- `POST /users/login` - User login, returns JWT token
- `GET /users` - Get all users (requires auth)
- `GET /user` - Get current user (requires auth)
- `GET /recipes` - Get user's recipes (requires auth)
- `POST /recipes` - Create new recipe (requires auth)
- `DELETE /recipes/<id>` - Delete recipe (requires auth)

## Running the Backend

### From source (for development)
```bash
./scripts/dev.sh
```

### Using Gradle directly
```bash
# From project root
RECIPES_SECRET_KEY="Test" ./gradlew :backend-kt:run --args="../backend/seed_data"
```

### Building distribution
```bash
./gradlew :backend-kt:distTar
```

### Running tests
```bash
./gradlew :backend-kt:test
```

## Configuration

Environment variables:
- `RECIPES_SECRET_KEY` - JWT secret key (required)
- `FLASK_HOST` - Server host (default: 127.0.0.1)
- `FLASK_PORT` - Server port (default: 5000)

Command line arguments:
- First argument: Path to data directory (default: system temp directory)

## Docker Support

Build the distribution and create a Docker image:

```bash
./gradlew :backend-kt:distTar
docker build -t my-kitchen-backend-kt backend-kt/
```

Run the container:
```bash
docker run -p 5000:5000 -e RECIPES_SECRET_KEY="your-secret-key" my-kitchen-backend-kt
```

## Testing

The backend includes comprehensive unit tests:

- **Database Tests**: YAML file operations, schema validation, backups
- **Authentication Tests**: JWT token generation and validation
- **Route Tests**: HTTP endpoint behavior and authentication

Run tests: `./gradlew :backend-kt:test`

## Architecture

- **Application.kt**: Main server setup and configuration
- **Routes**: Separate modules for user and recipe endpoints
- **Database**: YAML file persistence with backup capability
- **Authentication**: JWT token handling with user validation
- **Models**: Data classes for all API objects

## Compatibility

This backend is designed to be a drop-in replacement for the Python Flask backend:

- Same API endpoints and response format
- Compatible JWT tokens (can authenticate tokens from Python backend)
- Same YAML file format for data storage
- Identical error messages and status codes

## Performance

The Ktor backend offers several advantages:
- Lower memory usage compared to Python/Flask
- Better concurrent request handling
- Faster startup time
- Native JVM performance