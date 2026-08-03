/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Produces the text and the insertion point for a new user keyword.
 *
 * <p>Pure so the generated skeleton can be asserted in unit tests; the action
 * only applies the returned edit to the document.</p>
 */
public final class RobotKeywordGenerator {

    /** Where to insert and what to insert. */
    public static final class Edit {
        private final int offset;
        private final String text;
        private final int caretOffsetInText;

        Edit(int offset, String text, int caretOffsetInText) {
            this.offset = offset;
            this.text = text;
            this.caretOffsetInText = caretOffsetInText;
        }

        /** Document offset the text should be inserted at. */
        public int getOffset() {
            return offset;
        }

        /** Text to insert. */
        public String getText() {
            return text;
        }

        /** Absolute offset the caret should end up at after the insertion. */
        public int getCaretOffset() {
            return offset + caretOffsetInText;
        }
    }

    private static final String INDENT = "    ";

    private RobotKeywordGenerator() {
    }

    /**
     * Builds the edit that adds {@code keywordName} to the file, creating the
     * {@code *** Keywords ***} section when it is missing. Returns {@code null}
     * when the name is blank or the keyword already exists.
     */
    @Nullable
    public static Edit generate(String source, String keywordName) {
        if (keywordName == null || keywordName.trim().isEmpty()) {
            return null;
        }
        String name = keywordName.trim();
        String text = source == null ? "" : source.replace("\r\n", "\n").replace('\r', '\n');

        for (RobotStructure.Entry existing : RobotStructure.keywords(text)) {
            if (RobotSyntax.normalizeName(existing.getName()).equals(RobotSyntax.normalizeName(name))) {
                return null;
            }
        }

        String body = name + "\n"
                + INDENT + "[Documentation]    Describe what this keyword does.\n"
                + INDENT + "No Operation\n";
        int caretInBody = body.indexOf("No Operation");

        int sectionEnd = keywordsSectionEnd(text);
        if (sectionEnd >= 0) {
            String prefix = needsBlankLine(text, sectionEnd) ? "\n" : "";
            return new Edit(sectionEnd, prefix + body, prefix.length() + caretInBody);
        }

        StringBuilder appended = new StringBuilder();
        if (!text.isEmpty() && !text.endsWith("\n")) {
            appended.append('\n');
        }
        if (!text.isEmpty()) {
            appended.append('\n');
        }
        appended.append("*** Keywords ***\n").append(body);
        int caret = appended.indexOf("No Operation");
        return new Edit(text.length(), appended.toString(), caret);
    }

    /**
     * Offset just past the last line of the {@code *** Keywords ***} section, or
     * {@code -1} when the file has no such section.
     */
    private static int keywordsSectionEnd(String text) {
        int sectionStart = -1;
        int lineStart = 0;

        for (int i = 0; i <= text.length(); i++) {
            boolean endOfText = i == text.length();
            if (!endOfText && text.charAt(i) != '\n') {
                continue;
            }
            String line = text.substring(lineStart, i);
            String raw = RobotSections.rawHeaderName(line);
            if (RobotSections.isHeader(line)) {
                String canonical = raw == null ? null : RobotSections.canonicalName(raw);
                if ("Keywords".equals(canonical)) {
                    sectionStart = lineStart;
                } else if (sectionStart >= 0) {
                    return trimTrailingBlankLines(text, lineStart);
                }
            }
            lineStart = i + 1;
        }
        return sectionStart >= 0 ? trimTrailingBlankLines(text, text.length()) : -1;
    }

    /** Moves the insertion point above any blank lines that precede {@code end}. */
    private static int trimTrailingBlankLines(String text, int end) {
        int cut = end;
        while (cut > 0 && text.charAt(cut - 1) == '\n') {
            int previousLineStart = text.lastIndexOf('\n', cut - 2) + 1;
            String previous = text.substring(previousLineStart, cut - 1);
            if (!previous.trim().isEmpty()) {
                break;
            }
            cut = previousLineStart;
        }
        return cut;
    }

    private static boolean needsBlankLine(String text, int offset) {
        if (offset == 0) {
            return false;
        }
        int lineStart = text.lastIndexOf('\n', offset - 2) + 1;
        String previous = text.substring(lineStart, Math.max(lineStart, offset - 1));
        return !previous.trim().isEmpty() && !RobotSections.isHeader(previous);
    }

    /**
     * Extracts a keyword name from the statement the caret sits on: the first
     * data cell that is not an assignment, a setting or a control structure.
     * Returns {@code null} when the line holds no usable name.
     */
    @Nullable
    public static String keywordNameAt(String source, int offset) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        String text = source.replace("\r\n", "\n").replace('\r', '\n');
        int safeOffset = Math.max(0, Math.min(offset, text.length()));
        int lineStart = text.lastIndexOf('\n', Math.max(0, safeOffset - 1)) + 1;
        int lineEnd = text.indexOf('\n', safeOffset);
        if (lineEnd < 0) {
            lineEnd = text.length();
        }
        String line = text.substring(lineStart, lineEnd);
        if (line.isEmpty() || (line.charAt(0) != ' ' && line.charAt(0) != '\t')) {
            return null;
        }

        List<RobotSyntax.Cell> cells = RobotSyntax.splitCells(line);
        for (RobotSyntax.Cell cell : cells) {
            String value = cell.getText();
            if (value.startsWith("[") || "...".equals(value)) {
                return null;
            }
            if (value.startsWith("$") || value.startsWith("@") || value.startsWith("&")) {
                continue;
            }
            if (RobotSyntax.isMarker(value)) {
                return null;
            }
            return value;
        }
        return null;
    }
}
