/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Locates test cases, tasks and user keywords in a Robot Framework source file.
 *
 * <p>Used by test execution (which test is under the caret?), the run gutter
 * icons and the keyword generator. Pure and dependency-free so it stays unit
 * testable without an IDE fixture.</p>
 */
public final class RobotStructure {

    /** A named block — a test case, a task or a user keyword. */
    public static final class Entry {
        private final String name;
        private final int nameStart;
        private final int nameEnd;
        private final int bodyEnd;
        private final int line;
        private final boolean keyword;

        Entry(String name, int nameStart, int nameEnd, int bodyEnd, int line, boolean keyword) {
            this.name = name;
            this.nameStart = nameStart;
            this.nameEnd = nameEnd;
            this.bodyEnd = bodyEnd;
            this.line = line;
            this.keyword = keyword;
        }

        /** Name as written in the file. */
        public String getName() {
            return name;
        }

        /** Offset of the first character of the name. */
        public int getNameStart() {
            return nameStart;
        }

        /** Offset just past the last character of the name. */
        public int getNameEnd() {
            return nameEnd;
        }

        /** Offset just past the last line that belongs to this block. */
        public int getBodyEnd() {
            return bodyEnd;
        }

        /** 1-based line of the name. */
        public int getLine() {
            return line;
        }

        /** {@code true} for a user keyword, {@code false} for a test case or task. */
        public boolean isKeyword() {
            return keyword;
        }

        /** {@code true} when {@code offset} falls inside the name or the body. */
        public boolean contains(int offset) {
            return offset >= nameStart && offset <= bodyEnd;
        }
    }

    private RobotStructure() {
    }

    /** All test cases and tasks in the file, in source order. */
    public static List<Entry> testCases(String source) {
        return collect(source, false);
    }

    /** All user keywords in the file, in source order. */
    public static List<Entry> keywords(String source) {
        return collect(source, true);
    }

    /**
     * Returns the test case or task containing {@code offset}, or {@code null}
     * when the offset is outside every test case.
     */
    @Nullable
    public static Entry testCaseAt(String source, int offset) {
        for (Entry entry : testCases(source)) {
            if (entry.contains(offset)) {
                return entry;
            }
        }
        return null;
    }

    /** Returns {@code true} when the file declares at least one test case or task. */
    public static boolean hasTestCases(String source) {
        return !testCases(source).isEmpty();
    }

    private static List<Entry> collect(String source, boolean wantKeywords) {
        List<Entry> entries = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return entries;
        }
        String text = source.replace("\r\n", "\n").replace('\r', '\n');

        boolean inWantedSection = false;
        int lineStart = 0;
        int lineNumber = 1;
        Entry pending = null;
        int pendingEnd = 0;

        for (int i = 0; i <= text.length(); i++) {
            boolean endOfText = i == text.length();
            if (!endOfText && text.charAt(i) != '\n') {
                continue;
            }
            String line = text.substring(lineStart, i);

            String section = sectionName(line);
            if (section != null) {
                if (pending != null) {
                    entries.add(withBodyEnd(pending, pendingEnd));
                    pending = null;
                }
                inWantedSection = wantKeywords
                        ? "Keywords".equals(section)
                        : "Test Cases".equals(section) || "Tasks".equals(section);
            } else if (inWantedSection) {
                String trimmed = line.trim();
                boolean blank = trimmed.isEmpty();
                boolean comment = trimmed.startsWith("#");
                boolean indented = !line.isEmpty() && (line.charAt(0) == ' ' || line.charAt(0) == '\t');

                if (!blank && !comment && !indented) {
                    if (pending != null) {
                        entries.add(withBodyEnd(pending, pendingEnd));
                    }
                    pending = new Entry(trimmed, lineStart, lineStart + trimmed.length(),
                            lineStart + trimmed.length(), lineNumber, wantKeywords);
                    pendingEnd = i;
                } else if (pending != null && !blank) {
                    pendingEnd = i;
                }
            }

            lineStart = i + 1;
            lineNumber++;
        }
        if (pending != null) {
            entries.add(withBodyEnd(pending, pendingEnd));
        }
        return Collections.unmodifiableList(entries);
    }

    private static Entry withBodyEnd(Entry entry, int bodyEnd) {
        return new Entry(entry.name, entry.nameStart, entry.nameEnd,
                Math.max(bodyEnd, entry.nameEnd), entry.line, entry.keyword);
    }

    @Nullable
    private static String sectionName(String line) {
        String raw = RobotSections.rawHeaderName(line);
        if (raw == null) {
            return RobotSections.isHeader(line) ? "" : null;
        }
        String canonical = RobotSections.canonicalName(raw);
        return canonical != null ? canonical : "";
    }

    /**
     * Suggests a unique user keyword name derived from {@code base}, appending a
     * counter when the file already declares that name.
     */
    public static String uniqueKeywordName(String source, String base) {
        List<Entry> existing = keywords(source);
        String candidate = base;
        int counter = 2;
        while (isTaken(existing, candidate)) {
            candidate = base + " " + counter;
            counter++;
        }
        return candidate;
    }

    private static boolean isTaken(List<Entry> entries, String name) {
        String normalized = normalize(name);
        for (Entry entry : entries) {
            if (normalize(entry.getName()).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
    }
}
