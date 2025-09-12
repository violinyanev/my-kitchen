# Release Management Configuration

This document explains the enhanced release workflow that supports both production releases and release candidates.

## Overview

The release workflow now supports two types of releases:
1. **Release Candidates (RC)** - Built from main branch commits
2. **Production Releases** - Built from Git tags

## How it Works

### Release Candidates (Main Branch)
When code is pushed to the `main` branch, the workflow:
1. Gets the latest Git tag (e.g., `v1.2.0`)
2. Creates a release candidate version by adding `-rc` suffix (e.g., `v1.2.0-rc`) for display purposes
3. **Automatically cleans up older snapshot releases** - keeps only the most recent RC release and deletes all older ones with their tags
4. Creates a release with tag name `rc-{commit-sha}` (e.g., `rc-a1b2c3d4...`) to avoid repository tag restrictions
5. Builds the APK with `snapshotBuild=true` which:
   - Sets application ID to `com.ultraviolince.mykitchen.preview` (allows side-by-side installation)
   - Uses debug app name: "Kitchen on fire!"
   - Uses debug icon (black background vs production green)
6. Creates a GitHub release marked as "prerelease"

### Production Releases (Git Tags)
When a Git tag matching `v*.*.*` is pushed, the workflow:
1. Deletes any existing release candidate for this version
2. Builds the APK with production configuration:
   - Application ID: `com.ultraviolince.mykitchen`
   - App name: "My Kitchen"
   - Production icon (green background)
3. Creates a production GitHub release

## App Configuration Differences

| Aspect | Production Release | Release Candidate |
|--------|-------------------|-------------------|
| Application ID | `com.ultraviolince.mykitchen` | `com.ultraviolince.mykitchen.preview` |
| App Name | "My Kitchen" | "Kitchen on fire!" |
| Icon Background | Green (#3DDC84) | Black (#000000) |
| Installation | Replaces existing | Side-by-side with production |
| Tag Format | `v*.*.*` (e.g., `v1.2.0`) | `rc-{commit-sha}` (e.g., `rc-a1b2c3d4...`) |
| Version Display | Matches tag | Latest tag + `-rc` suffix |

## Build Properties

- `snapshotBuild=true`: Enables preview/RC configuration
- `versionName`: Set automatically based on Git tags
- `versionCode`: Set to Git commit count

## Usage Examples

### Creating a Release Candidate
```bash
# Push to main branch
git push origin main
# This creates v1.2.0-rc release candidate with tag rc-{commit-sha}
```

### Creating a Production Release
```bash
# Create and push a tag
git tag v1.2.1
git push origin v1.2.1
# This deletes any existing RCs and creates v1.2.1 production release
```

## File Changes Made

1. **`.github/workflows/release.yaml`**: Enhanced workflow with RC and production logic
2. **`app/build.gradle.kts`**: Added snapshot build configuration
3. **`app/src/release/res/values/strings.xml`**: Production app name
4. **`app/src/main/res/values/strings.xml`**: Added fallback app name

## Benefits

- **Safe Testing**: Release candidates can be installed alongside production
- **Clear Identification**: Different names and icons prevent confusion
- **Automated Cleanup**: Old RCs are automatically archived when production releases are created, and old snapshot releases are automatically removed when new ones are built (keeping only the latest)
- **Version Consistency**: RC versions match the upcoming production release
- **Repository Rule Compliance**: Uses commit-based tags to avoid version tag restrictions
