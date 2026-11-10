/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Single source of truth for Robot Framework language syntax as of RF 7.4.
 *
 * <p>Everything the plugin needs to know about markers, settings, scopes and
 * deprecations lives here so that the lexer, the completion contributor, the
 * analyzer and the inspections cannot drift apart. The class is pure and free of
 * IDE dependencies, which keeps it fully unit testable.</p>
 *
 * <p>Version coverage:</p>
 * <ul>
 *   <li>RF 6.0 — {@code Test Tags}, {@code Keyword Tags}, {@code AS} alias,
 *       deprecated singular section headers.</li>
 *   <li>RF 6.1 — {@code Name} setting, {@code &amp;{OPTIONS}} variable.</li>
 *   <li>RF 7.0 — {@code VAR}, inline {@code IF}, {@code [Setup]} on user keywords,
 *       {@code -tag} removal in {@code [Tags]}, deprecated {@code [Return]}.</li>
 *   <li>RF 7.1 — {@code SUITES} scope for {@code VAR}.</li>
 *   <li>RF 7.2 — {@code GROUP} block.</li>
 *   <li>RF 7.3 — variable type conversion, {@code ${name: type}}.</li>
 *   <li>RF 7.4 — {@code Secret} variables.</li>
 * </ul>
 */
public final class RobotSyntax {

    /** Robot Framework release this syntax model tracks. */
    public static final String TARGET_VERSION = "7.4";

    // ---- Control structures ---------------------------------------------------

    /** Markers that open a block terminated by {@code END}. {@code GROUP} is RF 7.2. */
    public static final Set<String> BLOCK_OPENERS =
            Set.of("IF", "FOR", "WHILE", "TRY", "GROUP");

    /** Markers that continue an already open block. */
    public static final Set<String> BLOCK_CONTINUATIONS =
            Set.of("ELSE", "ELSE IF", "EXCEPT", "FINALLY");

    /** Markers that are complete statements on their own. */
    public static final Set<String> STANDALONE_MARKERS =
            Set.of("BREAK", "CONTINUE", "RETURN", "VAR", "END");

    /** {@code FOR} loop separators. */
    public static final Set<String> FOR_SEPARATORS =
            Set.of("IN", "IN RANGE", "IN ENUMERATE", "IN ZIP");

    /** Scopes accepted by {@code VAR}. {@code SUITES} was added in RF 7.1. */
    public static final List<String> VAR_SCOPES =
            List.of("LOCAL", "TEST", "TASK", "SUITE", "SUITES", "GLOBAL");

    /** Configuration parameters accepted by {@code WHILE} (RF 6.1 / 7.0). */
    public static final List<String> WHILE_OPTIONS =
            List.of("limit=", "on_limit=", "on_limit_message=");

    private static final Set<String> ALL_MARKERS = buildAllMarkers();

    // ---- Settings -------------------------------------------------------------

    /** Settings valid in the {@code *** Settings ***} section. */
    public static final List<String> SUITE_SETTINGS = List.of(
            "Library", "Resource", "Variables", "Documentation", "Metadata", "Name",
            "Suite Setup", "Suite Teardown",
            "Test Setup", "Test Teardown", "Test Template", "Test Timeout", "Test Tags",
            "Task Setup", "Task Teardown", "Task Template", "Task Timeout", "Task Tags",
            "Keyword Tags");

    /** Settings valid inside a test case or task. */
    public static final List<String> TEST_SETTINGS = List.of(
            "[Documentation]", "[Tags]", "[Setup]", "[Teardown]", "[Template]", "[Timeout]");

    /** Settings valid inside a user keyword. {@code [Setup]} was added in RF 7.0. */
    public static final List<String> KEYWORD_SETTINGS = List.of(
            "[Documentation]", "[Tags]", "[Arguments]", "[Setup]", "[Teardown]", "[Timeout]");

    // ---- Built-in variables ---------------------------------------------------

