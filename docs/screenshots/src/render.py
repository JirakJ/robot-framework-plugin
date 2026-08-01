#!/usr/bin/env python3
"""Renders the Marketplace screenshots for the Robot Framework plugin.

The shots are HTML renders of the plugin's own output, driven by headless
Chrome. The token colours below mirror the fallbacks declared in
`editor/RobotSyntaxHighlighter` resolved against the IntelliJ Light scheme, and
the inspection texts are copied verbatim from `analysis/RobotCodeAnalyzer`, so
what the pictures show is what the plugin actually does.

Usage:  python3 docs/screenshots/src/render.py
Output: docs/screenshots/NN-name.png (2880x1800)
"""

import html
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SAMPLES = ROOT / "docs" / "screenshots" / "sample-project"
OUT = ROOT / "docs" / "screenshots"

CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

WIDTH, HEIGHT, SCALE = 1440, 900, 2

# --- IntelliJ Light resolutions of the plugin's TextAttributesKey fallbacks ---
COLORS = {
    "section": "#9E880D",   # METADATA
    "setting": "#9E880D",   # METADATA
    "keyword": "#0033B3",   # KEYWORD (bold)
    "control": "#0033B3",   # KEYWORD (bold)
    "variable": "#871094",  # INSTANCE_FIELD
    "string": "#067D17",    # STRING
    "tag": "#0033B3",       # MARKUP_TAG
    "comment": "#8C8C8C",   # LINE_COMMENT (italic)
    "text": "#080808",      # IDENTIFIER
}

MARKERS = {
    "IF", "ELSE IF", "ELSE", "FOR", "WHILE", "TRY", "EXCEPT", "FINALLY",
    "GROUP", "END", "BREAK", "CONTINUE", "RETURN", "VAR",
    "IN", "IN RANGE", "IN ENUMERATE", "IN ZIP",
}
BUILTINS = {"log", "shouldbeequal", "shouldcontain", "run", "setvariable", "sleep"}
SECTION_RE = re.compile(r"^\s*\*+.*$")
VARIABLE_RE = re.compile(r"[$@&%]\{")


def tokenize(line: str, in_tags: bool):
    """Yields (text, css_class) pairs mirroring RobotLexer's classification."""
    if SECTION_RE.match(line) and line.lstrip().startswith("*"):
        yield line, "section"
        return
    i, n = 0, len(line)
    while i < n:
        ch = line[i]
        if ch.isspace():
            j = i
            while j < n and line[j].isspace():
                j += 1
            yield line[i:j], None
            i = j
            continue
        if ch == "#":
            yield line[i:], "comment"
            return
        if VARIABLE_RE.match(line, i):
            depth, j = 0, i + 1
            while j < n:
                if line[j] == "{":
                    depth += 1
                elif line[j] == "}":
                    depth -= 1
                    if depth == 0:
                        j += 1
                        break
                j += 1
            yield line[i:j], "variable"
            i = j
            continue
        j = i
        while j < n and not line[j].isspace():
            j += 1
        word = line[i:j]
        # Multi-word markers are one cell (single space between the words).
        if j < n and line[j] == " " and j + 1 < n and line[j + 1] not in " \t":
            k = j + 1
            while k < n and not line[k].isspace():
                k += 1
            combined = f"{word} {line[j + 1:k]}"
            if combined.upper() in MARKERS or combined.upper() == "WITH NAME":
                word, j = combined, k
        upper = word.upper()
        if word.startswith("[") and word.endswith("]"):
            in_tags = word.lower() == "[tags]"
            yield word, "setting"
        elif in_tags:
            yield word, "tag"
        elif upper in MARKERS:
            in_tags = False
            yield word, "control"
        elif word.lower().replace(" ", "") in BUILTINS:
            in_tags = False
            yield word, "keyword"
        elif word.startswith('"') or word.startswith("'"):
            yield word, "string"
        else:
            yield word, "text"
        i = j
    return


