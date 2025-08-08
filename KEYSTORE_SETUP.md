# Android Keystore Setup

## Security Note

For security reasons, keystore credentials are **not** stored in this repository. Instead, they should be configured locally or via environment variables.

## Local Development Setup

1. Copy the template file:
   ```bash
   cp keystore.properties.template keystore.properties
   ```

2. Edit `keystore.properties` with your actual keystore credentials:
   ```properties
   storeFile=app/keystore/release.keystore
   storePassword=YOUR_ACTUAL_STORE_PASSWORD
   keyAlias=mykitchen
   keyPassword=YOUR_ACTUAL_KEY_PASSWORD
   ```

3. The `keystore.properties` file is gitignored and will not be committed.

## CI/CD Setup

For automated builds, set these environment variables:

- `KEYSTORE_FILE`: Path to keystore file (default: `app/keystore/release.keystore`)
- `KEYSTORE_PASSWORD`: Store password
- `KEY_ALIAS`: Key alias (default: `mykitchen`)
- `KEY_PASSWORD`: Key password

## Fallback Behavior

If release keystore credentials are not available, the build will automatically fall back to debug signing with a warning message.