    /** Automatic variables provided by Robot Framework. {@code &{OPTIONS}} is RF 6.1. */
    public static final List<String> BUILT_IN_VARIABLES = List.of(
            "${CURDIR}", "${TEMPDIR}", "${EXECDIR}", "${/}", "${:}", "${\\n}",
            "${SPACE}", "${EMPTY}", "@{EMPTY}", "&{EMPTY}", "${True}", "${False}", "${None}", "${null}",
            "${TEST NAME}", "@{TEST TAGS}", "${TEST DOCUMENTATION}", "${TEST STATUS}", "${TEST MESSAGE}",
            "${PREV TEST NAME}", "${PREV TEST STATUS}", "${PREV TEST MESSAGE}",
            "${SUITE NAME}", "${SUITE SOURCE}", "${SUITE DOCUMENTATION}",
            "&{SUITE METADATA}", "${SUITE STATUS}", "${SUITE MESSAGE}",
            "${KEYWORD STATUS}", "${KEYWORD MESSAGE}",
            "${LOG LEVEL}", "${OUTPUT DIR}", "${OUTPUT FILE}", "${LOG FILE}", "${REPORT FILE}",
            "${DEBUG FILE}", "&{OPTIONS}");

    // ---- Type conversion (RF 7.3) --------------------------------------------

    /**
     * Types Robot Framework converts out of the box. Used for completion only —
     * user libraries may register arbitrary custom types, so this list is never
     * used to reject a type.
     */
    public static final List<String> CONVERSION_TYPES = List.of(
            "Any", "str", "bool", "int", "float", "Decimal", "bytes", "bytearray",
            "datetime", "date", "timedelta", "Path", "None",
            "list", "tuple", "dict", "set", "frozenset", "Literal", "Union", "Secret");

    // ---- Deprecations ---------------------------------------------------------

    /**
     * A deprecated construct together with its replacement and the release that
     * deprecated it.
     */
    public static final class Deprecation {
        private final String replacement;
        private final String since;
        private final String advice;

        Deprecation(@Nullable String replacement, String since, String advice) {
            this.replacement = replacement;
            this.since = since;
            this.advice = advice;
        }

        /** Literal replacement text, or {@code null} when a mechanical fix is impossible. */
        @Nullable
        public String getReplacement() {
            return replacement;
        }

        /** Robot Framework version that deprecated the construct. */
        public String getSince() {
            return since;
        }

        /** Human readable migration advice. */
        public String getAdvice() {
            return advice;
        }
    }

    private static final Map<String, Deprecation> DEPRECATED_SETTINGS = new LinkedHashMap<>();
    private static final Map<String, Deprecation> DEPRECATED_KEYWORDS = new LinkedHashMap<>();

    static {
        DEPRECATED_SETTINGS.put("[return]", new Deprecation(
                "RETURN", "7.0",
                "The [Return] setting is deprecated. Use the RETURN statement instead."));
        DEPRECATED_SETTINGS.put("force tags", new Deprecation(
                "Test Tags", "6.0",
                "Force Tags was replaced by Test Tags in Robot Framework 6.0."));
        DEPRECATED_SETTINGS.put("default tags", new Deprecation(
                null, "6.0",
                "Default Tags is on its way out. Set the tags with Test Tags and remove "
                        + "them per test with the [Tags] '-tag' syntax."));

        DEPRECATED_KEYWORDS.put("continueforloop", new Deprecation(
                "CONTINUE", "5.0",
                "Continue For Loop is deprecated. Use the CONTINUE statement instead."));
        DEPRECATED_KEYWORDS.put("exitforloop", new Deprecation(
                "BREAK", "5.0",
                "Exit For Loop is deprecated. Use the BREAK statement instead."));
        DEPRECATED_KEYWORDS.put("returnfromkeyword", new Deprecation(
                "RETURN", "5.0",
                "Return From Keyword is deprecated. Use the RETURN statement instead."));
        DEPRECATED_KEYWORDS.put("continueforloopif", new Deprecation(
                null, "5.0",
                "Continue For Loop If is deprecated. Use IF with CONTINUE instead."));
        DEPRECATED_KEYWORDS.put("exitforloopif", new Deprecation(
                null, "5.0",
                "Exit For Loop If is deprecated. Use IF with BREAK instead."));
        DEPRECATED_KEYWORDS.put("returnfromkeywordif", new Deprecation(
                null, "5.0",
                "Return From Keyword If is deprecated. Use IF with RETURN instead."));
        DEPRECATED_KEYWORDS.put("runkeywordunless", new Deprecation(
                null, "5.0",
                "Run Keyword Unless is deprecated. Use native IF with a negated condition."));
    }

