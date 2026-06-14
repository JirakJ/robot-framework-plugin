/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import java.util.ArrayList;
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
 * <p>Produces {@link RobotFinding}s for common style and correctness smells:
 * tab indentation, trailing whitespace, duplicate test-case names, leftover
 * {@code TODO}/{@code FIXME} markers, and suite variables that are never
 * referenced. All offsets are computed against {@code \n}-normalized text so they
 * line up with the IntelliJ document model.</p>
 *
 * <p>The analyzer is pure and side-effect free, which keeps it fully unit
 * testable without an IDE fixture.</p>
 */
public final class RobotCodeAnalyzer {

    private static final Pattern TODO_MARKER =
            Pattern.compile("\\b(TODO|FIXME|XXX)\\b", Pattern.CASE_INSENSITIVE);

    /** Matches a variable reference or declaration body: ${name}, @{name}, &{name}. */
    private static final Pattern VARIABLE_REF =
            Pattern.compile("[$@&]\\{\\s*([^{}]+?)\\s*}");

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
        Matcher matcher = VARIABLE_REF.matcher(text);
        while (matcher.find()) {
            String inner = matcher.group(1);
            // Item/attribute access such as ${user}[name] or ${obj.attr}: use the base.
            String base = baseVariableName(inner);
            counts.merge(normalizeName(base), 1, Integer::sum);
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

    /** Returns the declared name (without the {@code ${}} wrapper) or {@code null}. */
    private static String declaredVariableName(String text) {
        if (text.isEmpty() || isSpaceOrTab(text.charAt(0))) {
            return null;
        }
        Matcher matcher = VARIABLE_REF.matcher(text.trim());
        if (matcher.lookingAt()) {
            return baseVariableName(matcher.group(1));
        }
        return null;
    }

    // ---- Shared helpers -------------------------------------------------------

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
        switch (name.toLowerCase(Locale.ROOT)) {
            case "test cases":
            case "test case":
            case "tasks":
            case "task":
                return Section.TEST_CASES;
            case "variables":
            case "variable":
                return Section.VARIABLES;
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

    private static boolean isSpaceOrTab(char c) {
        return c == ' ' || c == '\t';
    }

    private enum Section {
        NONE, TEST_CASES, VARIABLES, OTHER
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
