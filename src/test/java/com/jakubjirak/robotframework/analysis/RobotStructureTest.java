/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RobotStructure")
class RobotStructureTest {

    private static final String SUITE = ""
            + "*** Settings ***\n"                       // 1
            + "Library    OperatingSystem\n"             // 2
            + "\n"                                       // 3
            + "*** Test Cases ***\n"                     // 4
            + "First Test\n"                             // 5
            + "    [Tags]    smoke\n"                    // 6
            + "    Log    hello\n"                       // 7
            + "\n"                                       // 8
            + "Second Test\n"                            // 9
            + "    Log    world\n"                       // 10
            + "\n"                                       // 11
            + "*** Keywords ***\n"                       // 12
            + "Shared Step\n"                            // 13
            + "    No Operation\n";                      // 14

    private List<String> namesOf(List<RobotStructure.Entry> entries) {
        return entries.stream().map(RobotStructure.Entry::getName).collect(Collectors.toList());
    }

    @Nested
    @DisplayName("Test cases")
    class TestCaseTests {
        @Test
        @DisplayName("names are collected in source order")
        void names() {
            assertEquals(List.of("First Test", "Second Test"),
                    namesOf(RobotStructure.testCases(SUITE)));
        }

        @Test
        @DisplayName("keywords are not mistaken for test cases")
        void keywordsExcluded() {
            assertFalse(namesOf(RobotStructure.testCases(SUITE)).contains("Shared Step"));
            assertEquals(List.of("Shared Step"), namesOf(RobotStructure.keywords(SUITE)));
        }

        @Test
        @DisplayName("Tasks sections count as test cases")
        void tasks() {
            String source = "*** Tasks ***\nDo Work\n    Log    hi\n";
            assertEquals(List.of("Do Work"), namesOf(RobotStructure.testCases(source)));
        }

        @Test
        @DisplayName("comments and blank lines are not names")
        void commentsIgnored() {
            String source = "*** Test Cases ***\n# just a note\n\nReal Test\n    Log    hi\n";
            assertEquals(List.of("Real Test"), namesOf(RobotStructure.testCases(source)));
        }

        @Test
        @DisplayName("the name offset points at the name")
        void nameOffsets() {
            RobotStructure.Entry first = RobotStructure.testCases(SUITE).get(0);
            assertEquals("First Test", SUITE.substring(first.getNameStart(), first.getNameEnd()));
        }

        @Test
        @DisplayName("an empty file yields nothing")
        void empty() {
            assertEquals(List.of(), RobotStructure.testCases(""));
            assertEquals(List.of(), RobotStructure.testCases(null));
            assertFalse(RobotStructure.hasTestCases("*** Settings ***\nLibrary    X\n"));
        }
    }

    @Nested
    @DisplayName("Test case at offset")
    class OffsetTests {
        @Test
        @DisplayName("an offset on the name resolves to that test")
        void onName() {
            int offset = SUITE.indexOf("First Test");
            assertEquals("First Test", RobotStructure.testCaseAt(SUITE, offset).getName());
        }

        @Test
        @DisplayName("an offset inside the body resolves to the enclosing test")
        void inBody() {
            int offset = SUITE.indexOf("Log    world");
            assertEquals("Second Test", RobotStructure.testCaseAt(SUITE, offset).getName());
        }

        @Test
        @DisplayName("an offset in the Settings section resolves to nothing")
        void outsideTests() {
            assertNull(RobotStructure.testCaseAt(SUITE, SUITE.indexOf("Library")));
        }

        @Test
        @DisplayName("an offset in the Keywords section resolves to nothing")
        void inKeywords() {
            assertNull(RobotStructure.testCaseAt(SUITE, SUITE.indexOf("No Operation")));
        }
    }

    @Nested
    @DisplayName("Unique keyword names")
    class UniqueNameTests {
        @Test
        @DisplayName("a free name is returned unchanged")
        void free() {
            assertEquals("New Step", RobotStructure.uniqueKeywordName(SUITE, "New Step"));
        }

        @Test
        @DisplayName("a taken name gets a counter, matched the Robot Framework way")
        void taken() {
            assertEquals("Shared Step 2", RobotStructure.uniqueKeywordName(SUITE, "Shared Step"));
            assertEquals("shared_step 2", RobotStructure.uniqueKeywordName(SUITE, "shared_step"));
        }
    }

    @Nested
    @DisplayName("Robustness")
    class RobustnessTests {
        @Test
        @DisplayName("Windows line endings are handled")
        void crlf() {
            String source = "*** Test Cases ***\r\nWindows Test\r\n    Log    hi\r\n";
            List<RobotStructure.Entry> entries = RobotStructure.testCases(source);
            assertEquals(1, entries.size());
            assertEquals("Windows Test", entries.get(0).getName());
        }

        @Test
        @DisplayName("a file without a trailing newline still yields its last test")
        void noTrailingNewline() {
            String source = "*** Test Cases ***\nLast Test\n    Log    hi";
            List<RobotStructure.Entry> entries = RobotStructure.testCases(source);
            assertEquals(1, entries.size());
            RobotStructure.Entry entry = entries.get(0);
            assertNotNull(entry);
            assertTrue(entry.contains(source.length() - 1));
        }
    }
}
