# Security Analysis Documentation

This document describes the automated static security analysis system implemented for the My Kitchen project.

## Overview

The security analysis system provides comprehensive automated security checks for both the Android app (Kotlin) and the backend server (Python Flask). It runs on every push and pull request to the main branch, as well as on a weekly schedule.

## Security Workflow Components

### 1. Backend Security Analysis (Python)

**Tools Used:**
- **Bandit**: Python static security analyzer that identifies common security issues
- **Safety**: Checks Python dependencies against known security vulnerabilities
- **pip-audit**: Audits Python packages for known vulnerabilities (alternative/complement to Safety)
- **Semgrep**: Static analysis tool with custom security rules for Flask applications

**What it checks:**
- Hardcoded secrets and credentials
- SQL injection vulnerabilities
- Insecure use of cryptographic functions
- Flask-specific security issues (debug mode, CORS configuration, etc.)
- Known vulnerabilities in Python dependencies
- Unsafe YAML loading
- Insecure transport configurations

### 2. Android Security Analysis (Kotlin)

**Tools Used:**
- **Detekt**: Kotlin static analysis tool (already configured, enhanced for security)
- **Custom secret scanning**: Searches for hardcoded secrets in Android code

**What it checks:**
- Kotlin code quality and security issues
- Hardcoded API keys, passwords, and tokens in Android code
- Security-related coding patterns

### 3. Container Security Analysis

**Tools Used:**
- **Trivy**: Comprehensive container security scanner

**What it checks:**
- Known vulnerabilities in base Docker images
- Misconfigurations in Dockerfile
- Insecure dependencies in container layers
- High and critical severity vulnerabilities

### 4. Secrets and License Compliance

**Tools Used:**
- **GitLeaks**: Detects secrets, passwords, and API keys in Git repositories
- **pip-licenses**: Analyzes Python package licenses
- **Gradle dependencies**: Reports on Android dependency licenses

**What it checks:**
- Accidentally committed secrets, API keys, passwords
- License compatibility of all dependencies
- Compliance with open source license requirements

## Configuration Files

### `.gitleaks.toml`
Configures GitLeaks secret detection with:
- Custom rules for Flask secret keys, JWT secrets, database URLs
- Allowlist for test files and build configurations
- Project-specific patterns

### `pyproject.toml`
Configures Bandit Python security analysis with:
- Scan paths and exclusions
- Confidence and severity levels
- Custom rules for the project

### `.semgrep.yml`
Custom Semgrep rules for Flask security including:
- Flask debug mode detection
- Hardcoded secret detection
- SQL injection patterns
- CORS misconfiguration
- Unsafe YAML loading

## Running Security Analysis

### Automated Execution
The security analysis runs automatically:
- On every push to main branch
- On every pull request to main branch
- Weekly on Mondays at 2 AM UTC
- Can be manually triggered via GitHub Actions UI

### Local Execution

To run security analysis locally:

#### Backend Security
```bash
# Install tools
pip install bandit[toml] safety pip-audit semgrep

# Run individual tools
bandit -r backend/image/
safety check
pip-audit
python -m semgrep --config=auto backend/image/
```

#### Secrets Scanning
```bash
# Install GitLeaks
# See: https://github.com/gitleaks/gitleaks#installation

# Run scan
gitleaks detect --config .gitleaks.toml
```

#### Container Security
```bash
# Build image
cd backend/image && docker build -t my-kitchen-backend .

# Install Trivy
# See: https://aquasecurity.github.io/trivy/latest/getting-started/installation/

# Scan image
trivy image my-kitchen-backend
```

## Security Reports

All security tools generate reports that are:
- Displayed in the GitHub Actions workflow logs
- Saved as workflow artifacts (retained for 30 days)
- Available in both JSON and human-readable formats

### Report Locations
- **Backend Security**: `backend-security-reports` artifact
- **Android Security**: `android-security-reports` artifact
- **Container Security**: `container-security-reports` artifact
- **Compliance**: `compliance-reports` artifact
- **Summary**: `security-summary` artifact

## Handling Security Issues

### Severity Levels
- **HIGH/CRITICAL**: Must be fixed before merging
- **MEDIUM**: Should be reviewed and addressed
- **LOW**: Can be reviewed and addressed in follow-up

### False Positives
If a security tool reports a false positive:

1. **For Bandit**: Add `# nosec` comment or configure in `pyproject.toml`
2. **For GitLeaks**: Add pattern to allowlist in `.gitleaks.toml`
3. **For Semgrep**: Add `# nosemgrep` comment or configure rules
4. **For Trivy**: Use `.trivyignore` file if needed

### Common Issues and Solutions

#### Hardcoded Secrets
- Use environment variables or secure secret management
- For Android: Use BuildConfig fields or encrypted SharedPreferences
- For Python: Use environment variables or secure vaults

#### Dependency Vulnerabilities
- Update vulnerable dependencies to patched versions
- If update not possible, assess risk and document decision
- Consider alternative packages if available

#### Container Vulnerabilities
- Update base Docker images regularly
- Use minimal/distroless base images when possible
- Remove unnecessary packages and tools

## Integration with Development Workflow

### Pull Request Checks
Security analysis results are integrated into the PR review process:
- Failed security checks prevent merging
- Security reports are available as PR artifacts
- Reviewers can assess security impact of changes

### Continuous Monitoring
Weekly automated scans ensure ongoing security:
- Detect new vulnerabilities in existing dependencies
- Monitor for accidentally committed secrets
- Track security posture over time

## Best Practices

### For Developers
1. Run security tools locally before committing
2. Never commit secrets, API keys, or passwords
3. Keep dependencies updated regularly
4. Review security analysis results in PRs
5. Follow secure coding practices for Flask and Android

### For Maintainers
1. Review and triage security findings regularly
2. Update security tool configurations as needed
3. Monitor for new security tools and best practices
4. Ensure security policies are enforced consistently
5. Document security decisions and exceptions

## Future Enhancements

Potential improvements to consider:
- **DAST (Dynamic Application Security Testing)**: Test running applications
- **Dependency Review Action**: GitHub's native dependency vulnerability checking
- **CodeQL**: GitHub's semantic code analysis
- **License compliance automation**: Automated license policy enforcement
- **Security metrics dashboard**: Track security posture over time
- **Integration with security platforms**: SIEM, vulnerability management systems
