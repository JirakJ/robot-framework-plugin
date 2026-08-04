# JetBrains Marketplace Listing — Robot Framework Professional

---

## 📝 Plugin Information

| Field       | Value                                  |
|-------------|----------------------------------------|
| **Name**    | Robot Framework Professional           |
| **Plugin ID** | `com.jakubjirak.robotframework`     |
| **Version** | 2026.6.2                               |
| **Category** | Test Automation / Languages & Frameworks |
| **Tags**    | Robot Framework, Test Automation, RPA, Test Runner, Inspections |

---

## 🎯 Short Description

> Robot Framework 7.4 IDE support — syntax highlighting, smart completion, navigation, inspections with one-click migration fixes, a formatter, and test execution from the gutter.

---

## 📖 Full Description

### 🤖 The Ultimate Robot Framework Experience for JetBrains IDEs

**The Problem:** Robot Framework development in JetBrains IDEs lacks proper language support. Developers struggle with plain-text editing, no navigation, no completion, and no way to run a single test without leaving the IDE.

**The Solution:** Robot Framework Professional brings full language intelligence to your favorite JetBrains IDE — Robot Framework 7.4 syntax, smart completion, navigation, inspections that migrate deprecated syntax for you, a formatter, and test execution straight from the editor gutter.

---

### ✨ Key Features

#### 🤖 Robot Framework 7.4 Syntax

- 🧩 **GROUP blocks** (RF 7.2) — highlighting, folding, completion, live template, and END matching
- 🏷️ **Variable type conversion** (RF 7.3) — `${amount: int}`, `@{ids: list[int]}`, unions and `Literal[...]` highlighted, completed, and validated
- 🔐 **Secret variables** (RF 7.4) — completion, live template, and a check that rejects literal secrets
- 📦 **VAR statement** (RF 7.0/7.1) — completion for every scope including `SUITES`
- 🧭 **Migration fixes** — one-click replacements for `[Return]`, `Force Tags`, `WITH NAME`, singular section headers, and the deprecated loop keywords

#### ✨ Everything, for everyone

- 🎨 **Syntax Highlighting** — Markers, typed variables, tags, section headers, and comments with full semantic coloring
- 🧠 **Smart Code Completion** — RF 7.x control structures, conversion types, BuiltIn, Collections, and String library completions
- 🔗 **Go to Declaration** — Navigate to keyword definitions with normalized matching
- 🔍 **Find References** — Locate all usages of keywords and variables across your project
- 📖 **Inline Documentation** — Hover documentation with parameter hints
- 📁 **Code Folding** — Collapse test cases, keywords, and sections
- 🏗️ **Structure View** — Hierarchical outline of test suites, test cases, and keywords
- 🍞 **Breadcrumbs** — Contextual breadcrumb navigation in the editor
- ⚠️ **Inspections** — Undefined keywords, undefined variables, duplicate definitions, missing resources, test name length
- ✏️ **Rename Refactoring** — Safe rename across all references
- ▶️ **Test Execution** — Run icon on every test case; run configurations with `--test`, tag filters, output directory, extra arguments, interpreter and working directory
- 🐞 **Run with DEBUG log** — Re-run with `--loglevel DEBUG` when a failure needs the detail
- 🛠️ **Generate Keyword** — Turn the keyword call under the caret into a skeleton

---

### 💰 Pricing Model

**Freemium** — `<product-descriptor code="PROBOTFRAMEWORK" optional="true"/>`.

`optional="true"` means the licence is never checked when the IDE starts and no
code path is gated on one, so **every feature is available to everyone**. A
subscription supports the plugin; it does not unlock anything.

| Tier                | Price                  | What you get                          |
|---------------------|------------------------|---------------------------------------|
| **Without licence** | $0                     | Every feature the plugin has          |
| **Subscription**    | See Marketplace        | The same, plus supporting development |

The listing used to advertise Pro-only features — a step-through debugger, a
performance profiler, a web inspector, an interactive REPL, parallel execution.
None of them existed; the code behind them was a message box and two static
labels, removed in 2026.6.0. Should an exclusive paid feature be introduced
later, note that Robot Framework has no debug protocol an IDE can attach to, so
a genuine "debugger" would mean shipping a listener-based runner, and a real
REPL would mean integrating a package such as `robotframework-interactive`.

