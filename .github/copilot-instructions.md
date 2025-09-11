# ⚠️ DEPRECATED: Instructions moved to AGENTS.md

> **This file has been superseded by [`AGENTS.md`](../AGENTS.md) in the repository root.**
> 
> **Please use [`AGENTS.md`](../AGENTS.md) as the canonical source for all custom instructions.**
> 
> This file is kept temporarily for compatibility but will be removed in a future update.

---

# My Kitchen - Recipe Management Application

My Kitchen is a free and open source recipe management application featuring an Android mobile app and a self-hosted Python Flask backend server. Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the information here.

## 📍 For Complete Instructions

**→ See [`AGENTS.md`](../AGENTS.md) for the complete and up-to-date instructions including:**

- 🚨 Critical validation requirements
- 🔧 Development setup and build commands
- 🧪 Testing procedures (unit, screenshot, instrumented)
- 📋 Code quality standards and troubleshooting
- 🏗️ Architecture notes and project structure
- 🚀 CI/CD pipeline requirements

## 🚨 CRITICAL: ALWAYS Validate Before Committing
**Every PR must pass ALL GitHub Actions checks. Use `./scripts/validate-pr.sh` before committing ANY changes to ensure green checks.**

**⚠️ INSTRUMENTED TESTS ARE MANDATORY**: Work is NOT complete until both unit tests AND instrumented tests pass. GitHub Actions runs instrumented tests on Android API levels 28, 31, 34, and 35. If you cannot run instrumented tests locally, ensure all other validations pass and let GitHub Actions verify instrumented tests automatically.

---

**📖 For detailed information, workflows, troubleshooting, and complete development guidelines, please refer to [`AGENTS.md`](../AGENTS.md).**
