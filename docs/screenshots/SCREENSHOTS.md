# Screenshots & Media Guide

Marketplace listing assets for the **Robot Framework** plugin.

All shots are built from the sample suites in [`sample-project/`](sample-project),
which are crafted so the advertised features light up without any extra setup.

---

## How the shots are produced

```bash
python3 docs/screenshots/src/render.py
```

[`src/render.py`](src/render.py) renders each shot as HTML and captures it with
headless Chrome at 1440×900 @2× → **2880×1800 PNG**. Two things keep the pictures
honest instead of aspirational:

- The token colours in the renderer are the `TextAttributesKey` fallbacks declared
  in `editor/RobotSyntaxHighlighter`, resolved against the **IntelliJ Light**
  scheme — so section headers are the olive `METADATA` colour, control structures
  and BuiltIn keywords are `KEYWORD` blue, variables are `INSTANCE_FIELD` purple,
  and suite settings such as `Library` are plain text, exactly as the plugin
  renders them today.
- The inspection titles and messages are copied verbatim from
  `analysis/RobotCodeAnalyzer`.

`SampleProjectTest` asserts that the sample suites keep producing exactly the
findings the screenshots show, so a behaviour change breaks the build rather than
silently making the listing wrong.

**Known gaps visible in the shots** (candidates for a follow-up release):
suite settings in `*** Settings ***` are not highlighted, and a variable's type
annotation renders in the variable colour because `ROBOT_VARIABLE_TYPE` falls back
to `CLASS_REFERENCE`, which the IntelliJ Light scheme leaves unstyled.

Re-capture any shot as a live IDE screenshot (`./gradlew runIde`) whenever an
exact pixel match matters; framing and content already follow the shipped
behaviour.

---

## Screenshots

### 1. `01-code-inspections.png` — Robot Framework 7.4 inspections
**File:** `rf74_migration.robot`

Six findings at once: `WITH NAME` (RF 6.0) and `Force Tags` (RF 6.0) as
deprecations, a literal value for a `Secret` variable (RF 7.4) and a malformed
`list[int` type (RF 7.3) as errors, a `VAR` created in SUITE scope without a
value (RF 7.4), and a `GROUP` (RF 7.2) that is never closed with `END`. The
tooltip shows the Secret message.

### 2. `02-quick-fixes.png` — One-click migration
**File:** `rf74_migration.robot`

The intention popup on `[Return]` offering **Replace with 'RETURN'**. The same
mechanism backs `Force Tags` → `Test Tags`, `WITH NAME` → `AS`, singular section
headers, and the deprecated loop keywords.

### 3. `03-formatter-before-after.png` — Robot file formatter
**File:** `messy_suite.robot`

Before/after pair for **Robot Framework → Format Robot File**
(`Ctrl+Alt+Shift+R`): canonical section headers, tabs → spaces, collapsed
blank-line runs. See [`messy_suite.formatted.robot`](sample-project/messy_suite.formatted.robot)
for the exact expected output.

### 4. `04-settings.png` — Formatter settings
**Where:** Settings → Tools → Robot Framework

Indent size and header normalization alongside the interpreter options.

### 5. `05-rf74-syntax.png` — Robot Framework 7.4 syntax
**File:** `rf74_showcase.robot`

A suite using `GROUP` (7.2), typed variables (7.3), a `Secret` variable (7.4),
`VAR` with the `SUITES` scope (7.1), `WHILE` limits, an inline `IF`, and
`[Setup]` on a user keyword (7.0).

### 6. `06-type-completion.png` — Conversion type completion
**File:** `rf74_showcase.robot`

Typing a colon inside a variable offers the conversion types Robot Framework 7.3
understands, including the 7.4 `Secret` type.

---

## Asset checklist

- [x] `01-code-inspections.png`
- [x] `02-quick-fixes.png`
- [x] `03-formatter-before-after.png`
- [x] `04-settings.png`
- [x] `05-rf74-syntax.png`
- [x] `06-type-completion.png`

Upload the PNGs to the JetBrains Marketplace listing (Edit Plugin → Media).
Marketplace media must be uploaded manually — the publish token only ships the
plugin ZIP.
