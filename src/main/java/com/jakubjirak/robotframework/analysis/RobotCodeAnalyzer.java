/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dependency-free static analyzer for Robot Framework source files.
 *
 * <p>Produces {@link RobotFinding}s for style smells (tab indentation, trailing
 * whitespace, duplicate test names, leftover {@code TODO} markers, unused suite
 * variables) and for syntax that Robot Framework 6.0–7.4 deprecated or
 * introduced: {@code [Return]}, {@code Force Tags}, {@code Exit For Loop} and
 * friends, singular section headers, {@code WITH NAME}, {@code FOR IN ZIP}
 * without {@code mode=}, malformed {@code ${name: type}} type expressions,
 * literal values for {@code Secret} variables, {@code VAR} without a value, and
 * unbalanced {@code IF}/{@code FOR}/{@code WHILE}/{@code TRY}/{@code GROUP}
 * blocks.</p>
 *
 * <p>All offsets are computed against {@code \n}-normalized text so they line up
 * with the IntelliJ document model. The analyzer is pure and side-effect free,
 * which keeps it fully unit testable without an IDE fixture.</p>
 */
public final class RobotCodeAnalyzer {

    private static final Pattern TODO_MARKER =
            Pattern.compile("\\b(TODO|FIXME|XXX)\\b", Pattern.CASE_INSENSITIVE);

    /** Assignment cell in a keyword call, e.g. {@code ${result} =} or {@code @{items}}. */
    private static final Pattern ASSIGNMENT_CELL =
            Pattern.compile("^[$@&]\\{.*}\\s*=?$");

    private RobotCodeAnalyzer() {
    }

    /**
     * Analyzes the given source and returns all findings, ordered by offset.
     */
    public static List<RobotFinding> analyze(String source) {
        List<RobotFinding> findings = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return findings;
        }

        String text = source.replace("\r\n", "\n").replace('\r', '\n');
        List<Line> lines = toLines(text);

        analyzeLineLevel(lines, findings);
        analyzeDuplicateTestNames(lines, findings);
        analyzeUnusedVariables(text, lines, findings);
        analyzeSectionHeaders(lines, findings);
        analyzeContentOutsideSection(lines, findings);
        analyzeSettingsSection(lines, findings);
        analyzeStatements(lines, findings);
        analyzeBlocks(lines, findings);
        analyzeVariableTypes(lines, findings);
        analyzeSecretVariables(lines, findings);
        analyzeTestCaseBodies(lines, findings);

