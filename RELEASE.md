# My Kitchen Release Automation

This document describes the automated release process for My Kitchen.

## Release Process

The automated release system uses semantic-release to analyze commit messages and automatically determine version bumps.

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

1. Navigate to GitHub Actions
2. Select "Create Release" workflow
3. Click "Run workflow"
4. Choose options:
   - **Dry run**: Preview what would be released
   - **First release**: Create initial v1.0.0 tag

The system will automatically:
1. Analyze commits since last release
2. Determine appropriate version bump
3. Create git tag and changelog
4. Trigger APK build via existing release workflow