def render_code(source: str, marks: dict) -> str:
    """marks: {line_number: 'error'|'warning'|'weak'} for the squiggle underlay."""
    rows = []
    in_tags = False
    for number, line in enumerate(source.rstrip("\n").split("\n"), start=1):
        spans = []
        if line.strip().startswith("[Tags]"):
            in_tags = True
        elif line.strip() and not line.startswith(" "):
            in_tags = False
        for text, css in tokenize(line, in_tags):
            escaped = html.escape(text).replace(" ", "&nbsp;")
            spans.append(f'<span class="{css}">{escaped}</span>' if css else escaped)
        mark = marks.get(number)
        code = "".join(spans) or "&nbsp;"
        if mark:
            code = f'<span class="mark {mark}">{code}</span>'
        rows.append(
            f'<div class="row"><span class="ln">{number}</span>'
            f'<span class="code">{code}</span></div>'
        )
    return "\n".join(rows)


STYLE = """
* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  width: %dpx; height: %dpx; background: #F2F2F2;
  font-family: -apple-system, "SF Pro Text", "Helvetica Neue", sans-serif;
  -webkit-font-smoothing: antialiased;
}
.window { width: 100%%; height: 100%%; background: #FFFFFF; display: flex; flex-direction: column; }
.titlebar {
  height: 38px; background: #ECECEC; border-bottom: 1px solid #D5D5D5;
  display: flex; align-items: center; padding: 0 14px; gap: 8px; flex: none;
}
.dot { width: 12px; height: 12px; border-radius: 50%%; }
.dot.r { background: #FF5F57; } .dot.y { background: #FEBC2E; } .dot.g { background: #28C840; }
.title { flex: 1; text-align: center; font-size: 13px; color: #3C3C3C; margin-right: 56px; }
.tabbar {
  height: 34px; background: #F7F8FA; border-bottom: 1px solid #E0E0E0;
  display: flex; align-items: stretch; flex: none;
}
.tab {
  display: flex; align-items: center; gap: 7px; padding: 0 16px; font-size: 12.5px;
  color: #2B2B2B; background: #FFFFFF; border-bottom: 2px solid #3574F0;
}
.tab .fileicon {
  width: 11px; height: 11px; border-radius: 50%%; border: 2px solid #59A869; display: inline-block;
}
.editor { flex: 1; position: relative; overflow: hidden; padding: 10px 0; }
.row { display: flex; align-items: baseline; height: 21px; line-height: 21px; }
.ln {
  width: 54px; flex: none; text-align: right; padding-right: 14px;
  font-family: "JetBrains Mono", "SF Mono", Menlo, monospace; font-size: 12.5px; color: #ADADAD;
}
.code {
  font-family: "JetBrains Mono", "SF Mono", Menlo, monospace; font-size: 13px;
  color: #080808; white-space: pre;
}
.section { color: %s; font-weight: 700; }
.setting { color: %s; }
.keyword, .control { color: %s; font-weight: 700; }
.variable { color: %s; }
.string { color: %s; }
.tag { color: %s; }
.comment { color: %s; font-style: italic; }
.text { color: %s; }
.mark { padding-bottom: 2px; }
.mark.error { border-bottom: 2px dotted #E55765; }
.mark.warning { background: #FFF6D9; border-bottom: 2px dotted #E3B341; }
.mark.weak { border-bottom: 2px dotted #A0A0A0; }
.stripe { position: absolute; top: 0; right: 0; width: 12px; height: 100%%; }
.stripe i { position: absolute; right: 2px; width: 8px; height: 4px; display: block; }
.popup {
  position: absolute; background: #FFFFFF; border: 1px solid #C9CCD1; border-radius: 8px;
  box-shadow: 0 6px 22px rgba(0,0,0,0.17); padding: 14px 16px; font-size: 12.5px;
  color: #1F1F1F; line-height: 1.45;
}
.popup h4 { font-size: 13px; margin-bottom: 7px; display: flex; align-items: center; gap: 7px; }
.badge {
  display: inline-flex; align-items: center; justify-content: center; width: 16px; height: 16px;
  border-radius: 3px; color: #FFF; font-size: 11px; font-weight: 700;
}
.badge.error { background: #E55765; } .badge.warn { background: #E3B341; }
.popup .hint { margin-top: 10px; padding-top: 9px; border-top: 1px solid #EBECF0; color: #6E6E6E; }
.popup .hint b { color: #3574F0; font-weight: 500; }
.menu { position: absolute; background: #FFFFFF; border: 1px solid #C9CCD1; border-radius: 8px;
        box-shadow: 0 6px 22px rgba(0,0,0,0.17); padding: 5px 0; font-size: 12.5px; min-width: 330px; }
.menu .item { padding: 6px 15px; display: flex; align-items: center; gap: 9px; color: #1F1F1F; }
.menu .item.sel { background: #D4E2FF; }
.menu .bulb { color: #E3B341; font-size: 13px; }
.menu .kind { margin-left: auto; color: #8C8C8C; font-size: 11.5px;
              font-family: "JetBrains Mono", "SF Mono", Menlo, monospace; }
.caption {
  position: absolute; left: 0; bottom: 0; width: 100%%; padding: 11px 22px;
  background: #F7F8FA; border-top: 1px solid #E0E0E0; font-size: 13px; color: #4A4A4A;
}
.caption b { color: #1F1F1F; }
.caption code, .popup code {
  font-family: "JetBrains Mono", "SF Mono", Menlo, monospace; font-size: 12px; color: #1F1F1F;
}
""" % (WIDTH, HEIGHT, COLORS["section"], COLORS["setting"], COLORS["keyword"],
       COLORS["variable"], COLORS["string"], COLORS["tag"], COLORS["comment"], COLORS["text"])


