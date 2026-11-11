/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RobotFormatterOptions and RobotFinding")
class RobotFormatterOptionsAndFindingTest {

    @Test
    void defaultsAreTheDocumentedOnes() {
        RobotFormatterOptions o = RobotFormatterOptions.defaults();
        assertEquals(4, o.getIndentSize());
        assertTrue(o.isNormalizeSectionHeaders());
        assertTrue(o.isTrimTrailingWhitespace());
        assertTrue(o.isSeparateSections());
        assertEquals(1, o.getMaxConsecutiveBlankLines());
        assertTrue(o.isEnsureTrailingNewline());
    }

    @Test
    void builderClampsInvalidValues() {
        RobotFormatterOptions o = new RobotFormatterOptions.Builder()
                .indentSize(1)
                .maxConsecutiveBlankLines(-3)
                .build();
        assertEquals(2, o.getIndentSize());
        assertEquals(0, o.getMaxConsecutiveBlankLines());
    }

    @Test
    void toBuilderRoundTripsEveryOption() {
        RobotFormatterOptions custom = new RobotFormatterOptions.Builder()
                .indentSize(8)
                .normalizeSectionHeaders(false)
                .trimTrailingWhitespace(false)
                .separateSections(false)
                .maxConsecutiveBlankLines(3)
                .ensureTrailingNewline(false)
                .build();
        RobotFormatterOptions copy = custom.toBuilder().build();
        assertEquals(8, copy.getIndentSize());
        assertFalse(copy.isNormalizeSectionHeaders());
        assertFalse(copy.isTrimTrailingWhitespace());
        assertFalse(copy.isSeparateSections());
        assertEquals(3, copy.getMaxConsecutiveBlankLines());
        assertFalse(copy.isEnsureTrailingNewline());
    }

    @Test
    void findingExposesItsFields() {
        RobotFinding f = new RobotFinding(RobotFinding.Category.TAB_INDENTATION, 10, 14, 3, "tab", "    ");
        assertEquals(RobotFinding.Category.TAB_INDENTATION, f.getCategory());
        assertEquals(10, f.getStartOffset());
        assertEquals(14, f.getEndOffset());
        assertEquals(4, f.getLength());
        assertEquals(3, f.getLine());
        assertEquals("tab", f.getMessage());
        assertEquals("    ", f.getReplacement());
        assertEquals("TAB_INDENTATION@3[10,14] tab", f.toString());
    }

    @Test
    void findingWithoutReplacementIsNotFixable() {
        assertNull(new RobotFinding(RobotFinding.Category.ORPHAN_END, 0, 3, 1, "orphan").getReplacement());
    }

    @Test
    void findingEqualityCoversEveryField() {
        RobotFinding a = new RobotFinding(RobotFinding.Category.TODO_COMMENT, 1, 2, 1, "m", "r");
        RobotFinding same = new RobotFinding(RobotFinding.Category.TODO_COMMENT, 1, 2, 1, "m", "r");
        assertEquals(a, same);
        assertEquals(a.hashCode(), same.hashCode());
        assertEquals(a, a);
        assertNotEquals(a, new RobotFinding(RobotFinding.Category.UNUSED_VARIABLE, 1, 2, 1, "m", "r"));
        assertNotEquals(a, new RobotFinding(RobotFinding.Category.TODO_COMMENT, 0, 2, 1, "m", "r"));
        assertNotEquals(a, new RobotFinding(RobotFinding.Category.TODO_COMMENT, 1, 3, 1, "m", "r"));
        assertNotEquals(a, new RobotFinding(RobotFinding.Category.TODO_COMMENT, 1, 2, 2, "m", "r"));
        assertNotEquals(a, new RobotFinding(RobotFinding.Category.TODO_COMMENT, 1, 2, 1, "x", "r"));
        assertNotEquals(a, new RobotFinding(RobotFinding.Category.TODO_COMMENT, 1, 2, 1, "m", null));
        assertNotEquals(a, "not a finding");
    }
}
