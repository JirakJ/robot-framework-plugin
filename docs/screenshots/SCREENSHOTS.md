# Screenshots & Media Guide

Marketplace listing assets for the **Robot Framework Professional** plugin.

All shots are captured against the ready-made sample project in
[`sample-project/`](sample-project), which is crafted so every new feature lights
up without any extra setup.

---

## Capture environment

- **IDE:** IntelliJ IDEA (Community is fine) 2024.3+
- **Theme:** IntelliJ Light, Light editor color scheme
- **Window size:** 1600×1000 logical pixels
- **Output:** `docs/screenshots/NN-name.png` (PNG, ≥ 1280×800 — Marketplace minimum)
- **Zoom:** default font size; hide the navigation bar and minimap for a clean frame

Open `sample-project/` as a project (or copy the two `.robot` files into any
project that has this plugin installed) before capturing.

---

## Screenshots

### 1. `01-code-inspections.png` — Code-quality inspections
**File:** `quality_demo.robot`

**What to capture:** the editor showing all four new inspections firing at once:
- **Duplicate test case name** — red error under the second `User Can Log In` (line 17).
- **Tab character used for separation** — warning on the tab-indented lines (22–23).
- **Trailing whitespace** — weak warning at the end of line 19.
- **Unresolved TODO/FIXME marker** — weak warnings on the `# TODO` (line 13) and `# FIXME` (line 23) comments.

Hover one finding so its tooltip/description is visible, and make sure the
right-hand error stripe shows the mixed severities.

### 2. `02-quick-fixes.png` — One-click quick fixes
**File:** `quality_demo.robot`

**What to capture:** the cursor on a tab-indented line with the intention popup
(`Alt+Enter`) open, showing **Convert tabs to spaces**. Capturing the
**Remove trailing whitespace** quick fix on line 19 also works.

### 3. `03-formatter-before-after.png` — Robot file formatter
**File:** `messy_suite.robot`

**What to capture:** a before/after pair (split or two stacked frames). On the
left, the messy file; on the right, the result after **Robot Framework → Format
Robot File** (`Ctrl+Alt+Shift+R`). Highlight that lower-case headers became
`*** Settings ***` / `*** Test Cases ***` / `*** Keywords ***`, tabs became
spaces, blank-line runs collapsed, and a blank line was inserted before each
section header. See [`messy_suite.formatted.robot`](sample-project/messy_suite.formatted.robot)
for the exact expected output.

### 4. `04-settings.png` — Formatter settings
**Where:** Settings → Tools → Robot Framework

**What to capture:** the settings page with the new **Formatter** section visible
— *Indent size (spaces)* and *Normalize section headers* — alongside the existing
interpreter/execution options.

### 5. `05-inspection-settings.png` — Inspection profile (optional)
**Where:** Settings → Editor → Inspections → Robot Framework

**What to capture:** the Robot Framework inspection group listing the new
inspections (Duplicate test case name, Tab character, Trailing whitespace,
TODO/FIXME marker, Unused suite variable) so users can see they are configurable.

---

## Asset checklist

- [x] `01-code-inspections.png`
- [x] `02-quick-fixes.png`
- [x] `03-formatter-before-after.png`
- [x] `04-settings.png`
- [ ] `05-inspection-settings.png` (optional)

> The committed PNGs are IntelliJ-Light-themed renders (2880×1800) of the
> features against the sample project. Re-capture them as live IDE screenshots
> any time for an exact pixel match; the framing and content already match the
> shipped behaviour.

Upload the PNGs to the JetBrains Marketplace listing (Edit Plugin → Media).
Marketplace media must be uploaded manually — the publish token only ships the
plugin ZIP.