    private RobotSyntax() {
    }

    // ---- Marker queries -------------------------------------------------------

    /** Returns {@code true} for any Robot Framework control structure marker. */
    public static boolean isMarker(String cell) {
        return cell != null && ALL_MARKERS.contains(normalizeMarker(cell));
    }

    /** Returns {@code true} for markers that open a block closed by {@code END}. */
    public static boolean isBlockOpener(String cell) {
        return cell != null && BLOCK_OPENERS.contains(normalizeMarker(cell));
    }

    /** Returns {@code true} for {@code ELSE}, {@code ELSE IF}, {@code EXCEPT} and {@code FINALLY}. */
    public static boolean isBlockContinuation(String cell) {
        return cell != null && BLOCK_CONTINUATIONS.contains(normalizeMarker(cell));
    }

    /** Returns {@code true} for the {@code END} marker. */
    public static boolean isBlockEnd(String cell) {
        return cell != null && "END".equals(normalizeMarker(cell));
    }

    /** Returns {@code true} for a {@code FOR} loop separator such as {@code IN ZIP}. */
    public static boolean isForSeparator(String cell) {
        return cell != null && FOR_SEPARATORS.contains(normalizeMarker(cell));
    }

    /**
     * Markers are matched case-insensitively and with runs of whitespace collapsed
     * so that {@code else if} and {@code ELSE  IF} both resolve to {@code ELSE IF}.
     */
    public static String normalizeMarker(String cell) {
        return cell.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    /**
     * Normalizes a keyword or setting name the way Robot Framework matches them:
     * case-insensitive, ignoring spaces and underscores.
     */
    public static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
    }

    // ---- Deprecation queries --------------------------------------------------

    /**
     * Returns the deprecation record for a setting such as {@code [Return]} or
     * {@code Force Tags}, or {@code null} when the setting is current.
     */
    @Nullable
    public static Deprecation deprecatedSetting(String setting) {
        if (setting == null) {
            return null;
        }
        String key = setting.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return DEPRECATED_SETTINGS.get(key);
    }

