/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RobotKeywordGenerator")
class RobotKeywordGeneratorTest {

    private String apply(String source, String name) {
        RobotKeywordGenerator.Edit edit = RobotKeywordGenerator.generate(source, name);
        assertNotNull(edit, "expected an edit for " + name);
        return source.substring(0, edit.getOffset()) + edit.getText() + source.substring(edit.getOffset());
    }

    @Nested
    @DisplayName("Insertion")
    class InsertionTests {
        @Test
        @DisplayName("appends to an existing Keywords section")
        void existingSection() {
            String source = "*** Keywords ***\nExisting\n    No Operation\n";
            String result = apply(source, "New Step");
            assertEquals("*** Keywords ***\n"
                    + "Existing\n"
                    + "    No Operation\n"
                    + "\n"
                    + "New Step\n"
                    + "    [Documentation]    Describe what this keyword does.\n"
                    + "    No Operation\n", result);
        }

        @Test
        @DisplayName("creates the Keywords section when it is missing")
        void missingSection() {
            String source = "*** Test Cases ***\nT\n    Log    hi\n";
            String result = apply(source, "New Step");
            assertTrue(result.endsWith("\n*** Keywords ***\n"
                    + "New Step\n"
                    + "    [Documentation]    Describe what this keyword does.\n"
                    + "    No Operation\n"), result);
            assertTrue(result.startsWith(source), "existing content must be preserved");
        }

        @Test
        @DisplayName("inserts before a section that follows the Keywords section")
        void sectionInTheMiddle() {
            String source = "*** Keywords ***\nExisting\n    No Operation\n\n*** Test Cases ***\nT\n    Existing\n";
            String result = apply(source, "New Step");
            int keyword = result.indexOf("New Step\n");
            int testCases = result.indexOf("*** Test Cases ***");
            assertTrue(keyword < testCases, "the keyword must land inside the Keywords section");
        }

        @Test
        @DisplayName("the caret lands on the generated step")
        void caretPosition() {
            String source = "*** Keywords ***\nExisting\n    No Operation\n";
            RobotKeywordGenerator.Edit edit = RobotKeywordGenerator.generate(source, "New Step");
            assertNotNull(edit);
            String result = source.substring(0, edit.getOffset()) + edit.getText()
                    + source.substring(edit.getOffset());
            assertTrue(result.startsWith("No Operation", edit.getCaretOffset()));
        }

        @Test
        @DisplayName("the generated keyword is itself clean")
        void generatedKeywordHasNoFindings() {
            String source = "*** Test Cases ***\nT\n    New Step\n";
            String result = apply(source, "New Step");
            assertEquals(java.util.List.of(), RobotCodeAnalyzer.analyze(result));
        }
    }

    @Nested
    @DisplayName("Guards")
    class GuardTests {
        @Test
        @DisplayName("a duplicate keyword is refused, matched the Robot Framework way")
        void duplicate() {
            String source = "*** Keywords ***\nShared Step\n    No Operation\n";
            assertNull(RobotKeywordGenerator.generate(source, "Shared Step"));
            assertNull(RobotKeywordGenerator.generate(source, "shared_step"));
        }

        @Test
        @DisplayName("a blank name is refused")
        void blankName() {
            assertNull(RobotKeywordGenerator.generate("*** Keywords ***\n", "   "));
            assertNull(RobotKeywordGenerator.generate("*** Keywords ***\n", null));
        }
    }

    @Nested
    @DisplayName("Name under the caret")
    class NameAtTests {
        private static final String SUITE = "*** Test Cases ***\nT\n"
                + "    Open The Door    quickly\n"
                + "    ${result} =    Compute Total    1    2\n"
                + "    [Tags]    smoke\n"
                + "    IF    ${x}\n";

        @Test
        @DisplayName("a plain keyword call is picked up")
        void plainCall() {
            int offset = SUITE.indexOf("Open The Door");
            assertEquals("Open The Door", RobotKeywordGenerator.keywordNameAt(SUITE, offset));
        }

        @Test
        @DisplayName("assignment cells are skipped")
        void assignment() {
            int offset = SUITE.indexOf("Compute Total");
            assertEquals("Compute Total", RobotKeywordGenerator.keywordNameAt(SUITE, offset));
        }

        @Test
        @DisplayName("settings and control structures yield nothing")
        void notAKeyword() {
            assertNull(RobotKeywordGenerator.keywordNameAt(SUITE, SUITE.indexOf("[Tags]")));
            assertNull(RobotKeywordGenerator.keywordNameAt(SUITE, SUITE.indexOf("IF")));
        }

        @Test
        @DisplayName("a test case name line yields nothing")
        void testCaseName() {
            assertNull(RobotKeywordGenerator.keywordNameAt(SUITE, SUITE.indexOf("\nT\n") + 1));
        }
    }
}
