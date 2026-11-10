/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

/**
 * Immutable configuration for {@link RobotFormatter}.
 *
 * <p>All options have sensible Robot Framework defaults (4-space indentation,
 * normalized section headers, trimmed trailing whitespace). Use {@link #defaults()}
 * for the recommended profile or {@link Builder} for custom configurations.</p>
 */
public final class RobotFormatterOptions {

    private final int indentSize;
    private final boolean normalizeSectionHeaders;
    private final boolean trimTrailingWhitespace;
    private final boolean separateSections;
    private final int maxConsecutiveBlankLines;
    private final boolean ensureTrailingNewline;

    private RobotFormatterOptions(Builder builder) {
        this.indentSize = builder.indentSize;
        this.normalizeSectionHeaders = builder.normalizeSectionHeaders;
        this.trimTrailingWhitespace = builder.trimTrailingWhitespace;
        this.separateSections = builder.separateSections;
        this.maxConsecutiveBlankLines = builder.maxConsecutiveBlankLines;
        this.ensureTrailingNewline = builder.ensureTrailingNewline;
    }

    public static RobotFormatterOptions defaults() {
        return new Builder().build();
    }

    public int getIndentSize() {
        return indentSize;
    }

    public boolean isNormalizeSectionHeaders() {
        return normalizeSectionHeaders;
    }

    public boolean isTrimTrailingWhitespace() {
        return trimTrailingWhitespace;
    }

    public boolean isSeparateSections() {
        return separateSections;
    }

    public int getMaxConsecutiveBlankLines() {
        return maxConsecutiveBlankLines;
    }

    public boolean isEnsureTrailingNewline() {
        return ensureTrailingNewline;
    }

    public Builder toBuilder() {
        return new Builder()
                .indentSize(indentSize)
                .normalizeSectionHeaders(normalizeSectionHeaders)
                .trimTrailingWhitespace(trimTrailingWhitespace)
                .separateSections(separateSections)
                .maxConsecutiveBlankLines(maxConsecutiveBlankLines)
                .ensureTrailingNewline(ensureTrailingNewline);
    }

    public static final class Builder {
        private int indentSize = 4;
        private boolean normalizeSectionHeaders = true;
        private boolean trimTrailingWhitespace = true;
        private boolean separateSections = true;
        private int maxConsecutiveBlankLines = 1;
        private boolean ensureTrailingNewline = true;

        public Builder indentSize(int value) {
            // Robot Framework requires at least two spaces as a cell separator.
            this.indentSize = Math.max(2, value);
            return this;
        }

        public Builder normalizeSectionHeaders(boolean value) {
            this.normalizeSectionHeaders = value;
            return this;
        }

        public Builder trimTrailingWhitespace(boolean value) {
            this.trimTrailingWhitespace = value;
            return this;
        }

        public Builder separateSections(boolean value) {
            this.separateSections = value;
            return this;
        }

        public Builder maxConsecutiveBlankLines(int value) {
            this.maxConsecutiveBlankLines = Math.max(0, value);
            return this;
        }

        public Builder ensureTrailingNewline(boolean value) {
            this.ensureTrailingNewline = value;
            return this;
        }

        public RobotFormatterOptions build() {
            return new RobotFormatterOptions(this);
        }
    }
}
