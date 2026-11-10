/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic, dependency-free formatter for Robot Framework source files.
 *
 * <p>The formatter normalizes a {@code .robot}/{@code .resource} file the way
 * {@code robotidy} would for the most common style issues:</p>
 * <ul>
 *   <li>Expands tab characters to spaces (Robot Framework treats tabs as a
 *       single separator, which is a frequent source of alignment bugs).</li>
 *   <li>Canonicalizes section headers, e.g. {@code *** test cases ***} →
 *       {@code *** Test Cases ***}.</li>
 *   <li>Strips trailing whitespace from every line.</li>
 *   <li>Collapses runs of blank lines and guarantees a single blank line before
 *       each section header.</li>
 *   <li>Normalizes line endings to {@code \n} and ensures a single trailing
 *       newline.</li>
 * </ul>
 *
 * <p>Formatting is idempotent: {@code format(format(x)).equals(format(x))}.</p>
 */
public final class RobotFormatter {

    private final RobotFormatterOptions options;

    public RobotFormatter() {
        this(RobotFormatterOptions.defaults());
    }

    public RobotFormatter(RobotFormatterOptions options) {
        this.options = options;
    }

    /**
     * Formats the given source text. Returns the input unchanged when it is
     * {@code null} or empty.
     */
    public String format(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        boolean hadTrailingNewline = endsWithLineBreak(source);
        List<String> lines = splitLines(normalizeLineEndings(source));

        List<String> processed = new ArrayList<>(lines.size());
        for (String line : lines) {
            processed.add(processLine(line));
        }

        List<String> collapsed = normalizeBlankLines(processed);

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < collapsed.size(); i++) {
            if (i > 0) {
                out.append('\n');
            }
            out.append(collapsed.get(i));
        }

        if (options.isEnsureTrailingNewline()) {
            if (out.length() > 0) {
                out.append('\n');
            }
        } else if (hadTrailingNewline && out.length() > 0) {
            out.append('\n');
        }

        return out.toString();
    }

    private String processLine(String line) {
        String result = expandTabs(line);

        if (options.isNormalizeSectionHeaders() && RobotSections.isHeader(result)) {
            String canonical = RobotSections.canonicalize(result);
            if (canonical != null) {
                result = canonical;
            }
        }

        if (options.isTrimTrailingWhitespace()) {
            result = stripTrailing(result);
        }
        return result;
    }

    private String expandTabs(String line) {
        if (line.indexOf('\t') < 0) {
            return line;
        }
        String replacement = " ".repeat(options.getIndentSize());
        return line.replace("\t", replacement);
    }

    private List<String> normalizeBlankLines(List<String> lines) {
        List<String> result = new ArrayList<>(lines.size());
        int blankRun = 0;

        for (String line : lines) {
            boolean blank = line.isBlank();

            if (blank) {
                blankRun++;
                // Suppress blank lines before the first real content.
                if (!result.isEmpty() && blankRun <= options.getMaxConsecutiveBlankLines()) {
                    result.add("");
                }
                continue;
            }

            if (options.isSeparateSections()
                    && RobotSections.isHeader(line)
                    && !result.isEmpty()
                    && !lastIsBlank(result)) {
                result.add("");
            }

            result.add(line);
            blankRun = 0;
        }

        // Drop blank lines accumulated at the very end; the trailing newline is
        // handled separately by format().
        while (!result.isEmpty() && result.get(result.size() - 1).isEmpty()) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    private static boolean lastIsBlank(List<String> lines) {
        return lines.get(lines.size() - 1).isEmpty();
    }

    private static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static boolean endsWithLineBreak(String text) {
        char last = text.charAt(text.length() - 1);
        return last == '\n' || last == '\r';
    }

    private static List<String> splitLines(String text) {
        List<String> lines = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines.add(text.substring(start, i));
                start = i + 1;
            }
        }
        // Keep the final segment even when it is empty only if there was no
        // trailing newline; trailing newline produces no extra empty entry.
        if (start < text.length()) {
            lines.add(text.substring(start));
        }
        return lines;
    }

    private static String stripTrailing(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }
}
