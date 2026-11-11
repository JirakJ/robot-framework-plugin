# Robot Framework

[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE) ![Version](https://img.shields.io/badge/version-2026.9.0-green)

> **Free & open source.** Since 2026.8.0 the plugin is completely free — every feature is unlocked for everyone, no license needed — and the source code is available under the Apache License 2.0.

Robot Framework language support for JetBrains IDEs: syntax highlighting, code completion, navigation, inspections with one-click migration fixes, a formatter, and test execution from the editor gutter.

Tracks the syntax of **Robot Framework 7.4**.

## Robot Framework 7.x syntax

- **`GROUP` blocks** (RF 7.2) — highlighting, folding (`GROUP...END`), completion, `group` live template, and `END` matching
- **Variable type conversion** (RF 7.3) — `${amount: int}`, `@{ids: list[int]}`, `&{m: dict[str, int]}`, unions and `Literal["UP", "DOWN"]` get their own colour, completion after the colon, and validation; `typedvar` live template
- **Secret variables** (RF 7.4) — completion, `secret` live template, and an inspection that rejects literal secrets in `*** Variables ***`
- **`VAR` statement** (RF 7.0, `SUITES` scope in 7.1) — scope and `separator=` completion, `var` live template, and a warning for a non-local `VAR` without a value (deprecated in RF 7.4)
- **`WHILE` limits** — completion for `limit=`, `on_limit=`, `on_limit_message=`; `while` live template
- **Inline `IF`**, **`[Setup]` on user keywords** and **`-tag` removal** (RF 7.0) are understood by the editor and the inspections
- **`*** Tasks ***`** behaves like Test Cases; **`*** Comments ***`** content is greyed out as ignored data
- **Migration fixes** — `[Return]` → `RETURN`, `Force Tags` → `Test Tags`, `WITH NAME` → `AS`, singular headers → plural, `Exit For Loop` → `BREAK`, `Continue For Loop` → `CONTINUE`, `Return From Keyword` → `RETURN`, `FOR IN ZIP` without `mode=`, literal `-tag` in `Test Tags` → `\-tag`

## Features

- **Syntax highlighting** — full RF syntax coloring for keywords, variables (`${}`, `@{}`, `&{}`, `%{}`), variable types, section headers, control structures (IF, TRY, FOR, WHILE, GROUP), tags, and embedded arguments
- **Smart code completion** — auto-complete for RF 7.x control structures, 40+ BuiltIn library keywords, Collections & String library keywords, built-in variables, settings section keywords, conversion types, and user-defined keywords/variables
- **Go to declaration** — Ctrl+Click (Cmd+Click on Mac) navigation to keyword and variable definitions with normalized matching (handles `My Keyword`, `my_keyword`, `MyKeyword`)
- **Find references** — locate all usages of keywords and variables across your project
- **Inline documentation** — hover documentation for keywords, plus `arg0:`, `arg1:` inlay hints for positional arguments
- **Code folding** — collapse/expand test cases, keywords, and section blocks
- **Structure view** — hierarchical overview of `.robot` file structure with PSI-based model
- **Breadcrumbs navigation** — path-based navigation in the editor
- **Annotators & inspections** — real-time detection of undefined keywords, undefined variables, duplicate keywords, missing resource files, and embedded argument validation
- **Block structure inspection** — unclosed `IF`/`FOR`/`WHILE`/`TRY`/`GROUP` and orphan `END` (inline `IF` correctly needs no `END`)
- **Test name length inspection** — warning for overly long test names
- **Empty test case inspection** — detects test cases with no steps, with an *Add `No Operation`* fix
- **Code formatter** — reformat a suite with `Ctrl+Alt+Shift+R`: canonical section headers, tabs → spaces, trimmed trailing whitespace, normalized blank lines (idempotent)
- **Quality inspections + quick fixes** — duplicate test-case names, tab separators (fix: convert to spaces), trailing whitespace (fix: remove), unresolved `TODO`/`FIXME`/`XXX` markers, and unused suite variables
- **Rename refactoring** — rename keywords with automatic propagation across files
- **Test execution** — Run icon in the gutter of every test case; run configurations with `--test`, `--include`/`--exclude`, `--outputdir`, extra arguments, interpreter and working directory
- **Run with DEBUG log** — re-run with `--loglevel DEBUG` (Ctrl+Shift+F9) when a failure needs the detail
- **Generate Keyword** — turns the keyword call under the caret into a skeleton in the `*** Keywords ***` section
- **Live templates** — snippet support for common RF patterns
- **Customizable colors** — configurable syntax highlighting via Color Settings page

## Installation

1. **JetBrains Marketplace**: Settings → Plugins → Marketplace → Search "Robot Framework"
2. **Manual**: Download from [Releases](https://github.com/JirakJ/robot-framework-plugin/releases) → Settings → Plugins → ⚙️ → Install from disk

## Requirements

- IntelliJ IDEA 2024.2+ (build 242+)
- Any JetBrains IDE (IntelliJ, WebStorm, PyCharm, etc.)
- Robot Framework installed in your project environment

## Configuration

Settings → Tools → Robot Framework

Customize syntax highlighting colors via Settings → Editor → Color Scheme → Robot Framework.

## Usage

### Run a Test
Click the Run icon in the gutter next to a test case name, or press **Ctrl+Shift+F10** to run the test under the caret (the whole suite when the caret is outside a test). **Ctrl+Shift+F9** re-runs it with `--loglevel DEBUG`.

Robot Framework is started as `<interpreter> -m robot` with an argument list — never through a shell — so paths and test names containing spaces or shell metacharacters are safe. Set the interpreter per configuration or globally under Settings → Tools → Robot Framework.

### Navigate to Keywords
Place your cursor on any keyword and press **Ctrl+Click** (Cmd+Click on Mac) to jump to its definition. Works across files with normalized keyword matching.

### Generate Keywords
Put the caret on a keyword call that does not exist yet and choose **Generate Keyword** from the context menu. The skeleton is added to the `*** Keywords ***` section, which is created if the file has none.

### Format a Suite
Open a `.robot`/`.resource` file → **Robot Framework → Format Robot File** (`Ctrl+Alt+Shift+R`), or right-click → **Format Robot File**. Indent size and header normalization are configurable under Settings → Tools → Robot Framework.

## Screenshots

| Robot Framework 7.4 syntax | Conversion type completion |
| --- | --- |
| ![RF 7.4 syntax](docs/screenshots/05-rf74-syntax.png) | ![Type completion](docs/screenshots/06-type-completion.png) |

| Inspections | One-click migration |
| --- | --- |
| ![Inspections](docs/screenshots/01-code-inspections.png) | ![Quick fixes](docs/screenshots/02-quick-fixes.png) |

| Formatter (before / after) | Formatter settings |
| --- | --- |
| ![Formatter](docs/screenshots/03-formatter-before-after.png) | ![Settings](docs/screenshots/04-settings.png) |

The capture kit (sample project, renderer and per-shot guide) lives in
[`docs/screenshots`](docs/screenshots/SCREENSHOTS.md); regenerate with
`python3 docs/screenshots/src/render.py`.

## Building from Source

```bash
./gradlew buildPlugin
```

## Contributing

Issues and pull requests are welcome at https://github.com/JirakJ/robot-framework-plugin.

## License

Copyright 2026 Jakub Jirák. Licensed under the [Apache License, Version 2.0](LICENSE).
