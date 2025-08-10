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
   - **Dry run**: Preview what would be released without creating actual release
   - **First release**: Create initial v1.0.0 tag (only needed for very first release)

The system will automatically:
1. Analyze commits since last release using conventional commit format
2. Determine appropriate version bump based on commit types
3. Create git tag and changelog
4. Trigger APK build via existing release workflow

## Examples

Example commits and their version impact:
- `feat: add recipe sharing functionality` → minor version bump (1.0.0 → 1.1.0)
- `fix: resolve app crash on startup` → patch version bump (1.0.0 → 1.0.1)  
- `feat!: redesign database schema` → major version bump (1.0.0 → 2.0.0)
- `docs: update README with new features` → no version bump
