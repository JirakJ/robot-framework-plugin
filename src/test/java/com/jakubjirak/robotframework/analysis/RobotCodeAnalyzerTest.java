/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RobotCodeAnalyzer}.
 */
@DisplayName("RobotCodeAnalyzer")
class RobotCodeAnalyzerTest {

    private List<RobotFinding> analyze(String src) {
        return RobotCodeAnalyzer.analyze(src);
    }

    private List<RobotFinding> ofCategory(String src, RobotFinding.Category category) {
        return analyze(src).stream()
                .filter(f -> f.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("empty and null input produce no findings")
    void emptyInput() {
        assertTrue(analyze("").isEmpty());
        assertTrue(analyze(null).isEmpty());
    }

    @Nested
    @DisplayName("tab indentation")
    class Tabs {

        @Test
        @DisplayName("a tab character is reported with a one-char range at its offset")
        void tabReported() {
            String src = "*** Test Cases ***\nExample\n\tLog    Hi\n";
            List<RobotFinding> tabs = ofCategory(src, RobotFinding.Category.TAB_INDENTATION);
            assertEquals(1, tabs.size());
            RobotFinding f = tabs.get(0);
            assertEquals(src.indexOf('\t'), f.getStartOffset());
            assertEquals(1, f.getLength());
            assertEquals(3, f.getLine());
        }

        @Test
        @DisplayName("space-indented files produce no tab findings")
        void noTabs() {
            String src = "*** Test Cases ***\nExample\n    Log    Hi\n";
            assertTrue(ofCategory(src, RobotFinding.Category.TAB_INDENTATION).isEmpty());
        }
    }

    @Nested
    @DisplayName("trailing whitespace")
    class TrailingWhitespace {

        @Test
        @DisplayName("trailing spaces are reported from the first trailing char to EOL")
        void trailingSpaces() {
            String src = "*** Settings ***\nLibrary    Collections   \n";
            List<RobotFinding> ws = ofCategory(src, RobotFinding.Category.TRAILING_WHITESPACE);
            assertEquals(1, ws.size());
            RobotFinding f = ws.get(0);
            assertEquals("Library    Collections".length(),
                    f.getStartOffset() - src.indexOf("Library"));
            assertEquals(3, f.getLength());
        }

        @Test
        @DisplayName("clean lines produce no trailing-whitespace findings")
        void cleanLines() {
            String src = "*** Settings ***\nLibrary    Collections\n";
            assertTrue(ofCategory(src, RobotFinding.Category.TRAILING_WHITESPACE).isEmpty());
        }
    }

    @Nested
    @DisplayName("duplicate test names")
    class DuplicateNames {

        @Test
        @DisplayName("a repeated test name is flagged once, on the second occurrence")
        void duplicate() {
            String src = "*** Test Cases ***\nLogin Works\n    Log    a\nLogin Works\n    Log    b\n";
            List<RobotFinding> dups = ofCategory(src, RobotFinding.Category.DUPLICATE_TEST_NAME);
            assertEquals(1, dups.size());
            assertEquals(4, dups.get(0).getLine());
        }

        @Test
        @DisplayName("matching is case, space and underscore insensitive")
        void normalizedMatching() {
            String src = "*** Test Cases ***\nLogin Works\n    Log    a\nlogin_works\n    Log    b\n";
            assertEquals(1, ofCategory(src, RobotFinding.Category.DUPLICATE_TEST_NAME).size());
        }

        @Test
        @DisplayName("a keyword sharing a test's name is not a duplicate")
        void keywordNotDuplicate() {
            String src = "*** Test Cases ***\nDo Thing\n    Log    a\n\n*** Keywords ***\nDo Thing\n    Log    b\n";
            assertTrue(ofCategory(src, RobotFinding.Category.DUPLICATE_TEST_NAME).isEmpty());
        }

        @Test
        @DisplayName("unique test names produce no findings")
        void unique() {
            String src = "*** Test Cases ***\nFirst\n    Log    a\nSecond\n    Log    b\n";
            assertTrue(ofCategory(src, RobotFinding.Category.DUPLICATE_TEST_NAME).isEmpty());
        }
    }

    @Nested
    @DisplayName("TODO markers")
    class TodoMarkers {

        @Test
        @DisplayName("TODO, FIXME and XXX in comments are reported")
        void markers() {
            assertEquals(1, ofCategory("# TODO fix this\n", RobotFinding.Category.TODO_COMMENT).size());
            assertEquals(1, ofCategory("    Log    x    # FIXME later\n",
                    RobotFinding.Category.TODO_COMMENT).size());
            assertEquals(1, ofCategory("# XXX hack\n", RobotFinding.Category.TODO_COMMENT).size());
        }

        @Test
        @DisplayName("the marker word outside a comment is ignored")
        void notInComment() {
            String src = "*** Test Cases ***\nTODO Driven Test\n    Log    todo is a name\n";
            assertTrue(ofCategory(src, RobotFinding.Category.TODO_COMMENT).isEmpty());
        }
    }

    @Nested
    @DisplayName("unused variables")
    class UnusedVariables {

        @Test
        @DisplayName("a declared but never referenced variable is reported")
        void unused() {
            String src = "*** Variables ***\n${UNUSED}    1\n";
            List<RobotFinding> v = ofCategory(src, RobotFinding.Category.UNUSED_VARIABLE);
            assertEquals(1, v.size());
            assertEquals(2, v.get(0).getLine());
            assertTrue(v.get(0).getMessage().contains("${UNUSED}"));
        }

        @Test
        @DisplayName("a variable referenced elsewhere is not reported")
        void used() {
            String src = "*** Variables ***\n${BASE_URL}    http://x\n\n"
                    + "*** Test Cases ***\nOpen\n    Log    ${BASE_URL}\n";
            assertTrue(ofCategory(src, RobotFinding.Category.UNUSED_VARIABLE).isEmpty());
        }

        @Test
        @DisplayName("item access counts as usage")
        void itemAccessUsage() {
            String src = "*** Variables ***\n&{USER}    name=bob\n\n"
                    + "*** Test Cases ***\nGreet\n    Log    ${USER}[name]\n";
            assertTrue(ofCategory(src, RobotFinding.Category.UNUSED_VARIABLE).isEmpty());
        }
    }

    @Test
    @DisplayName("findings are ordered by start offset")
    void orderedByOffset() {
        String src = "*** Test Cases ***\nExample\n\tLog    x   \n";
        List<RobotFinding> all = analyze(src);
        assertFalse(all.isEmpty());
        for (int i = 1; i < all.size(); i++) {
            assertTrue(all.get(i - 1).getStartOffset() <= all.get(i).getStartOffset());
        }
    }
}