---

### 🎯 Perfect For

- **QA engineers** writing and maintaining Robot Framework test suites
- **RPA developers** building automation workflows with Robot Framework
- **Test automation teams** needing a professional RF development environment
- **DevOps engineers** integrating Robot Framework into CI/CD pipelines

---

### ⚙️ Requirements

| Requirement        | Value                    |
|--------------------|--------------------------|
| **Minimum IDE Build** | 242 (IntelliJ 2024.2+) |
| **Java Version**   | 17+                      |
| **Runtime**        | Robot Framework installed (for test execution) |

---

### 🚀 Quick Start

1. **Install** the plugin from JetBrains Marketplace
2. **Open** a project containing `.robot` files — syntax highlighting activates automatically
3. **Start typing** to see smart code completion for keywords and variables
4. **Navigate** using Go to Declaration (Ctrl+Click) and Find References
5. **Run tests** using the built-in run configuration for Robot Framework
6. **Re-run with `--loglevel DEBUG`** (Ctrl+Shift+F9) when a failure needs the detail

---

## 📸 Screenshots & Media

1. **Syntax Highlighting** — Robot Framework test file with full semantic coloring for keywords, variables, and sections
2. **Code Completion** — Smart dropdown showing Robot Framework keywords and library completions
3. **Go to Declaration** — Navigation from keyword usage to its definition
4. **Structure View** — Hierarchical tree showing test suites, test cases, and keywords
5. **Inspections** — Inline warnings for undefined keywords, duplicate definitions, and missing resources
6. **Test execution** — Run icon in the gutter and the run configuration form
7. **Robot Framework 7.4 syntax** — GROUP, typed variables, Secret variables
8. **One-click migration** — Deprecated syntax replaced from the intention popup

---

## 🏷️ Marketplace Categories

| Type          | Category                  |
|---------------|---------------------------|
| **Primary**   | Test Automation           |
| **Secondary** | Languages & Frameworks    |

---

## 🔍 Keywords (SEO)

`Robot Framework`, `test automation`, `RPA`, `keyword-driven testing`, `acceptance testing`, `test runner`, `GROUP`, `Selenium`, `Browser Library`, `test execution`, `.robot files`, `RF 7.4`, `automation framework`, `QA tools`, `robotidy`

---

## 📋 Compatibility

| IDE                          | Compatible |
|------------------------------|------------|
| IntelliJ IDEA Community      | ✅          |
| IntelliJ IDEA Ultimate       | ✅          |
| PyCharm Community             | ✅          |
| PyCharm Professional          | ✅          |
| WebStorm                     | ✅          |
| GoLand                       | ✅          |
| PhpStorm                     | ✅          |
| CLion                        | ✅          |
| Rider                        | ✅          |
| RubyMine                     | ✅          |

**Minimum Build:** 242 (IntelliJ Platform 2024.2+)

---

## 📞 Vendor Information

| Field        | Value                        |
|--------------|------------------------------|
| **Vendor**   | Jakub Jirak                  |
| **Email**    | jirak@jakubjirak.com         |
| **Website**  | https://jakubjirak.com       |
| **Support**  | support@jakubjirak.com       |

---

## ✅ Pre-Launch Checklist

- [ ] Plugin icon (40×40 SVG and 80×80 PNG) prepared
- [ ] Screenshots (1280×800 minimum) captured for all key features
- [ ] Short description within 255-character limit
- [ ] Full description reviewed and proofread
- [x] Pricing tiers configured in the JetBrains Marketplace portal (freemium, `PROBOTFRAMEWORK`)
- [ ] Plugin signed with JetBrains Marketplace certificate
- [ ] License agreement (EULA) uploaded
- [ ] CHANGELOG.md up to date with latest version notes
- [ ] Compatibility tested on minimum build (232)
- [ ] Free vs Pro feature gating verified
- [ ] Robot Framework 7+ keyword library coverage validated
- [ ] Support email monitored and responsive
