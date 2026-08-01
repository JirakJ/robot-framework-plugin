/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import java.util.Objects;

/**
 * A single code-quality finding produced by {@link RobotCodeAnalyzer}.
 *
 * <p>Offsets are absolute character positions within the analyzed text using
 * {@code \n} line endings, matching how the IntelliJ document model exposes file
 * content. {@link #getLine()} is 1-based for human-readable reporting.</p>
 */
public final class RobotFinding {

    /** The kind of issue, used to route findings to the matching inspection. */
    public enum Category {
        TAB_INDENTATION,
        TRAILING_WHITESPACE,
        DUPLICATE_TEST_NAME,
        TODO_COMMENT,
        UNUSED_VARIABLE,
        /** Setting replaced by newer syntax, e.g. {@code [Return]} → {@code RETURN}. */
        DEPRECATED_SETTING,
        /** BuiltIn keyword replaced by a control structure, e.g. {@code Exit For Loop} → {@code BREAK}. */
        DEPRECATED_KEYWORD,
        /** Singular section header such as {@code *** Test Case ***} (deprecated in RF 6.0). */
        SINGULAR_SECTION_HEADER,
        /** Malformed type expression in {@code ${name: type}} (RF 7.3). */
        INVALID_VARIABLE_TYPE,
        /** A {@code Secret} variable given a literal value in the Variables section (RF 7.4). */
        SECRET_LITERAL_VALUE,
        /** {@code VAR} creating a non-local variable without a value (deprecated in RF 7.4). */
        VAR_WITHOUT_VALUE,
        /** Literal {@code -tag} in {@code Test Tags} (deprecated in RF 7.2). */
        HYPHEN_TAG_LITERAL,
        /** Block opened with IF/FOR/WHILE/TRY/GROUP and never closed with END. */
        UNCLOSED_BLOCK,
        /** {@code END} without a matching block opener. */
        ORPHAN_END,
        /** {@code FOR ... IN ZIP} without an explicit {@code mode=} (deprecated in RF 7.0). */
        FOR_IN_ZIP_WITHOUT_MODE,
        /** Library alias declared with {@code WITH NAME} instead of {@code AS} (RF 6.0). */
        WITH_NAME_ALIAS,
        /** Data before the first section header. */
        CONTENT_OUTSIDE_SECTION,
        /** Test case or task name longer than the readability threshold. */
        LONG_TEST_NAME,
        /** Test case or task without any executable step. */
        EMPTY_TEST_CASE
    }

    private final Category category;
    private final int startOffset;
    private final int endOffset;
    private final int line;
    private final String message;
    private final String replacement;

    public RobotFinding(Category category, int startOffset, int endOffset, int line, String message) {
        this(category, startOffset, endOffset, line, message, null);
    }

    public RobotFinding(Category category, int startOffset, int endOffset, int line,
                        String message, String replacement) {
        this.category = category;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.line = line;
        this.message = message;
        this.replacement = replacement;
    }

    /**
     * Text that should replace the highlighted range, or {@code null} when the
     * finding cannot be fixed mechanically.
     */
    public String getReplacement() {
        return replacement;
    }

    public Category getCategory() {
        return category;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public int getLength() {
        return endOffset - startOffset;
    }

    public int getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RobotFinding)) {
            return false;
        }
        RobotFinding that = (RobotFinding) o;
        return startOffset == that.startOffset
                && endOffset == that.endOffset
                && line == that.line
                && category == that.category
                && Objects.equals(message, that.message)
                && Objects.equals(replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, startOffset, endOffset, line, message, replacement);
    }

    @Override
    public String toString() {
        return category + "@" + line + "[" + startOffset + "," + endOffset + "] " + message;
    }
}
