# Changelog

All notable changes to the Robot Framework plugin will be documented in this file.

## [2026.3.6] - 2026-03-27

### Added
- **Freemium licensing** — Added `product-descriptor` for JetBrains Marketplace freemium model (code: `PROBOTFRAMEWORK`)

## [1.0.0] - 2024-01-15

### Added

#### Features
- **Advanced Syntax Highlighting** - RF-specific tokenization for keywords, variables, and arguments
- **IntelliSense Auto-completion** - Smart completion for keywords, variables, and libraries
- **Code Navigation** - Go to Definition, Find References, Quick Documentation
- **Web Inspector** - Visual element selection with automatic locator generation (CSS/XPath)
- **Interactive REPL** - Test keywords without creating full test files
- **Basic Test Execution** - Run and debug tests with breakpoint support
- **Smart Documentation** - Hover docs, keyword signatures, embedded library documentation

### Technical Implementation
- Built with IntelliJ Platform SDK 2024.1+
- Java 17 compatibility
- Gradle build system with automatic version incrementing
- Comprehensive plugin.xml configuration
- Extensible architecture for future features

## [Unreleased]

### Planned Features
- Python keyword debugging with step-into support
- Locator validation against live pages
- Historical test execution tracking
- Flaky test detection
- GitLab CI/GitHub Actions templates
- TestRail/Zephyr integration
- AI-powered test generation
- Self-healing locators

---

*For more information, visit [https://jakubjirak.com](https://jakubjirak.com)*
## [2025.11.20]
### Added
- Restored Robot Framework plugin
- Fixed build configuration
