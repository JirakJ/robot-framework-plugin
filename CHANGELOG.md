# Changelog

All notable changes to the Robot Framework plugin will be documented in this file.

## [2026.6.0] - 2026-08-03

Makes the advertised test execution real and removes the features that were only
pretending to exist.

### Added
- **Test execution that runs your tests.** The run configuration now carries a suite file or directory, `--test`, `--include`/`--exclude` tags, `--outputdir`, extra arguments, a Python interpreter and a working directory, with a real settings form and `checkConfiguration` validation. Robot is started as `<interpreter> -m robot` with an argument list handed straight to `GeneralCommandLine`, so nothing is ever interpreted by a shell — a path or test name containing spaces, quotes, `;` or `$(…)` stays a single argument.
- **Run gutter icons** on every test case and task (`RobotRunLineMarkerContributor`).
- **Run from context** — `RobotRunConfigurationProducer` builds a configuration from the editor, the project view or a right-click: inside a test it runs that test, otherwise the whole suite.
- **Run Test** (`Ctrl+Shift+F10`) and **Run Test with DEBUG Log** (`Ctrl+Shift+F9`), the latter adding `--loglevel DEBUG`.
- **Generate Keyword** now works: it takes the keyword call under the caret, refuses duplicates using Robot Framework's own name matching, creates the `*** Keywords ***` section when the file has none, and leaves the caret on the generated step.
- New pure engines `RobotStructure` (test cases, tasks, keywords, "which test is at this offset") and `RobotKeywordGenerator`, both fully unit tested.

### Fixed
- **Suite settings were never highlighted.** `Library`, `Documentation`, `Test Tags`, `Suite Setup` and the rest rendered as plain text, because the lexer only recognised `[Bracketed]` settings. Multi-word settings are matched at a cell boundary, so a test named `Library Loads Fast` is not mistaken for the `Library` setting.
- **The RF 7.3 variable type was invisible.** `ROBOT_VARIABLE_TYPE` fell back to `CLASS_REFERENCE`, which the IntelliJ Light scheme leaves unstyled, so the type rendered in the variable's own colour. It now falls back to `METADATA`.

### Removed
- **Placeholder features.** The "Interactive REPL" action opened a message box reading "FREE Feature!", the "Web Inspector" tool window was a static `JLabel` listing things it could not do, and the "Robot Framework" tool window was a label reading "Test Results". None of them did anything; test output now goes to the normal run console. The custom `programRunner` was redundant with the platform's default runner, and the empty `RobotRunConfigurationOptions` is gone.
- Claims of a step-through debugger, a performance profiler, parallel execution, a REPL and a web inspector are removed from the plugin description and the README. **The paid tier now has no exclusive features** — see `MARKETPLACE_LISTING.md` for the open decision.

### Changed
- 309 unit tests (was 260), all green.

## [2026.5.0] - 2026-08-01

Brings the plugin up to the syntax of Robot Framework 7.4.

