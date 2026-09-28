# Contributing to Krypton Vault

Thank you for your interest in contributing to Krypton Vault! As an air-gapped, zero-knowledge security project, we welcome community contributions, bug reports, and enhancements.

## Development Setup

1. **Prerequisites**:
   - Android Studio (Ladybug or newer)
   - JDK 17
   - Android SDK with Platform API 36

2. **Clone & Build**:
   ```bash
   git clone https://github.com/your-username/krypton-vault.git
   cd krypton-vault
   ./gradlew assembleDebug
   ```

## Contribution Guidelines

1. **Air-Gapped Guarantee**: Never introduce any network permissions (`android.permission.INTERNET`) or telemetry into the application manifest or dependencies.
2. **Code Style**: Follow standard Kotlin coding conventions and Jetpack Compose best practices.
3. **Commit Messages**: Write clear, descriptive commit messages summarizing changes.
4. **Pull Requests**:
   - Fork the repository and create a descriptive branch.
   - Test builds thoroughly locally before opening a pull request.
   - Detail the changes and rationale in the PR description.
