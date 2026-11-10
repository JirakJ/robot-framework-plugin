/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RobotFormatter}.
 */
@DisplayName("RobotFormatter")
class RobotFormatterTest {

    private final RobotFormatter formatter = new RobotFormatter();

    @Nested
    @DisplayName("empty input")
    class EmptyInput {

        @Test
        @DisplayName("null is returned unchanged")
        void nullInput() {
            assertNull(formatter.format(null));
        }

        @Test
        @DisplayName("empty string is returned unchanged")
        void emptyInput() {
            assertEquals("", formatter.format(""));
        }
    }

    @Test
    @DisplayName("tabs are expanded to the configured indent size")
    void tabsExpanded() {
        String input = "*** Test Cases ***\nExample\n\tLog\tHello\n";
        String expected = "*** Test Cases ***\nExample\n    Log    Hello\n";
        assertEquals(expected, formatter.format(input));
    }

    @Test
    @DisplayName("custom indent size controls tab expansion width")
    void customIndentSize() {
        RobotFormatter twoSpace = new RobotFormatter(
                RobotFormatterOptions.defaults().toBuilder().indentSize(2).build());
        assertEquals("a  b\n", twoSpace.format("a\tb\n"));
    }

    @Nested
    @DisplayName("section header normalization")
    class HeaderNormalization {

        @Test
        @DisplayName("lower-case header is canonicalized")
        void lowerCase() {
            assertEquals("*** Test Cases ***\n", formatter.format("*** test cases ***\n"));
        }

        @Test
        @DisplayName("singular spelling is canonicalized to plural")
        void singular() {
            assertEquals("*** Settings ***\n", formatter.format("*** setting ***\n"));
        }

        @Test
        @DisplayName("irregular asterisk spacing is normalized")
        void spacing() {
            assertEquals("*** Keywords ***\n", formatter.format("***Keywords***\n"));
        }

        @Test
        @DisplayName("unknown header keeps its name but gets canonical delimiters")
        void unknownHeader() {
            assertEquals("*** My Custom ***\n", formatter.format("*****  My Custom *\n"));
        }

        @Test
        @DisplayName("normalization can be disabled")
        void disabled() {
            RobotFormatter raw = new RobotFormatter(
                    RobotFormatterOptions.defaults().toBuilder().normalizeSectionHeaders(false).build());
            assertEquals("*** test cases ***\n", raw.format("*** test cases ***\n"));
        }
    }

    @Test
    @DisplayName("trailing whitespace is stripped")
    void trailingWhitespaceStripped() {
        assertEquals("*** Settings ***\nLibrary    Collections\n",
                formatter.format("*** Settings ***   \nLibrary    Collections\t\n"));
    }

    @Test
    @DisplayName("runs of blank lines are collapsed to one")
    void blankLinesCollapsed() {
        String input = "*** Settings ***\nLibrary    Collections\n\n\n\n*** Variables ***\n${X}    1\n";
        String expected = "*** Settings ***\nLibrary    Collections\n\n*** Variables ***\n${X}    1\n";
        assertEquals(expected, formatter.format(input));
    }

    @Test
    @DisplayName("a blank line is inserted before a section header when missing")
    void blankLineInsertedBeforeHeader() {
        String input = "*** Settings ***\nLibrary    Collections\n*** Variables ***\n${X}    1\n";
        String expected = "*** Settings ***\nLibrary    Collections\n\n*** Variables ***\n${X}    1\n";
        assertEquals(expected, formatter.format(input));
    }

    @Test
    @DisplayName("CRLF and CR line endings are normalized to LF")
    void lineEndingsNormalized() {
        assertEquals("*** Settings ***\nLibrary    Collections\n",
                formatter.format("*** Settings ***\r\nLibrary    Collections\r"));
    }

    @Test
    @DisplayName("a single trailing newline is ensured")
    void trailingNewlineEnsured() {
        assertEquals("*** Settings ***\n", formatter.format("*** Settings ***"));
    }

    @Test
    @DisplayName("trailing blank lines at EOF are removed")
    void trailingBlankLinesRemoved() {
        assertEquals("*** Settings ***\n", formatter.format("*** Settings ***\n\n\n"));
    }

    @Test
    @DisplayName("formatting is idempotent")
    void idempotent() {
        String messy = "***test cases***\nExample\n\tLog\tHi   \n\n\n\nOther\n\tNo Operation\n";
        String once = formatter.format(messy);
        String twice = formatter.format(once);
        assertEquals(once, twice);
    }

    @Test
    @DisplayName("a leading blank line before the first header is removed")
    void leadingBlankRemoved() {
        String formatted = formatter.format("\n\n*** Settings ***\nLibrary    OperatingSystem\n");
        assertTrue(formatted.startsWith("*** Settings ***"), formatted);
    }
}