        findings.sort((a, b) -> {
            int byOffset = Integer.compare(a.getStartOffset(), b.getStartOffset());
            return byOffset != 0 ? byOffset : a.getCategory().compareTo(b.getCategory());
        });
        return findings;
    }

    // ---- Line-level checks: tabs, trailing whitespace, TODO markers -----------

    private static void analyzeLineLevel(List<Line> lines, List<RobotFinding> findings) {
        for (Line line : lines) {
            detectTab(line, findings);
            detectTrailingWhitespace(line, findings);
            detectTodo(line, findings);
        }
    }

    private static void detectTab(Line line, List<RobotFinding> findings) {
        int tab = line.text.indexOf('\t');
        if (tab < 0) {
            return;
        }
        int abs = line.start + tab;
        findings.add(new RobotFinding(
                RobotFinding.Category.TAB_INDENTATION,
                abs, abs + 1, line.number,
                "Tab character used for separation. Robot Framework treats a tab as a single "
                        + "separator; use spaces to keep cell alignment predictable."));
    }

    private static void detectTrailingWhitespace(Line line, List<RobotFinding> findings) {
        int end = line.text.length();
        int trimEnd = end;
        while (trimEnd > 0 && isSpaceOrTab(line.text.charAt(trimEnd - 1))) {
            trimEnd--;
        }
        if (trimEnd == end) {
            return;
        }
        int abs = line.start + trimEnd;
        findings.add(new RobotFinding(
                RobotFinding.Category.TRAILING_WHITESPACE,
                abs, line.start + end, line.number,
                "Trailing whitespace."));
    }

    private static void detectTodo(Line line, List<RobotFinding> findings) {
        int hash = line.text.indexOf('#');
        if (hash < 0) {
            return;
        }
        String comment = line.text.substring(hash);
        Matcher matcher = TODO_MARKER.matcher(comment);
        if (matcher.find()) {
            int abs = line.start + hash + matcher.start();
            findings.add(new RobotFinding(
                    RobotFinding.Category.TODO_COMMENT,
                    abs, abs + matcher.group().length(), line.number,
                    "Unresolved '" + matcher.group().toUpperCase(Locale.ROOT) + "' marker in comment."));
        }
    }

    // ---- Duplicate test-case names -------------------------------------------

    private static void analyzeDuplicateTestNames(List<Line> lines, List<RobotFinding> findings) {
        Section section = Section.NONE;
        Set<String> seen = new HashSet<>();

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                section = header;
                continue;
            }
            if (section != Section.TEST_CASES) {
                continue;
            }
            String name = testCaseName(line.text);
            if (name == null) {
                continue;
            }
            String normalized = normalizeName(name);
            if (!seen.add(normalized)) {
                int abs = line.start + indentWidth(line.text);
                findings.add(new RobotFinding(
                        RobotFinding.Category.DUPLICATE_TEST_NAME,
                        abs, line.start + line.text.length(), line.number,
                        "Duplicate test case name '" + name.trim() + "'. "
                                + "Robot Framework runs only one test per unique name."));
            }
        }
    }

    /** A test-case name is a non-indented, non-comment, non-header line. */
    private static String testCaseName(String text) {
        if (text.isEmpty() || isSpaceOrTab(text.charAt(0))) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }
        return trimmed;
    }

    // ---- Unused suite variables ----------------------------------------------

    private static void analyzeUnusedVariables(String text, List<Line> lines, List<RobotFinding> findings) {
        Section section = Section.NONE;
        Map<String, Line> declarations = new HashMap<>();
        List<NamedDeclaration> ordered = new ArrayList<>();

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                section = header;
                continue;
            }
            if (section != Section.VARIABLES) {
                continue;
            }
            String name = declaredVariableName(line.text);
            if (name == null) {
                continue;
            }
            String normalized = normalizeName(name);
            if (!declarations.containsKey(normalized)) {
                declarations.put(normalized, line);
                ordered.add(new NamedDeclaration(name, normalized, line));
            }
        }

        if (ordered.isEmpty()) {
            return;
        }

        Map<String, Integer> usageCount = countVariableUsages(text);

        for (NamedDeclaration decl : ordered) {
            int total = usageCount.getOrDefault(decl.normalized, 0);
            // The declaration itself counts as one occurrence; >1 means it is used.
            if (total <= 1) {
                int abs = decl.line.start;
                // Highlight the full ${name} declaration token.
                int brace = decl.line.text.indexOf('}');
                int tokenLen = brace >= 0 ? brace + 1 : decl.line.text.length();
                String token = decl.line.text.substring(0, tokenLen);
                findings.add(new RobotFinding(
                        RobotFinding.Category.UNUSED_VARIABLE,
                        abs, abs + tokenLen, decl.line.number,
                        "Variable '" + token + "' appears unused in this file."));
            }
        }
    }

    private static Map<String, Integer> countVariableUsages(String text) {
        Map<String, Integer> counts = new HashMap<>();
        for (VariableToken token : findVariableTokens(text)) {
            RobotSyntax.TypedVariable parsed = RobotSyntax.parseVariable(token.text);
            if (parsed == null) {
                continue;
            }
            counts.merge(normalizeName(baseVariableName(parsed.getName())), 1, Integer::sum);
        }
        return counts;
    }

    private static String baseVariableName(String inner) {
        int cut = inner.length();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '.' || c == '[') {
                cut = i;
                break;
            }
        }
        return inner.substring(0, cut);
    }

    /**
     * Returns the declared name (without the {@code ${}} wrapper and without an
     * RF 7.3 type annotation) or {@code null} when the line is not a declaration.
     */
    private static String declaredVariableName(String text) {
        if (text.isEmpty() || isSpaceOrTab(text.charAt(0))) {
            return null;
        }
        List<VariableToken> tokens = findVariableTokens(text);
        if (tokens.isEmpty() || tokens.get(0).start != 0) {
            return null;
        }
        RobotSyntax.TypedVariable parsed = RobotSyntax.parseVariable(tokens.get(0).text);
        return parsed == null ? null : baseVariableName(parsed.getName());
    }

    // ---- Section headers (RF 6.0 deprecated singular spellings) ---------------

    private static void analyzeSectionHeaders(List<Line> lines, List<RobotFinding> findings) {
        for (Line line : lines) {
            if (!RobotSections.isSingularHeader(line.text)) {
                continue;
            }
            String canonical = RobotSections.canonicalize(line.text);
            int start = line.start + indentWidth(line.text);
            int end = line.start + stripTrailingWidth(line.text);
            findings.add(new RobotFinding(
                    RobotFinding.Category.SINGULAR_SECTION_HEADER,
                    start, end, line.number,
                    "Singular section headers are deprecated since Robot Framework 6.0. "
                            + "Use '" + canonical + "'.",
                    canonical));
        }
    }

    // ---- Data before the first section header ---------------------------------

    private static void analyzeContentOutsideSection(List<Line> lines, List<RobotFinding> findings) {
        for (Line line : lines) {
            if (sectionOf(line.text) != null) {
                return;
            }
            String trimmed = line.text.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int start = line.start + indentWidth(line.text);
            findings.add(new RobotFinding(
                    RobotFinding.Category.CONTENT_OUTSIDE_SECTION,
                    start, line.start + stripTrailingWidth(line.text), line.number,
                    "Data before the first section header is ignored by Robot Framework. "
                            + "Move it under a section such as '*** Settings ***'."));
            // One report per file is enough; the rest is the same mistake.
            return;
        }
    }

    // ---- *** Settings *** ------------------------------------------------------

    private static void analyzeSettingsSection(List<Line> lines, List<RobotFinding> findings) {
        Section section = Section.NONE;

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                section = header;
                continue;
            }
            if (section != Section.SETTINGS) {
                continue;
            }
            List<RobotSyntax.Cell> cells = RobotSyntax.splitCells(line.text);
            if (cells.isEmpty()) {
                continue;
            }
            RobotSyntax.Cell first = cells.get(0);
            reportDeprecatedSetting(line, first, findings);
            reportHyphenTags(line, cells, findings);
            reportWithNameAlias(line, cells, findings);
        }
    }

    private static void reportDeprecatedSetting(Line line, RobotSyntax.Cell cell,
                                                List<RobotFinding> findings) {
        RobotSyntax.Deprecation deprecation = RobotSyntax.deprecatedSetting(cell.getText());
        if (deprecation == null) {
            return;
        }
        findings.add(new RobotFinding(
                RobotFinding.Category.DEPRECATED_SETTING,
                line.start + cell.getStart(), line.start + cell.getEnd(), line.number,
                deprecation.getAdvice() + " (deprecated in Robot Framework " + deprecation.getSince() + ")",
                deprecation.getReplacement()));
    }

    /**
     * Robot Framework 7.2 deprecated writing a literal {@code -tag} in
     * {@code Test Tags}; the escaped form {@code \-tag} keeps the current meaning.
     */
    private static void reportHyphenTags(Line line, List<RobotSyntax.Cell> cells,
                                         List<RobotFinding> findings) {
        String setting = RobotSyntax.normalizeName(cells.get(0).getText());
        if (!"testtags".equals(setting) && !"tasktags".equals(setting)) {
            return;
        }
        for (int i = 1; i < cells.size(); i++) {
            RobotSyntax.Cell cell = cells.get(i);
            String tag = cell.getText();
            if (!tag.startsWith("-") || tag.length() < 2) {
                continue;
            }
            findings.add(new RobotFinding(
                    RobotFinding.Category.HYPHEN_TAG_LITERAL,
                    line.start + cell.getStart(), line.start + cell.getEnd(), line.number,
                    "Robot Framework 7.2 deprecated the literal '" + tag + "' in " + cells.get(0).getText()
                            + "; a future release will read it as tag removal. Escape it as '\\" + tag + "'.",
                    "\\" + tag));
        }
    }

    /** Robot Framework 6.0 replaced the {@code WITH NAME} alias marker with {@code AS}. */
    private static void reportWithNameAlias(Line line, List<RobotSyntax.Cell> cells,
                                            List<RobotFinding> findings) {
        if (!"library".equals(RobotSyntax.normalizeName(cells.get(0).getText()))) {
            return;
        }
        for (int i = 1; i < cells.size(); i++) {
            RobotSyntax.Cell cell = cells.get(i);
            if (!"WITH NAME".equals(RobotSyntax.normalizeMarker(cell.getText()))) {
                continue;
            }
            findings.add(new RobotFinding(
                    RobotFinding.Category.WITH_NAME_ALIAS,
                    line.start + cell.getStart(), line.start + cell.getEnd(), line.number,
                    "'WITH NAME' was replaced by 'AS' in Robot Framework 6.0.",
                    "AS"));
        }
    }

    // ---- Statements inside tests, tasks and keywords ---------------------------

    private static void analyzeStatements(List<Line> lines, List<RobotFinding> findings) {
        Section section = Section.NONE;

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                section = header;
                continue;
            }
            if (section != Section.TEST_CASES && section != Section.KEYWORDS) {
                continue;
            }
            List<RobotSyntax.Cell> cells = RobotSyntax.splitCells(line.text);
            if (cells.isEmpty()) {
                continue;
            }
            RobotSyntax.Cell first = cells.get(0);
            if (first.getText().startsWith("[")) {
                reportDeprecatedSetting(line, first, findings);
                continue;
            }
            reportDeprecatedKeyword(line, cells, findings);
            reportForInZipWithoutMode(line, cells, findings);
            reportVarWithoutValue(line, cells, findings);
        }
    }

    private static void reportDeprecatedKeyword(Line line, List<RobotSyntax.Cell> cells,
                                                List<RobotFinding> findings) {
        RobotSyntax.Cell call = keywordCell(cells);
        if (call == null) {
            return;
        }
        RobotSyntax.Deprecation deprecation = RobotSyntax.deprecatedKeyword(call.getText());
        if (deprecation == null) {
            return;
        }
        findings.add(new RobotFinding(
                RobotFinding.Category.DEPRECATED_KEYWORD,
                line.start + call.getStart(), line.start + call.getEnd(), line.number,
                deprecation.getAdvice() + " (deprecated in Robot Framework " + deprecation.getSince() + ")",
                deprecation.getReplacement()));
    }

    /**
     * Returns the cell holding the keyword name, skipping assignment cells such as
     * {@code ${result} =}. Returns {@code null} for control structures.
     */
    private static RobotSyntax.Cell keywordCell(List<RobotSyntax.Cell> cells) {
        for (RobotSyntax.Cell cell : cells) {
            String text = cell.getText();
            if (ASSIGNMENT_CELL.matcher(text).matches()) {
                continue;
            }
            if (RobotSyntax.isMarker(text) || "...".equals(text)) {
                return null;
            }
            return cell;
        }
        return null;
    }

    /**
     * Robot Framework 7.0 deprecated {@code FOR ... IN ZIP} without an explicit
     * {@code mode=}, because the default changes in a future release.
     */
    private static void reportForInZipWithoutMode(Line line, List<RobotSyntax.Cell> cells,
                                                  List<RobotFinding> findings) {
        if (!"FOR".equals(RobotSyntax.normalizeMarker(cells.get(0).getText()))) {
            return;
        }
        boolean inZip = false;
        boolean hasMode = false;
        for (RobotSyntax.Cell cell : cells) {
            if ("IN ZIP".equals(RobotSyntax.normalizeMarker(cell.getText()))) {
                inZip = true;
            } else if (cell.getText().toLowerCase(Locale.ROOT).startsWith("mode=")) {
                hasMode = true;
            }
        }
        if (!inZip || hasMode) {
            return;
        }
        RobotSyntax.Cell marker = cells.get(0);
        findings.add(new RobotFinding(
                RobotFinding.Category.FOR_IN_ZIP_WITHOUT_MODE,
                line.start + marker.getStart(), line.start + marker.getEnd(), line.number,
                "FOR IN ZIP without an explicit 'mode=' is deprecated since Robot Framework 7.0. "
                        + "Add 'mode=SHORTEST', 'mode=LONGEST' or 'mode=STRICT'."));
    }

    /**
     * Robot Framework 7.4 deprecated creating a non-local variable with
     * {@code VAR} without giving it a value.
     */
    private static void reportVarWithoutValue(Line line, List<RobotSyntax.Cell> cells,
                                              List<RobotFinding> findings) {
        if (!"VAR".equals(RobotSyntax.normalizeMarker(cells.get(0).getText())) || cells.size() < 2) {
            return;
        }
        String scope = null;
        boolean hasValue = false;
        for (int i = 2; i < cells.size(); i++) {
            String text = cells.get(i).getText();
            String lower = text.toLowerCase(Locale.ROOT);
            if (lower.startsWith("scope=")) {
                scope = text.substring("scope=".length()).trim().toUpperCase(Locale.ROOT);
            } else if (lower.startsWith("separator=")) {
                continue;
            } else {
                hasValue = true;
            }
        }
        if (hasValue || scope == null || "LOCAL".equals(scope)) {
            return;
        }
        RobotSyntax.Cell name = cells.get(1);
        findings.add(new RobotFinding(
                RobotFinding.Category.VAR_WITHOUT_VALUE,
                line.start + name.getStart(), line.start + name.getEnd(), line.number,
                "Creating a " + scope + " scoped variable without a value is deprecated since "
                        + "Robot Framework 7.4. Give it an explicit value such as ${EMPTY}."));
    }

    // ---- Block balance (IF / FOR / WHILE / TRY / GROUP) -----------------------

    private static void analyzeBlocks(List<Line> lines, List<RobotFinding> findings) {
        Deque<OpenBlock> stack = new ArrayDeque<>();
        Section section = Section.NONE;

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                reportUnclosed(stack, findings);
                section = header;
                continue;
            }
            if (section != Section.TEST_CASES && section != Section.KEYWORDS) {
                continue;
            }
            String trimmed = line.text.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (!isSpaceOrTab(line.text.charAt(0))) {
                // A new test case or keyword name ends any block left open above it.
                reportUnclosed(stack, findings);
                continue;
            }
            List<RobotSyntax.Cell> cells = RobotSyntax.splitCells(line.text);
            if (cells.isEmpty()) {
                continue;
            }
            RobotSyntax.Cell first = cells.get(0);
            if ("...".equals(first.getText())) {
                continue;
            }
            String marker = RobotSyntax.normalizeMarker(first.getText());

            if (RobotSyntax.isBlockOpener(marker)) {
                // Inline IF (RF 7.0) carries the keyword call on the same row and
                // therefore needs no END.
                if ("IF".equals(marker) && cells.size() > 2) {
                    continue;
                }
                stack.push(new OpenBlock(marker, line, first));
            } else if (RobotSyntax.isBlockEnd(marker)) {
                if (stack.isEmpty()) {
                    findings.add(new RobotFinding(
                            RobotFinding.Category.ORPHAN_END,
                            line.start + first.getStart(), line.start + first.getEnd(), line.number,
                            "END without a matching IF, FOR, WHILE, TRY or GROUP."));
                } else {
                    stack.pop();
                }
            }
        }
        reportUnclosed(stack, findings);
    }

    private static void reportUnclosed(Deque<OpenBlock> stack, List<RobotFinding> findings) {
        while (!stack.isEmpty()) {
            OpenBlock open = stack.pop();
            findings.add(new RobotFinding(
                    RobotFinding.Category.UNCLOSED_BLOCK,
                    open.line.start + open.cell.getStart(),
                    open.line.start + open.cell.getEnd(),
                    open.line.number,
                    open.marker + " block is never closed with END."));
        }
    }

    // ---- Variable type conversion (RF 7.3) ------------------------------------

    private static void analyzeVariableTypes(List<Line> lines, List<RobotFinding> findings) {
        Section section = Section.NONE;

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                section = header;
                continue;
            }
            if (section == Section.COMMENTS) {
                continue;
            }
            // Types inside a trailing comment are prose, not syntax.
            int comment = RobotSyntax.commentStart(line.text);
            String data = comment < 0 ? line.text : line.text.substring(0, comment);

            for (VariableToken token : findVariableTokens(data)) {
                RobotSyntax.TypedVariable parsed = RobotSyntax.parseVariable(token.text);
                if (parsed == null || !parsed.isTyped()) {
                    continue;
                }
                String problem = RobotSyntax.validateTypeExpression(parsed.getType());
                if (problem == null) {
                    continue;
                }
                findings.add(new RobotFinding(
                        RobotFinding.Category.INVALID_VARIABLE_TYPE,
                        line.start + token.start + parsed.getTypeStart(),
                        line.start + token.start + parsed.getTypeEnd(),
                        line.number,
                        problem));
            }
        }
    }

    // ---- Secret variables (RF 7.4) --------------------------------------------

    private static void analyzeSecretVariables(List<Line> lines, List<RobotFinding> findings) {
        Section section = Section.NONE;

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                section = header;
                continue;
            }
            if (section != Section.VARIABLES) {
                continue;
            }
            List<RobotSyntax.Cell> cells = RobotSyntax.splitCells(line.text);
            if (cells.size() < 2 || cells.get(0).getStart() != 0) {
                continue;
            }
            RobotSyntax.TypedVariable declaration = RobotSyntax.parseVariable(cells.get(0).getText());
            if (declaration == null || !isSecretType(declaration.getType())) {
                continue;
            }
            for (int i = 1; i < cells.size(); i++) {
                RobotSyntax.Cell value = cells.get(i);
                if (containsVariableReference(value.getText())) {
                    continue;
                }
                findings.add(new RobotFinding(
                        RobotFinding.Category.SECRET_LITERAL_VALUE,
                        line.start + value.getStart(), line.start + value.getEnd(), line.number,
                        "A Secret variable cannot be given a literal value in the Variables section. "
                                + "Read it from another variable or from the environment, "
                                + "e.g. %{PASSWORD} or %{PASSWORD=default}."));
            }
        }
    }

    private static boolean isSecretType(String type) {
        if (type == null) {
            return false;
        }
        return "secret".equals(type.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean containsVariableReference(String cell) {
        for (int i = 0; i + 1 < cell.length(); i++) {
            if ("$@&%".indexOf(cell.charAt(i)) >= 0 && cell.charAt(i + 1) == '{') {
                return true;
            }
        }
        return false;
    }

    // ---- Test case bodies: empty tests and over-long names ---------------------

    /** Names longer than this are hard to read in reports and in the runner output. */
    public static final int MAX_TEST_NAME_LENGTH = 80;

    private static void analyzeTestCaseBodies(List<Line> lines, List<RobotFinding> findings) {
        Section section = Section.NONE;
        Line currentName = null;
        boolean hasStep = false;

        for (Line line : lines) {
            Section header = sectionOf(line.text);
            if (header != null) {
                currentName = reportTestCase(currentName, hasStep, findings);
                hasStep = false;
                section = header;
                continue;
            }
            if (section != Section.TEST_CASES) {
                continue;
            }
            String trimmed = line.text.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (!isSpaceOrTab(line.text.charAt(0))) {
                currentName = reportTestCase(currentName, hasStep, findings);
                hasStep = false;
                currentName = line;
                reportLongName(line, findings);
                continue;
            }
            if (currentName == null) {
                continue;
            }
            List<RobotSyntax.Cell> cells = RobotSyntax.splitCells(line.text);
            if (cells.isEmpty()) {
                continue;
            }
            String first = cells.get(0).getText();
            // Settings and continuation rows are not executable steps.
            if (!first.startsWith("[") && !"...".equals(first)) {
                hasStep = true;
            }
        }
        reportTestCase(currentName, hasStep, findings);
    }

    private static Line reportTestCase(Line name, boolean hasStep, List<RobotFinding> findings) {
        if (name != null && !hasStep) {
            findings.add(new RobotFinding(
                    RobotFinding.Category.EMPTY_TEST_CASE,
                    name.start, name.start + stripTrailingWidth(name.text), name.number,
                    "Test case '" + name.text.trim() + "' has no steps. Robot Framework fails a "
                            + "test that contains no keywords."));
        }
        return null;
    }

    private static void reportLongName(Line line, List<RobotFinding> findings) {
        String name = line.text.trim();
        if (name.length() <= MAX_TEST_NAME_LENGTH) {
            return;
        }
        findings.add(new RobotFinding(
                RobotFinding.Category.LONG_TEST_NAME,
                line.start, line.start + stripTrailingWidth(line.text), line.number,
                "Test case name is " + name.length() + " characters long; keep it under "
                        + MAX_TEST_NAME_LENGTH + " so reports stay readable."));
    }

    // ---- Shared helpers -------------------------------------------------------

    /** A variable token together with its absolute offset in the analyzed text. */
    private static final class VariableToken {
        final String text;
        final int start;

        VariableToken(String text, int start) {
            this.text = text;
            this.start = start;
        }
    }

    /**
     * Finds every {@code ${...}}-style token in the text using brace matching, so
     * that nested variables and type annotations stay in one piece.
     */
    private static List<VariableToken> findVariableTokens(String text) {
        List<VariableToken> tokens = new ArrayList<>();
        for (int i = 0; i + 1 < text.length(); i++) {
            if ("$@&%".indexOf(text.charAt(i)) < 0 || text.charAt(i + 1) != '{') {
                continue;
            }
            int depth = 0;
            int end = -1;
            for (int j = i + 1; j < text.length(); j++) {
                char c = text.charAt(j);
                if (c == '\n') {
                    break;
                }
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        end = j;
                        break;
                    }
                }
            }
            if (end < 0) {
                continue;
            }
            tokens.add(new VariableToken(text.substring(i, end + 1), i));
            i = end;
        }
        return tokens;
    }

    private static List<Line> toLines(String text) {
        List<Line> lines = new ArrayList<>();
        int start = 0;
        int number = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines.add(new Line(text.substring(start, i), start, number));
                start = i + 1;
                number++;
            }
        }
        if (start <= text.length()) {
            lines.add(new Line(text.substring(start), start, number));
        }
        return lines;
    }

    private static Section sectionOf(String text) {
        if (!RobotSections.isHeader(text)) {
            return null;
        }
        String name = RobotSections.rawHeaderName(text);
        if (name == null) {
            return Section.OTHER;
        }
        String canonical = RobotSections.canonicalName(name);
        if (canonical == null) {
            return Section.OTHER;
        }
        switch (canonical) {
            case "Test Cases":
            case "Tasks":
                return Section.TEST_CASES;
            case "Variables":
                return Section.VARIABLES;
            case "Settings":
                return Section.SETTINGS;
            case "Keywords":
                return Section.KEYWORDS;
            case "Comments":
                return Section.COMMENTS;
            default:
                return Section.OTHER;
        }
    }

    private static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
    }

    private static int indentWidth(String text) {
        int i = 0;
        while (i < text.length() && isSpaceOrTab(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int stripTrailingWidth(String text) {
        int end = text.length();
        while (end > 0 && isSpaceOrTab(text.charAt(end - 1))) {
            end--;
        }
        return end;
    }

    private static boolean isSpaceOrTab(char c) {
        return c == ' ' || c == '\t';
    }

    private enum Section {
        NONE, SETTINGS, TEST_CASES, VARIABLES, KEYWORDS, COMMENTS, OTHER
    }

    private static final class Line {
        final String text;
        final int start;
        final int number;

        Line(String text, int start, int number) {
            this.text = text;
            this.start = start;
            this.number = number;
        }
    }

    private static final class OpenBlock {
        final String marker;
        final Line line;
        final RobotSyntax.Cell cell;

        OpenBlock(String marker, Line line, RobotSyntax.Cell cell) {
            this.marker = marker;
            this.line = line;
            this.cell = cell;
        }
    }

    private static final class NamedDeclaration {
        final String original;
        final String normalized;
        final Line line;

        NamedDeclaration(String original, String normalized, Line line) {
            this.original = original;
            this.normalized = normalized;
            this.line = line;
        }
    }
}