### Added
- **GROUP blocks** (RF 7.2) — new `GROUP` marker in the lexer and highlighter, folding with a `GROUP...END` placeholder, completion, a `group` live template, and END matching in the block-structure inspection.
- **Variable type conversion** (RF 7.3) — `${amount: int}`, `@{ids: list[int]}`, `&{m: dict[str, int]}`, unions and `Literal["UP", "DOWN"]` are parsed as one token, the type is painted with its own colour, and completion offers the built-in conversion types right after the colon. `typedvar` live template added.
- **Secret variables** (RF 7.4) — `Secret` offered in type completion, a `secret` live template, and an inspection that rejects a literal value for a `Secret` variable in the Variables section (RF only accepts a value read from another variable or the environment).
- **VAR statement** (RF 7.0/7.1) — completion for `scope=` including the RF 7.1 `SUITES` scope and for `separator=`, a `var` live template, and a warning when a non-local `VAR` is created without a value (deprecated in RF 7.4).
- **WHILE options** — completion for `limit=`, `on_limit=` and `on_limit_message=`, plus a `while` live template.
- **Deprecated Robot Framework syntax** inspection with one-click fixes: `[Return]` → `RETURN` (RF 7.0), `Force Tags` → `Test Tags` (RF 6.0), `Default Tags`, `WITH NAME` → `AS` (RF 6.0), singular section headers → plural (RF 6.0), `Exit For Loop` → `BREAK`, `Continue For Loop` → `CONTINUE`, `Return From Keyword` → `RETURN`, `Run Keyword Unless`, `FOR ... IN ZIP` without `mode=` (RF 7.0), and a literal `-tag` in `Test Tags` → `\-tag` (RF 7.2).
- **Block structure** inspection — unclosed `IF`/`FOR`/`WHILE`/`TRY`/`GROUP` and orphan `END`, with inline `IF` (RF 7.0) correctly treated as needing no `END`.
- **Data before the first section header** inspection.
- **Control structure documentation** — hovering `GROUP`, `VAR`, `WHILE`, `RETURN` and friends shows what the marker does and which release introduced it.
- **`*** Tasks ***` and `*** Comments ***`** — Tasks behave exactly like Test Cases everywhere, and content of a Comments section is greyed out as the ignored data it is.

### Fixed
- **Section headers were never tokenized.** The lexer consumed one `*` at a time, so `*** Settings ***` produced three header tokens and an identifier. Every feature keyed on section headers — highlighting, folding, structure view, completion of file-local variables and keywords, go-to declaration, rename, documentation — was silently degraded. Headers are now single tokens.
- **Token ranges were wrong.** `getTokenStart()` returned the start of the buffer for every token instead of the start of the current token.
- **False "Content must be inside a section" errors.** The PSI-walking syntax-error annotator flagged practically every identifier; it has been replaced by two analyzer-backed inspections.
- **Empty test case detection** was a PSI heuristic that never fired, and its quick fix did nothing. Both are now text-based, tested, and the fix really inserts `No Operation`.
- **Duplicate registrations removed** — two syntax highlighter factories and two documentation providers were registered for the same language.
- **Typed variables no longer look unused** — `${RETRIES: int}` is now matched against `${RETRIES}` usages.

### Changed
- **`.txt` files are no longer claimed** as Robot Framework data. Robot Framework dropped the plain-text and TSV formats years ago, so the plugin was taking over every `.txt` file in the project — and now flagging them — for a format it cannot run. `.robot` and `.resource` remain.
- Completion no longer suggests deprecated syntax (`[Return]`, `Force Tags`, `Exit For Loop`, …).
- Colour settings page moved to the highlighter that is actually in use and gained entries for control structures, tags, embedded arguments and variable types.
- 258 unit tests (was 163), all green.

## [2026.4.0] - 2026-06-14

### Added
- **Robot file formatter** — Reformat the current suite via *Robot Framework → Format Robot File* (`Ctrl+Alt+Shift+R`): canonicalizes section headers (`*** test cases ***` → `*** Test Cases ***`), expands tabs to spaces, strips trailing whitespace, collapses blank-line runs, and guarantees a blank line before each section header. Formatting is idempotent.
- **Code-quality inspections** — Duplicate test-case name (error), tab character used for separation (warning, with quick fix), trailing whitespace (weak warning, with quick fix), unresolved `TODO`/`FIXME`/`XXX` markers (weak warning), and unused suite variables (off by default).
- **Quick fixes** — *Convert tabs to spaces* and *Remove trailing whitespace*.
- **Formatter settings** — Indent size and header normalization under Settings → Tools → Robot Framework.

### Technical
- New dependency-free, fully unit-tested engines `RobotFormatter` and `RobotCodeAnalyzer` (32 new tests); inspections delegate to the analyzer via `checkFile` so detection logic stays IDE-independent.

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
