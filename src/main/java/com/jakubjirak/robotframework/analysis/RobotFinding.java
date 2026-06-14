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
        UNUSED_VARIABLE
    }

    private final Category category;
    private final int startOffset;
    private final int endOffset;
    private final int line;
    private final String message;

    public RobotFinding(Category category, int startOffset, int endOffset, int line, String message) {
        this.category = category;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.line = line;
        this.message = message;
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
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, startOffset, endOffset, line, message);
    }

    @Override
    public String toString() {
        return category + "@" + line + "[" + startOffset + "," + endOffset + "] " + message;
    }
}
