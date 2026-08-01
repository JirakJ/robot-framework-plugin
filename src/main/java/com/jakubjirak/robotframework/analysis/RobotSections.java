/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recognition and canonicalization of Robot Framework section headers
 * (for example {@code *** Test Cases ***}).
 *
 * <p>Shared by {@link RobotFormatter} and {@link RobotCodeAnalyzer} so both apply
 * exactly the same rules. All logic is pure and free of IDE dependencies.</p>
 */
public final class RobotSections {

    /** A line is a section header when it starts with one or more asterisks. */
    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^\\s*\\*+\\s*(.*?)\\s*\\**\\s*$");

    /** Canonical spelling keyed by normalized (lower-cased, singular-tolerant) name. */
    private static final Map<String, String> CANONICAL = new LinkedHashMap<>();

    /** Singular spellings, deprecated since Robot Framework 6.0. */
    private static final Set<String> SINGULAR = Set.of(
            "setting", "variable", "test case", "task", "keyword", "comment");

    static {
        // Robot Framework accepts both singular and plural spellings of most headers.
        register("settings", "Settings");
        register("setting", "Settings");
        register("variables", "Variables");
        register("variable", "Variables");
        register("test cases", "Test Cases");
        register("test case", "Test Cases");
        register("tasks", "Tasks");
        register("task", "Tasks");
        register("keywords", "Keywords");
        register("keyword", "Keywords");
        register("comments", "Comments");
        register("comment", "Comments");
    }

    private RobotSections() {
    }

    private static void register(String normalized, String canonical) {
        CANONICAL.put(normalized, canonical);
    }

    /**
     * Returns the section name found on the line, or {@code null} when the line is
     * not a section header. The name is returned verbatim (not canonicalized).
     */
    @Nullable
    public static String rawHeaderName(String line) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        String inner = matcher.group(1).trim();
        return inner.isEmpty() ? null : inner;
    }

    /** Returns {@code true} when the line is a section header. */
    public static boolean isHeader(String line) {
        return rawHeaderName(line) != null;
    }

    /**
     * Returns {@code true} when the line is a header whose name is a recognized
     * Robot Framework section.
     */
    public static boolean isKnownHeader(String line) {
        String name = rawHeaderName(line);
        return name != null && CANONICAL.containsKey(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the canonical spelling for a bare section name (not a whole header
     * line), or {@code null} when the name is not a known section.
     */
    @Nullable
    public static String canonicalName(String name) {
        return name == null ? null : CANONICAL.get(name.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Returns {@code true} when the header line uses a singular section name such
     * as {@code *** Test Case ***}, deprecated since Robot Framework 6.0.
     */
    public static boolean isSingularHeader(String line) {
        String name = rawHeaderName(line);
        return name != null && SINGULAR.contains(name.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the canonical form of a header line (for example
     * {@code *** Test Cases ***}). Unknown headers keep their original name but are
     * still wrapped in the canonical {@code *** name ***} delimiters.
     * Returns {@code null} when the line is not a header.
     */
    @Nullable
    public static String canonicalize(String line) {
        String name = rawHeaderName(line);
        if (name == null) {
            return null;
        }
        String canonical = CANONICAL.get(name.toLowerCase(Locale.ROOT));
        return "*** " + (canonical != null ? canonical : name) + " ***";
    }
}
