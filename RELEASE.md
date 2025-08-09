# My Kitchen Release Automation

This document describes the automated release process for My Kitchen.

## Release Process

The automated release system uses Google's release-please action with manifest-based configuration to analyze commit messages and automatically determine version bumps.

### Configuration Files

- `.release-please-config.json` - Contains release configuration including changelog sections and extra files to update
- `.release-please-manifest.json` - Tracks the current version of the project
- `version.txt` - Simple version file that gets updated with each release

### Supported Commit Types

- `feat:` - New features (minor version bump)
- `fix:` - Bug fixes (patch version bump)
- `perf:` - Performance improvements (patch version bump)
- `refactor:` - Code refactoring (patch version bump)
- `build:` - Build system changes (patch version bump)
- `docs:` - Documentation only changes (no version bump)
- `style:` - Code style changes (no version bump)
- `test:` - Test changes (no version bump)
- `ci:` - CI configuration changes (no version bump)
- `chore:` - Maintenance tasks (no version bump)

### Breaking Changes

Add `!` after the type for breaking changes (major version bump):
- `feat!:` - Breaking feature change
- `fix!:` - Breaking bug fix

## Usage

The release process is fully automated using manifest-based configuration:

1. **Make changes** with conventional commit messages (see supported types above)
2. **Push to main branch** - the workflow automatically detects releasable changes using the manifest
3. **Review the release PR** - if there are releasable changes, a PR will be created with:
   - Updated version number in `.release-please-manifest.json`
   - Updated `version.txt` file
   - Updated `app/build.gradle.kts` with new version
   - Generated changelog with organized sections
   - All changes since last release
4. **Merge the release PR** - this automatically:
   - Creates a GitHub release with the new version tag
   - Builds and uploads the APK to the release
   
No manual workflow dispatch needed - everything happens automatically based on your commits and the manifest configuration!

## Examples

Example commits and their version impact:
- `feat: add recipe sharing functionality` → minor version bump (1.0.0 → 1.1.0)
- `fix: resolve app crash on startup` → patch version bump (1.0.0 → 1.0.1)  
- `feat!: redesign database schema` → major version bump (1.0.0 → 2.0.0)
- `docs: update README with new features` → no version bump