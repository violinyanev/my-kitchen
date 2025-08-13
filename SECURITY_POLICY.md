# Security Policy

## Supported Versions

We provide security updates for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| Latest release   | ✅ |
| Previous release | ✅ |
| Older versions   | ❌ |

## Reporting a Vulnerability

If you discover a security vulnerability in My Kitchen, please report it responsibly:

### For Security Issues
1. **DO NOT** create a public GitHub issue for security vulnerabilities
2. Send an email to the maintainers with details of the vulnerability
3. Include steps to reproduce the issue
4. Allow time for the issue to be addressed before public disclosure

### What to Report
- Authentication bypasses
- Data exposure vulnerabilities
- Injection vulnerabilities (SQL, command, etc.)
- Cross-site scripting (XSS)
- Cross-site request forgery (CSRF)
- Insecure cryptographic implementations
- Path traversal vulnerabilities
- Any other security-related issues

### Response Timeline
- **Acknowledgment**: Within 48 hours
- **Initial Assessment**: Within 1 week
- **Fix Development**: Varies based on severity
- **Release**: Critical issues prioritized

## Security Measures

This project implements several security measures:

### Automated Security Analysis
- **Static Code Analysis**: Bandit, Detekt, Semgrep
- **Dependency Scanning**: Safety, pip-audit, dependency review
- **Container Security**: Trivy vulnerability scanning
- **Secrets Detection**: GitLeaks with custom rules
- **License Compliance**: Automated license checking

### Development Security
- All dependencies are regularly updated
- Security-focused code review practices
- Automated security testing in CI/CD
- Container security best practices

### Runtime Security
- JWT-based authentication
- Input validation and sanitization
- Secure communication (HTTPS recommended)
- Environment-based configuration

## Security Best Practices

### For Users
- Keep the application updated to the latest version
- Use strong, unique passwords
- Enable HTTPS when self-hosting
- Regularly backup your data
- Monitor access logs for suspicious activity

### For Contributors
- Follow secure coding practices
- Never commit secrets or credentials
- Run security analysis tools locally
- Keep dependencies updated
- Review security implications of changes

### For Self-Hosters
- Use HTTPS with valid certificates
- Implement proper firewall rules
- Regular security updates
- Monitor system logs
- Use secure database configurations
- Implement proper backup strategies

## Third-Party Dependencies

We regularly monitor and update third-party dependencies for security vulnerabilities. Our automated security analysis includes:

- Python package vulnerability scanning
- Android/Kotlin dependency analysis
- Container base image security scanning
- License compliance monitoring

## Security Updates

Security updates are released as soon as possible after discovery and verification. Updates are communicated through:

- GitHub Releases with security advisory
- Security badges on README
- Documentation updates

## Additional Resources

- [Security Analysis Documentation](SECURITY.md)
- [GitHub Security Advisories](https://github.com/violinyanev/my-kitchen/security/advisories)
- [Automated Security Workflow](.github/workflows/security.yml)

Thank you for helping keep My Kitchen secure!