def page(title: str, code_html: str, extra: str, caption: str) -> str:
    return f"""<!doctype html><meta charset="utf-8"><style>{STYLE}</style>
<div class="window">
  <div class="titlebar"><span class="dot r"></span><span class="dot y"></span>
    <span class="dot g"></span><span class="title">{html.escape(title)}</span></div>
  <div class="tabbar"><div class="tab"><span class="fileicon"></span>{html.escape(title.split(' ')[0])}</div></div>
  <div class="editor">
    {code_html}
    {extra}
    <div class="caption">{caption}</div>
  </div>
</div>"""


EDITOR_PAD, ROW_HEIGHT = 10, 21


def row_top(line: int) -> int:
    """Y offset of a code row inside .editor, so overlays line up with the text."""
    return EDITOR_PAD + (line - 1) * ROW_HEIGHT


def stripe(items) -> str:
    marks = "".join(f'<i style="top:{top}px;background:{color}"></i>' for top, color in items)
    return f'<div class="stripe">{marks}</div>'


def shoot(name: str, document: str) -> None:
    with tempfile.TemporaryDirectory() as tmp:
        page_file = Path(tmp) / "page.html"
        page_file.write_text(document, encoding="utf-8")
        target = OUT / name
        subprocess.run(
            # No --user-data-dir: passing one makes headless Chrome hang on macOS.
            [CHROME, "--headless", "--disable-gpu", "--hide-scrollbars",
             f"--screenshot={target}",
             f"--window-size={WIDTH},{HEIGHT}",
             f"--force-device-scale-factor={SCALE}",
             page_file.as_uri()],
            check=True, capture_output=True, timeout=90,
        )
        print(f"  {name}  ({target.stat().st_size // 1024} KB)", flush=True)


