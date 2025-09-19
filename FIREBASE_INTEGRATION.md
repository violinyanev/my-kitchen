# Firebase Integration Documentation

This document describes the Firebase integration implemented in the My Kitchen Android application.

## Overview

The Firebase integration adds analytics and crash reporting capabilities to the My Kitchen app using Firebase Analytics and Firebase Crashlytics.

## Features Implemented

### 1. Firebase Analytics
- **App Startup Tracking**: Logs when the app starts
- **User Authentication Events**: Tracks login/logout events
- **Recipe Interactions**: Tracks recipe creation, viewing, and deletion
- **Custom Event Logging**: Flexible event logging with custom parameters
- **User Properties**: Sets user properties for better analytics segmentation

### 2. Firebase Crashlytics
- **Crash Reporting**: Automatic crash reporting with context
- **Non-Fatal Error Logging**: Log exceptions that don't crash the app
- **Custom Error Context**: Categorized error logging for different app areas
- **User Identification**: Associate crashes with specific users
- **Custom Keys**: Add custom metadata to crash reports

### 3. Firebase Manager
- **Centralized Management**: Single point of control for all Firebase services
- **Initialization**: Handles Firebase app initialization
- **Service Coordination**: Coordinates between Analytics and Crashlytics
- **Error Handling**: Centralized error logging across the app

## Integration Points

### App Initialization
```kotlin
class RecipesApp : Application() {
    private val firebaseManager: FirebaseManager by inject()
    
    override fun onCreate() {
        super.onCreate()
        // ... Koin setup
        firebaseManager.initialize()
    }
}
```

### Repository Layer
The Firebase integration is wired into the existing repository layer to track:
- Recipe creation events
- Recipe viewing events
- Recipe deletion events
- Error handling for all operations

### Backend Service
The RecipeServiceWrapper now includes Firebase integration for:
- Login/logout event tracking
- Backend error logging
- User identification for crash reports

## Configuration

### Google Services Configuration
The app includes a placeholder `google-services.json` file that supports both:
- Production package: `com.ultraviolince.mykitchen`
- Debug package: `com.ultraviolince.mykitchen.debug`

### For Production Use
To use Firebase in production:

1. **Create a Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use existing one
   - Add Android app with package name `com.ultraviolince.mykitchen`

2. **Replace Configuration**:
   - Download the real `google-services.json` from Firebase Console
   - Replace `app/google-services.json` with the real configuration

3. **Enable Services**:
   - Enable Analytics in Firebase Console
   - Enable Crashlytics in Firebase Console

## Analytics Events

### Automatic Events
- `app_startup`: When the app starts
- `login`: When user logs in (with method parameter)
- `logout`: When user logs out

### Recipe Events
- `recipe_created`: When a new recipe is created
- `recipe_viewed`: When a recipe is viewed
- `recipe_deleted`: When a recipe is deleted

### Custom Events
The `FirebaseAnalyticsService` supports logging custom events with parameters.

## Error Categories

### Crashlytics Error Types
- **Recipe Errors**: Issues with recipe operations
- **Backend Errors**: Network and API-related errors
- **Database Errors**: Local database operation errors

## Dependencies Added

### Gradle Dependencies
```kotlin
// Firebase BOM for version management
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-crashlytics")
```

### Gradle Plugins
```kotlin
// Google Services plugin
id("com.google.gms.google-services")
// Crashlytics plugin  
id("com.google.firebase.crashlytics")
```

## Testing

The Firebase integration includes comprehensive unit tests:
- `FirebaseManagerTest`: Tests the central Firebase manager
- `FirebaseAnalyticsServiceTest`: Tests analytics event logging
- `FirebaseCrashlyticsServiceTest`: Tests crash reporting functionality

All tests use MockK for mocking Firebase dependencies and verify correct method calls.

## Architecture Benefits

### Minimal Impact
- **Non-Breaking**: Existing functionality unchanged
- **Dependency Injection**: Firebase services are injected through Koin
- **Error Isolation**: Firebase errors don't affect app functionality
- **Modular Design**: Firebase services can be easily disabled or replaced

### Observability
- **Usage Analytics**: Understand how users interact with recipes
- **Error Tracking**: Identify and fix issues before they affect users
- **Performance Monitoring**: Track app startup and performance metrics

## Future Enhancements

Potential future Firebase integrations:
- **Firebase Remote Config**: Feature flags and A/B testing
- **Firebase Cloud Messaging**: Push notifications for recipe sharing
- **Firebase Authentication**: Alternative authentication method
- **Firebase Cloud Firestore**: Cloud-based recipe storage option

## Privacy Considerations

- Analytics data collection can be disabled per Firebase settings
- Crashlytics collection can be programmatically controlled
- No personal recipe data is sent to Firebase Analytics
- User IDs are only set when users explicitly log in to the backend

## Troubleshooting

### Common Issues
1. **Build Failures**: Ensure `google-services.json` includes both production and debug package names
2. **Analytics Not Working**: Verify Firebase project configuration and network connectivity
3. **Crashlytics Not Reporting**: Check that Crashlytics is enabled in Firebase Console

### Debug Information
- Firebase initialization is logged during app startup
- Analytics events are visible in Firebase Console with up to 24-hour delay
- Crashlytics reports appear in Firebase Console immediately