    /**
     * Returns the deprecation record for a BuiltIn keyword such as
     * {@code Exit For Loop}, or {@code null} when the keyword is current.
     */
    @Nullable
    public static Deprecation deprecatedKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        return DEPRECATED_KEYWORDS.get(normalizeName(keyword));
    }

    /** All deprecated settings, keyed by their normalized name. */
    public static Map<String, Deprecation> deprecatedSettings() {
        return Collections.unmodifiableMap(DEPRECATED_SETTINGS);
    }

    /** All deprecated BuiltIn keywords, keyed by their normalized name. */
    public static Map<String, Deprecation> deprecatedKeywords() {
        return Collections.unmodifiableMap(DEPRECATED_KEYWORDS);
    }

    // ---- Variable type syntax (RF 7.3) ---------------------------------------

    /**
     * A variable token split into its name and its optional type, for example
     * {@code ${amount: int}} → name {@code amount}, type {@code int}.
     */
    public static final class TypedVariable {
        private final String name;
        private final String type;
        private final int typeStart;
        private final int typeEnd;

        TypedVariable(String name, @Nullable String type, int typeStart, int typeEnd) {
            this.name = name;
            this.type = type;
            this.typeStart = typeStart;
            this.typeEnd = typeEnd;
        }

        /** Variable name without the decoration and without the type. */
        public String getName() {
            return name;
        }

        /** Declared type, or {@code null} when the variable is untyped. */
        @Nullable
        public String getType() {
            return type;
        }

        /** Offset of the type inside the original token, or {@code -1}. */
        public int getTypeStart() {
            return typeStart;
        }

        /** End offset of the type inside the original token, or {@code -1}. */
        public int getTypeEnd() {
            return typeEnd;
        }

        /** Returns {@code true} when the variable declares a conversion type. */
        public boolean isTyped() {
            return type != null;
        }
    }

    /**
     * Parses a full variable token such as {@code ${name: list[int]}}.
     *
     * <p>Robot Framework requires a space after the colon, which is what
     * distinguishes a type from a plain name containing a colon (for example the
     * built-in {@code ${:}}). Returns {@code null} when the token is not a
     * variable.</p>
     */
    @Nullable
    public static TypedVariable parseVariable(String token) {
        if (token == null || token.length() < 3) {
            return null;
        }
        char decoration = token.charAt(0);
        if ("$@&%".indexOf(decoration) < 0 || token.charAt(1) != '{') {
            return null;
        }
        int close = matchingBrace(token, 1);
        if (close < 0) {
            return null;
        }
        String body = token.substring(2, close);
        int colon = topLevelTypeColon(body);
        if (colon < 0) {
            return new TypedVariable(body.trim(), null, -1, -1);
        }
        String name = body.substring(0, colon).trim();
        int typeStart = 2 + colon + 1;
        int typeEnd = 2 + body.length();
        String type = token.substring(typeStart, typeEnd);
        return new TypedVariable(name, type.trim(), typeStart, typeEnd);
    }

    /**
     * Validates a type expression such as {@code list[int]}, {@code int | float} or
     * {@code Literal["UP", "DOWN"]}. Returns {@code null} when the expression is
     * well formed, otherwise a message describing the problem.
     *
     * <p>Only structural validity is checked. Unknown type names are accepted on
     * purpose: libraries can register custom converters and enums, so a name-based
     * whitelist would report valid code as broken.</p>
     */
    @Nullable
    public static String validateTypeExpression(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "Type is missing after ':'.";
        }
        String trimmed = type.trim();

        int depth = 0;
        boolean inString = false;
        char quote = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (inString) {
                if (c == quote) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                quote = c;
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth < 0) {
                    return "Unbalanced ']' in type '" + trimmed + "'.";
                }
            }
        }
        if (inString) {
            return "Unterminated string literal in type '" + trimmed + "'.";
        }
        if (depth != 0) {
            return "Unbalanced '[' in type '" + trimmed + "'.";
        }
        if (trimmed.startsWith("|") || trimmed.endsWith("|")) {
            return "Dangling '|' in union type '" + trimmed + "'.";
        }
        if (trimmed.contains("||")) {
            return "Empty union member in type '" + trimmed + "'.";
        }
        if (trimmed.startsWith("[")) {
            return "Type '" + trimmed + "' is missing a base type before '['.";
        }
        return null;
    }

    /**
     * Returns the index of the colon that introduces a type, or {@code -1}.
     * A type colon is followed by whitespace and lives outside nested variables,
     * brackets and string literals.
     */
    private static int topLevelTypeColon(String body) {
        int braceDepth = 0;
        int bracketDepth = 0;
        boolean inString = false;
        char quote = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (inString) {
                if (c == quote) {
                    inString = false;
                }
                continue;
            }
            switch (c) {
                case '"':
                case '\'':
                    inString = true;
                    quote = c;
                    break;
                case '{':
                    braceDepth++;
                    break;
                case '}':
                    braceDepth--;
                    break;
                case '[':
                    bracketDepth++;
                    break;
                case ']':
                    bracketDepth--;
                    break;
                case ':':
                    if (braceDepth == 0 && bracketDepth == 0
                            && i + 1 < body.length() && body.charAt(i + 1) == ' ') {
                        return i;
                    }
                    break;
                default:
                    break;
            }
        }
        return -1;
    }

    /** Returns the offset of the brace closing the one at {@code open}, or {@code -1}. */
    private static int matchingBrace(String text, int open) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static Set<String> buildAllMarkers() {
        Set<String> markers = new LinkedHashSet<>();
        markers.addAll(BLOCK_OPENERS);
        markers.addAll(BLOCK_CONTINUATIONS);
        markers.addAll(STANDALONE_MARKERS);
        markers.addAll(FOR_SEPARATORS);
        return Collections.unmodifiableSet(markers);
    }

    // ---- Cell splitting -------------------------------------------------------

    /** A single Robot Framework data cell together with its offset in the line. */
    public static final class Cell {
        private final String text;
        private final int start;

        Cell(String text, int start) {
            this.text = text;
            this.start = start;
        }

        /** Cell content with surrounding separators removed. */
        public String getText() {
            return text;
        }

        /** Offset of the cell inside its line. */
        public int getStart() {
            return start;
        }

        /** End offset of the cell inside its line. */
        public int getEnd() {
            return start + text.length();
        }

        @Override
        public String toString() {
            return text + "@" + start;
        }
    }

    /**
     * Splits a line into Robot Framework data cells. Cells are separated by two or
     * more spaces or by a single tab, and a trailing comment is dropped.
     *
     * <p>ponytail: space-separated format only. The pipe-separated format is legal
     * but vanishingly rare; add a branch here if a user ever reports it.</p>
     */
    public static List<Cell> splitCells(String line) {
        List<Cell> cells = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            return cells;
        }

        int end = line.length();
        int comment = commentStart(line);
        if (comment >= 0) {
            end = comment;
        }

        int i = 0;
        while (i < end) {
            i = skipSeparator(line, i, end);
            if (i >= end) {
                break;
            }
            int start = i;
            while (i < end && !isSeparatorAt(line, i, end)) {
                i++;
            }
            String text = stripTrailing(line.substring(start, i));
            if (!text.isEmpty()) {
                cells.add(new Cell(text, start));
            }
        }
        return cells;
    }

    /**
     * Advances past a separator run: any number of tabs, or two or more spaces.
     * A single space between words belongs to the cell, which is what lets
     * {@code Log To Console} stay one cell.
     */
    private static int skipSeparator(String line, int from, int end) {
        int i = from;
        boolean consumed = false;
        while (i < end) {
            char c = line.charAt(i);
            if (c == '\t') {
                i++;
                consumed = true;
            } else if (c == ' ' && (consumed || (i + 1 < end && line.charAt(i + 1) == ' '))) {
                i++;
                consumed = true;
            } else {
                break;
            }
        }
        return i;
    }

    /** Returns the offset of a standalone comment on the line, or {@code -1}. */
    public static int commentStart(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != '#') {
                continue;
            }
            // An escaped hash is data, not a comment.
            if (i > 0 && line.charAt(i - 1) == '\\') {
                continue;
            }
            return i;
        }
        return -1;
    }

    private static boolean isSeparatorAt(String line, int i, int end) {
        char c = line.charAt(i);
        if (c == '\t') {
            return true;
        }
        return c == ' ' && i + 1 < end && line.charAt(i + 1) == ' ';
    }

    private static String stripTrailing(String text) {
        int end = text.length();
        while (end > 0 && (text.charAt(end - 1) == ' ' || text.charAt(end - 1) == '\t')) {
            end--;
        }
        return text.substring(0, end);
    }
}
