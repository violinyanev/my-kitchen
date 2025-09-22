# Countly Analytics Setup Guide

This guide explains how to set up your own Countly analytics server and integrate it with the My Kitchen Android app.

## What is Countly?

Countly is an open-source, real-time mobile & web analytics platform that provides:
- Crash reporting and error tracking
- User behavior analytics
- Custom event tracking
- Real-time dashboard
- Privacy-focused data collection

## Table of Contents

1. [Server Setup Options](#server-setup-options)
2. [Self-Hosted Countly Installation](#self-hosted-countly-installation)
3. [Countly Cloud Setup](#countly-cloud-setup)
4. [App Configuration](#app-configuration)
5. [Testing the Integration](#testing-the-integration)
6. [Analytics Features](#analytics-features)
7. [Troubleshooting](#troubleshooting)

## Server Setup Options

### Option 1: Self-Hosted Countly (Recommended for Privacy)

Self-hosting gives you complete control over your data and complies with GDPR requirements.

### Option 2: Countly Cloud

Countly offers hosted solutions for easier setup but with data residing on their servers.

## Self-Hosted Countly Installation

### Prerequisites

- Linux server (Ubuntu 20.04+ recommended)
- 2GB+ RAM, 10GB+ storage
- Docker and Docker Compose installed
- Domain name (optional but recommended)

### Installation Steps

#### Method 1: Docker Compose (Easiest)

1. **Create installation directory:**
   ```bash
   mkdir countly-server
   cd countly-server
   ```

2. **Download Docker Compose configuration:**
   ```bash
   curl -L https://raw.githubusercontent.com/Countly/countly-server/master/docker-compose.yml -o docker-compose.yml
   ```

3. **Start Countly services:**
   ```bash
   docker-compose up -d
   ```

4. **Wait for installation (5-10 minutes):**
   ```bash
   docker-compose logs -f countly
   ```

5. **Access Countly dashboard:**
   - Open browser to `http://your-server-ip:6001`
   - Complete initial setup wizard
   - Create admin account

#### Method 2: Installation Script

1. **Download and run installation script:**
   ```bash
   wget -qO- https://raw.githubusercontent.com/Countly/countly-server/master/bin/countly.install.sh | bash
   ```

2. **Follow installation prompts**

#### Method 3: Manual Installation

For advanced users who want full control:

1. **Install Node.js 16+:**
   ```bash
   curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
   sudo apt-get install -y nodejs
   ```

2. **Install MongoDB:**
   ```bash
   wget -qO - https://www.mongodb.org/static/pgp/server-5.0.asc | sudo apt-key add -
   echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/5.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-5.0.list
   sudo apt-get update
   sudo apt-get install -y mongodb-org
   sudo systemctl start mongod
   sudo systemctl enable mongod
   ```

3. **Install Redis:**
   ```bash
   sudo apt-get install redis-server
   sudo systemctl start redis
   sudo systemctl enable redis
   ```

4. **Clone and install Countly:**
   ```bash
   git clone https://github.com/Countly/countly-server.git
   cd countly-server
   npm install
   ```

5. **Configure and start:**
   ```bash
   # Edit configuration
   cp frontend/express/config.sample.js frontend/express/config.js
   cp api/config.sample.js api/config.js
   
   # Start services
   npm start
   ```

### SSL/HTTPS Configuration

For production use, configure SSL:

1. **Install Nginx:**
   ```bash
   sudo apt-get install nginx
   ```

2. **Configure Nginx proxy:**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           proxy_pass http://localhost:6001;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

3. **Install SSL certificate (Let's Encrypt):**
   ```bash
   sudo apt-get install certbot python3-certbot-nginx
   sudo certbot --nginx -d your-domain.com
   ```

## Countly Cloud Setup

1. **Sign up for Countly Cloud:**
   - Visit [https://count.ly](https://count.ly)
   - Create account and select plan
   - Create new application

2. **Get credentials:**
   - Note your server URL
   - Copy the App Key from dashboard

## App Configuration

### Development Configuration

1. **Update build configuration:**
   ```kotlin
   // In app/build.gradle.kts, add build config fields:
   buildTypes {
       debug {
           buildConfigField("String", "COUNTLY_SERVER_URL", "\"http://your-server:6001\"")
           buildConfigField("String", "COUNTLY_APP_KEY", "\"your-app-key\"")
       }
       release {
           buildConfigField("String", "COUNTLY_SERVER_URL", "\"https://your-domain.com\"")
           buildConfigField("String", "COUNTLY_APP_KEY", "\"your-production-app-key\"")
       }
   }
   ```

2. **Enable Countly SDK dependency:**
   
   In `gradle/libs.versions.toml`, uncomment:
   ```toml
   countly = "23.12.0"
   ```
   
   In `app/build.gradle.kts`, uncomment:
   ```kotlin
   implementation(libs.countly.sdk)
   ```

3. **Update AnalyticsManager:**
   
   In `AnalyticsManager.kt`, uncomment the Countly integration code and update:
   ```kotlin
   fun initialize(serverUrl: String, appKey: String) {
       if (isInitialized) return

       try {
           val config = CountlyConfig(context, appKey, serverUrl)
               .setLoggingEnabled(BuildConfig.DEBUG) // Enable logging in debug
               .enableCrashReporting()
               .setRequiresConsent(false) // Or implement consent management
               .enableAutomaticViewTracking() // Optional: automatic screen tracking

           Countly.sharedInstance().init(config)
           isInitialized = true
           
           trackEvent("app_started")
       } catch (e: Exception) {
           e.printStackTrace()
       }
   }
   ```

4. **Update RecipesApp initialization:**
   ```kotlin
   private fun initializeAnalytics() {
       try {
           if (AnalyticsConfig.isEnabled) {
               val serverUrl = BuildConfig.COUNTLY_SERVER_URL
               val appKey = BuildConfig.COUNTLY_APP_KEY
               
               analyticsManager.initialize(serverUrl, appKey)
           }
       } catch (e: Exception) {
           e.printStackTrace()
       }
   }
   ```

### Production Configuration

1. **Set up environment variables or secure storage for credentials**
2. **Configure ProGuard rules:**
   ```
   # In proguard-rules.pro
   -keep class ly.count.android.sdk.** { *; }
   -keep class org.openudid.** { *; }
   ```

3. **Test in release build before deployment**

## Testing the Integration

### 1. Verify Connection

1. **Check logs:**
   ```bash
   adb logcat | grep -i countly
   ```

2. **Countly dashboard:**
   - Check "Real-time" section for active users
   - Look for events in "Events" section

### 2. Test Events

Trigger app actions and verify events appear:

- Launch app → `app_started` event
- Login → `auth_operation` event with `action: login`
- Create recipe → `recipe_operation` event with `action: create`
- View recipes → `recipe_operation` event with `action: list`

### 3. Test Crash Reporting

Force a crash and verify it appears in dashboard:
```kotlin
// Test crash
throw RuntimeException("Test crash for analytics")
```

## Analytics Features

### Events Being Tracked

The My Kitchen app tracks these events:

1. **App Lifecycle:**
   - App startup
   - App backgrounding/foregrounding

2. **Authentication:**
   - Login attempts (success/failure)
   - Logout events

3. **Recipe Operations:**
   - Recipe creation
   - Recipe viewing
   - Recipe editing
   - Recipe deletion
   - Recipe list viewing

4. **Navigation:**
   - Screen transitions
   - Feature usage

5. **Errors:**
   - Crash reports
   - Handled exceptions
   - API errors

### Custom Segmentation

Events include contextual data:
- User ID (if logged in)
- Recipe IDs for recipe operations
- Error types and messages
- Success/failure status

## Troubleshooting

### Common Issues

1. **Connection refused:**
   - Check server is running: `docker-compose ps`
   - Verify firewall settings
   - Check URL format (include protocol)

2. **Events not appearing:**
   - Verify app key is correct
   - Check device internet connection
   - Look for error logs
   - Ensure Countly SDK is properly initialized

3. **SSL certificate issues:**
   - Use HTTP for local testing
   - Verify certificate is valid
   - Check certificate chain

### Debug Commands

1. **Check Countly server logs:**
   ```bash
   docker-compose logs countly
   ```

2. **Test API endpoint:**
   ```bash
   curl http://your-server:6001/o/ping
   ```

3. **Check database connection:**
   ```bash
   docker-compose exec mongo mongo
   ```

### Server Maintenance

1. **Backup data:**
   ```bash
   docker-compose exec mongo mongodump --out /backup
   ```

2. **Update Countly:**
   ```bash
   docker-compose pull
   docker-compose up -d
   ```

3. **Monitor performance:**
   - Check CPU and memory usage
   - Monitor disk space
   - Review error logs regularly

## Security Considerations

1. **Network Security:**
   - Use HTTPS in production
   - Configure firewall rules
   - Restrict access to admin panel

2. **Data Privacy:**
   - Configure data retention policies
   - Implement consent management if required
   - Review data collection practices

3. **Access Control:**
   - Use strong admin passwords
   - Enable two-factor authentication
   - Regular security updates

## Advanced Configuration

### Custom Metrics

Add custom metrics to track business-specific KPIs:

```kotlin
// Track recipe complexity
analyticsManager.trackEvent("recipe_complexity", mapOf(
    "ingredients_count" to recipe.ingredients.size,
    "preparation_time" to recipe.prepTime,
    "difficulty" to recipe.difficulty
))
```

### User Profiles

Enable user profiles for better analytics:

```kotlin
// After successful login
Countly.userData.setUsername(user.email)
Countly.userData.setEmail(user.email)
Countly.userData.save()
```

### Performance Monitoring

Enable APM for performance insights:

```kotlin
val config = CountlyConfig(context, appKey, serverUrl)
    .enablePerformanceMonitoring()
    .setApplicationVersion(BuildConfig.VERSION_NAME)
```

---

For more detailed information, visit the official [Countly Documentation](https://support.count.ly/hc/en-us).