def main() -> int:
    if not Path(CHROME).exists():
        print(f"Google Chrome not found at {CHROME}", file=sys.stderr)
        return 1
    if not shutil.which("python3"):
        return 1

    migration = (SAMPLES / "rf74_migration.robot").read_text(encoding="utf-8")
    showcase = (SAMPLES / "rf74_showcase.robot").read_text(encoding="utf-8")

    print("Rendering Marketplace screenshots:")

    # 1 — inspections firing on the legacy suite
    # Line numbers follow rf74_migration.robot; SampleProjectTest guards the findings.
    marks = {4: "warning", 5: "warning", 9: "error", 10: "error", 15: "warning", 16: "error"}
    tooltip = """
    <div class="popup" style="left:430px; top:352px; width:640px;">
      <h4><span class="badge error">!</span>Literal value for a Secret variable</h4>
      A Secret variable cannot be given a literal value in the Variables section.
      Read it from another variable or from the environment, e.g. %{PASSWORD} or %{PASSWORD=default}.
      <div class="hint"><b>Alt+Enter</b> &nbsp;Show quick fixes</div>
    </div>"""
    shoot("01-code-inspections.png", page(
        "rf74_migration.robot — Robot Framework Professional",
        render_code(migration, marks),
        stripe([(row_top(4), "#E3B341"), (row_top(5), "#E3B341"), (row_top(9), "#E55765"),
                (row_top(10), "#E55765"), (row_top(15), "#E3B341"), (row_top(16), "#E55765")]) + tooltip,
        "<b>Robot Framework 7.4 inspections</b> — deprecated settings, an unclosed GROUP, "
        "a malformed variable type and a literal Secret, all caught as you type."))

    # 2 — quick fix for a deprecated setting
    menu = """
    <div class="menu" style="left:190px; top:474px;">
      <div class="item sel"><span class="bulb">&#128161;</span>Replace with 'RETURN'<span class="kind">quick fix</span></div>
      <div class="item"><span class="bulb">&#9881;</span>Suppress for statement<span class="kind">&nbsp;</span></div>
      <div class="item"><span class="bulb">&#9881;</span>Edit inspection profile setting<span class="kind">&nbsp;</span></div>
    </div>"""
    shoot("02-quick-fixes.png", page(
        "rf74_migration.robot — Robot Framework Professional",
        render_code(migration, {22: "warning"}),
        stripe([(row_top(22), "#E3B341")]) + menu,
        "<b>One-click migration</b> — <code>[Return]</code> becomes the RETURN statement "
        "introduced in Robot Framework 7.0. The same fix exists for Force Tags, WITH NAME "
        "and the deprecated loop keywords."))

    # 3 — the modern syntax the plugin now understands
    shoot("05-rf74-syntax.png", page(
        "rf74_showcase.robot — Robot Framework Professional",
        render_code(showcase, {}),
        '<div class="popup" style="left:820px; top:250px; width:520px;">'
        '<h4>Understood out of the box</h4>'
        'GROUP blocks (7.2) &middot; typed variables (7.3) &middot; Secret variables (7.4)<br>'
        'VAR with the SUITES scope (7.1) &middot; WHILE limits &middot; inline IF &middot; '
        '[Setup] on user keywords (7.0)</div>',
        "<b>Robot Framework 7.4 syntax</b> — folding, structure view, navigation and "
        "completion all work on the modern constructs."))

    # 4 — conversion type completion
    completion = f"""
    <div class="menu" style="left:148px; top:{row_top(2) + 23}px; min-width:300px;">
      <div class="item sel">int<span class="kind">Conversion type</span></div>
      <div class="item">float<span class="kind">Conversion type</span></div>
      <div class="item">bool<span class="kind">Conversion type</span></div>
      <div class="item">list[int]<span class="kind">Conversion type</span></div>
      <div class="item">datetime<span class="kind">Conversion type</span></div>
      <div class="item">Secret<span class="kind">Conversion type</span></div>
    </div>"""
    typing = (
        "*** Variables ***\n"
        "${RETRIES: }           3\n"
        "${PASSWORD: Secret}    %{APP_PASSWORD}\n"
        "@{BROWSERS: str}       chrome    firefox\n"
    )
    shoot("06-type-completion.png", page(
        "rf74_showcase.robot — Robot Framework Professional",
        render_code(typing, {}),
        completion,
        "<b>Type completion</b> — typing a colon inside a variable offers the conversion "
        "types Robot Framework 7.3 understands, including the 7.4 Secret type."))

    print("Done. Upload these under Edit Plugin -> Media on the Marketplace.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
