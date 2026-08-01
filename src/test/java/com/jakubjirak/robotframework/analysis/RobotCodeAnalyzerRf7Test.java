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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the Robot Framework 6.0–7.4 rules added to {@link RobotCodeAnalyzer}:
 * deprecated syntax, GROUP-aware block balancing, variable type conversion and
 * Secret variables.
 */
@DisplayName("RobotCodeAnalyzer — Robot Framework 7.x rules")
class RobotCodeAnalyzerRf7Test {

    private List<RobotFinding> findings(String source, RobotFinding.Category category) {
        return RobotCodeAnalyzer.analyze(source).stream()
                .filter(f -> f.getCategory() == category)
                .collect(Collectors.toList());
    }

    private String text(String source, RobotFinding finding) {
        return source.substring(finding.getStartOffset(), finding.getEndOffset());
    }

    @Nested
    @DisplayName("Deprecated settings")
    class DeprecatedSettingTests {
        @Test
        @DisplayName("[Return] is reported with a RETURN replacement")
        void returnSetting() {
            String source = "*** Keywords ***\nGet Value\n    [Return]    ${x}\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.DEPRECATED_SETTING);
            assertEquals(1, found.size());
            assertEquals("[Return]", text(source, found.get(0)));
            assertEquals("RETURN", found.get(0).getReplacement());
        }

        @Test
        @DisplayName("Force Tags is reported with a Test Tags replacement")
        void forceTags() {
            String source = "*** Settings ***\nForce Tags    smoke\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.DEPRECATED_SETTING);
            assertEquals(1, found.size());
            assertEquals("Test Tags", found.get(0).getReplacement());
        }

        @Test
        @DisplayName("current settings are silent")
        void currentSettings() {
            String source = "*** Settings ***\nTest Tags    smoke\nKeyword Tags    util\n";
            assertTrue(findings(source, RobotFinding.Category.DEPRECATED_SETTING).isEmpty());
        }

        @Test
        @DisplayName("[Return] inside a comment is ignored")
        void insideComment() {
            String source = "*** Keywords ***\nGet Value\n    # [Return]    ${x}\n    RETURN    1\n";
            assertTrue(findings(source, RobotFinding.Category.DEPRECATED_SETTING).isEmpty());
        }
    }

    @Nested
    @DisplayName("Deprecated keywords")
    class DeprecatedKeywordTests {
        @Test
        @DisplayName("Exit For Loop is reported with a BREAK replacement")
        void exitForLoop() {
            String source = "*** Test Cases ***\nT\n    FOR    ${i}    IN    1    2\n"
                    + "        Exit For Loop\n    END\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.DEPRECATED_KEYWORD);
            assertEquals(1, found.size());
            assertEquals("BREAK", found.get(0).getReplacement());
        }

        @Test
        @DisplayName("assignment cells are skipped when locating the keyword")
        void afterAssignment() {
            String source = "*** Keywords ***\nK\n    ${value} =    Return From Keyword\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.DEPRECATED_KEYWORD);
            assertEquals(1, found.size());
            assertEquals("Return From Keyword", text(source, found.get(0)));
        }

        @Test
        @DisplayName("Run Keyword If stays untouched")
        void runKeywordIf() {
            String source = "*** Test Cases ***\nT\n    Run Keyword If    ${c}    Log    hi\n";
            assertTrue(findings(source, RobotFinding.Category.DEPRECATED_KEYWORD).isEmpty());
        }

        @Test
        @DisplayName("a keyword argument that happens to match is not reported")
        void onlyFirstCell() {
            String source = "*** Test Cases ***\nT\n    Log    Exit For Loop\n";
            assertTrue(findings(source, RobotFinding.Category.DEPRECATED_KEYWORD).isEmpty());
        }
    }

    @Nested
    @DisplayName("Other deprecated syntax")
    class OtherDeprecationTests {
        @Test
        @DisplayName("singular section headers are reported with the plural form")
        void singularHeader() {
            String source = "*** Test Case ***\nT\n    Log    hi\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.SINGULAR_SECTION_HEADER);
            assertEquals(1, found.size());
            assertEquals("*** Test Cases ***", found.get(0).getReplacement());
        }

        @Test
        @DisplayName("plural headers are silent")
        void pluralHeader() {
            String source = "*** Test Cases ***\nT\n    Log    hi\n";
            assertTrue(findings(source, RobotFinding.Category.SINGULAR_SECTION_HEADER).isEmpty());
        }

        @Test
        @DisplayName("WITH NAME is reported with an AS replacement")
        void withName() {
            String source = "*** Settings ***\nLibrary    SeleniumLibrary    WITH NAME    Browser\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.WITH_NAME_ALIAS);
            assertEquals(1, found.size());
            assertEquals("AS", found.get(0).getReplacement());
        }

        @Test
        @DisplayName("AS is accepted")
        void asAlias() {
            String source = "*** Settings ***\nLibrary    SeleniumLibrary    AS    Browser\n";
            assertTrue(findings(source, RobotFinding.Category.WITH_NAME_ALIAS).isEmpty());
        }

        @Test
        @DisplayName("FOR IN ZIP without mode is reported")
        void forInZip() {
            String source = "*** Test Cases ***\nT\n    FOR    ${a}    ${b}    IN ZIP    ${x}    ${y}\n"
                    + "        Log    ${a}\n    END\n";
            assertEquals(1, findings(source, RobotFinding.Category.FOR_IN_ZIP_WITHOUT_MODE).size());
        }

        @Test
        @DisplayName("FOR IN ZIP with an explicit mode is silent")
        void forInZipWithMode() {
            String source = "*** Test Cases ***\nT\n"
                    + "    FOR    ${a}    ${b}    IN ZIP    ${x}    ${y}    mode=STRICT\n"
                    + "        Log    ${a}\n    END\n";
            assertTrue(findings(source, RobotFinding.Category.FOR_IN_ZIP_WITHOUT_MODE).isEmpty());
        }

        @Test
        @DisplayName("a literal -tag in Test Tags is reported with the escaped form")
        void hyphenTag() {
            String source = "*** Settings ***\nTest Tags    -skip    smoke\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.HYPHEN_TAG_LITERAL);
            assertEquals(1, found.size());
            assertEquals("\\-skip", found.get(0).getReplacement());
        }

        @Test
        @DisplayName("-tag in [Tags] is valid RF 7.0 removal syntax and stays silent")
        void hyphenTagInTestSetting() {
            String source = "*** Settings ***\nTest Tags    smoke\n\n"
                    + "*** Test Cases ***\nT\n    [Tags]    -smoke\n    Log    hi\n";
            assertTrue(findings(source, RobotFinding.Category.HYPHEN_TAG_LITERAL).isEmpty());
        }

        @Test
        @DisplayName("VAR without a value in a non-local scope is reported")
        void varWithoutValue() {
            String source = "*** Test Cases ***\nT\n    VAR    ${X}    scope=TEST\n    Log    ${X}\n";
            assertEquals(1, findings(source, RobotFinding.Category.VAR_WITHOUT_VALUE).size());
        }

        @Test
        @DisplayName("VAR with a value or local scope is silent")
        void varWithValue() {
            String withValue = "*** Test Cases ***\nT\n    VAR    ${X}    ${EMPTY}    scope=TEST\n";
            String localScope = "*** Test Cases ***\nT\n    VAR    ${X}    scope=LOCAL\n";
            String noScope = "*** Test Cases ***\nT\n    VAR    ${X}    value\n";
            assertTrue(findings(withValue, RobotFinding.Category.VAR_WITHOUT_VALUE).isEmpty());
            assertTrue(findings(localScope, RobotFinding.Category.VAR_WITHOUT_VALUE).isEmpty());
            assertTrue(findings(noScope, RobotFinding.Category.VAR_WITHOUT_VALUE).isEmpty());
        }
    }

    @Nested
    @DisplayName("Block balance")
    class BlockTests {
        @Test
        @DisplayName("a GROUP without END is reported (RF 7.2)")
        void unclosedGroup() {
            String source = "*** Test Cases ***\nT\n    GROUP    Setup phase\n        Log    hi\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.UNCLOSED_BLOCK);
            assertEquals(1, found.size());
            assertEquals("GROUP", text(source, found.get(0)));
        }

        @Test
        @DisplayName("a closed GROUP is silent")
        void closedGroup() {
            String source = "*** Test Cases ***\nT\n    GROUP    Setup\n        Log    hi\n    END\n";
            assertTrue(findings(source, RobotFinding.Category.UNCLOSED_BLOCK).isEmpty());
        }

        @Test
        @DisplayName("nested blocks are balanced independently")
        void nested() {
            String source = "*** Test Cases ***\nT\n"
                    + "    FOR    ${i}    IN    1    2\n"
                    + "        IF    ${i} > 1\n"
                    + "            GROUP    inner\n"
                    + "                Log    ${i}\n"
                    + "            END\n"
                    + "        END\n"
                    + "    END\n";
            assertTrue(findings(source, RobotFinding.Category.UNCLOSED_BLOCK).isEmpty());
            assertTrue(findings(source, RobotFinding.Category.ORPHAN_END).isEmpty());
        }

        @Test
        @DisplayName("inline IF needs no END (RF 7.0)")
        void inlineIf() {
            String source = "*** Test Cases ***\nT\n    IF    ${c}    Log    yes    ELSE    Log    no\n";
            assertTrue(findings(source, RobotFinding.Category.UNCLOSED_BLOCK).isEmpty());
        }

        @Test
        @DisplayName("multi-line IF still needs END")
        void multiLineIf() {
            String source = "*** Test Cases ***\nT\n    IF    ${c}\n        Log    yes\n";
            assertEquals(1, findings(source, RobotFinding.Category.UNCLOSED_BLOCK).size());
        }

        @Test
        @DisplayName("an END without an opener is reported")
        void orphanEnd() {
            String source = "*** Test Cases ***\nT\n    Log    hi\n    END\n";
            assertEquals(1, findings(source, RobotFinding.Category.ORPHAN_END).size());
        }

        @Test
        @DisplayName("a block left open by the next test is reported")
        void blockLeaksIntoNextTest() {
            String source = "*** Test Cases ***\nFirst\n    WHILE    ${c}\n        Log    hi\n"
                    + "Second\n    Log    hi\n";
            assertEquals(1, findings(source, RobotFinding.Category.UNCLOSED_BLOCK).size());
        }

        @Test
        @DisplayName("continuation rows do not affect the balance")
        void continuationRows() {
            String source = "*** Test Cases ***\nT\n    IF    ${c}\n"
                    + "        Log    a\n    ...    b\n    END\n";
            assertTrue(findings(source, RobotFinding.Category.UNCLOSED_BLOCK).isEmpty());
        }
    }

    @Nested
    @DisplayName("Variable type conversion")
    class TypeTests {
        @Test
        @DisplayName("valid types are silent")
        void validTypes() {
            String source = "*** Variables ***\n${A: int}    1\n@{B: list[int]}    [1]\n"
                    + "&{C: dict[str, int]}    a=1\n\n*** Test Cases ***\nT\n    Log    ${A}\n";
            assertTrue(findings(source, RobotFinding.Category.INVALID_VARIABLE_TYPE).isEmpty());
        }

        @Test
        @DisplayName("an unbalanced bracket is reported on the type only")
        void unbalanced() {
            String source = "*** Variables ***\n${A: list[int}    1\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.INVALID_VARIABLE_TYPE);
            assertEquals(1, found.size());
            assertEquals(" list[int", text(source, found.get(0)));
        }

        @Test
        @DisplayName("a dangling union operator is reported")
        void danglingUnion() {
            String source = "*** Keywords ***\nK\n    [Arguments]    ${n: int |}\n    Log    ${n}\n";
            assertEquals(1, findings(source, RobotFinding.Category.INVALID_VARIABLE_TYPE).size());
        }

        @Test
        @DisplayName("custom type names are accepted")
        void customType() {
            String source = "*** Keywords ***\nK\n    [Arguments]    ${d: MyDirection}\n    Log    ${d}\n";
            assertTrue(findings(source, RobotFinding.Category.INVALID_VARIABLE_TYPE).isEmpty());
        }

        @Test
        @DisplayName("types written inside a comment are ignored")
        void insideComment() {
            String source = "*** Variables ***\n# example: ${A: list[int}\n${A}    1\n"
                    + "\n*** Test Cases ***\nT\n    Log    ${A}\n";
            assertTrue(findings(source, RobotFinding.Category.INVALID_VARIABLE_TYPE).isEmpty());
        }

        @Test
        @DisplayName("a typed declaration still counts as used")
        void typedVariableUsage() {
            String source = "*** Variables ***\n${RETRIES: int}    3\n"
                    + "\n*** Test Cases ***\nT\n    Log    ${RETRIES}\n";
            assertTrue(findings(source, RobotFinding.Category.UNUSED_VARIABLE).isEmpty());
        }
    }

    @Nested
    @DisplayName("Secret variables")
    class SecretTests {
        @Test
        @DisplayName("a literal secret is reported")
        void literalSecret() {
            String source = "*** Variables ***\n${PW: Secret}    robot123\n"
                    + "\n*** Test Cases ***\nT\n    Log    ${PW}\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.SECRET_LITERAL_VALUE);
            assertEquals(1, found.size());
            assertEquals("robot123", text(source, found.get(0)));
        }

        @Test
        @DisplayName("environment and variable values are accepted")
        void indirectSecret() {
            String source = "*** Variables ***\n${A: Secret}    %{APP_PW}\n"
                    + "${B: Secret}    %{APP_PW=fallback}\n"
                    + "${C: Secret}    ${A}-suffix\n"
                    + "&{D: Secret}    key=${A}\n"
                    + "\n*** Test Cases ***\nT\n    Log    ${A}${B}${C}${D}\n";
            assertTrue(findings(source, RobotFinding.Category.SECRET_LITERAL_VALUE).isEmpty());
        }

        @Test
        @DisplayName("untyped variables may hold literals")
        void untypedVariable() {
            String source = "*** Variables ***\n${PW}    robot123\n"
                    + "\n*** Test Cases ***\nT\n    Log    ${PW}\n";
            assertTrue(findings(source, RobotFinding.Category.SECRET_LITERAL_VALUE).isEmpty());
        }
    }

    @Nested
    @DisplayName("Structure")
    class StructureTests {
        @Test
        @DisplayName("data before the first header is reported once")
        void contentOutsideSection() {
            String source = "Library    SeleniumLibrary\nResource    x.resource\n"
                    + "*** Settings ***\nLibrary    OperatingSystem\n";
            assertEquals(1, findings(source, RobotFinding.Category.CONTENT_OUTSIDE_SECTION).size());
        }

        @Test
        @DisplayName("leading comments and blank lines are fine")
        void leadingComments() {
            String source = "# suite header\n\n*** Settings ***\nLibrary    OperatingSystem\n";
            assertTrue(findings(source, RobotFinding.Category.CONTENT_OUTSIDE_SECTION).isEmpty());
        }

        @Test
        @DisplayName("a test without steps is reported")
        void emptyTest() {
            String source = "*** Test Cases ***\nEmpty Test\n    [Documentation]    nothing here\n"
                    + "\nReal Test\n    Log    hi\n";
            List<RobotFinding> found = findings(source, RobotFinding.Category.EMPTY_TEST_CASE);
            assertEquals(1, found.size());
            assertEquals("Empty Test", text(source, found.get(0)));
        }

        @Test
        @DisplayName("templated data rows count as steps")
        void templatedTest() {
            String source = "*** Test Cases ***\nT\n    [Template]    Check\n    1    2\n";
            assertTrue(findings(source, RobotFinding.Category.EMPTY_TEST_CASE).isEmpty());
        }

        @Test
        @DisplayName("an over-long test name is reported")
        void longName() {
            String name = "T".repeat(RobotCodeAnalyzer.MAX_TEST_NAME_LENGTH + 1);
            String source = "*** Test Cases ***\n" + name + "\n    Log    hi\n";
            assertEquals(1, findings(source, RobotFinding.Category.LONG_TEST_NAME).size());
        }

        @Test
        @DisplayName("a name at the limit is accepted")
        void nameAtLimit() {
            String name = "T".repeat(RobotCodeAnalyzer.MAX_TEST_NAME_LENGTH);
            String source = "*** Test Cases ***\n" + name + "\n    Log    hi\n";
            assertTrue(findings(source, RobotFinding.Category.LONG_TEST_NAME).isEmpty());
        }

        @Test
        @DisplayName("Tasks sections are analyzed like Test Cases")
        void tasksSection() {
            String source = "*** Tasks ***\nEmpty Task\n";
            assertEquals(1, findings(source, RobotFinding.Category.EMPTY_TEST_CASE).size());
        }

        @Test
        @DisplayName("Comments sections are ignored data")
        void commentsSection() {
            String source = "*** Comments ***\nIF    broken\n${A: list[int}\n"
                    + "\n*** Test Cases ***\nT\n    Log    hi\n";
            assertTrue(findings(source, RobotFinding.Category.UNCLOSED_BLOCK).isEmpty());
            assertTrue(findings(source, RobotFinding.Category.INVALID_VARIABLE_TYPE).isEmpty());
        }
    }

    @Nested
    @DisplayName("Clean modern suite")
    class CleanSuiteTests {
        private static final String SUITE = ""
                + "*** Settings ***\n"
                + "Library      OperatingSystem    AS    OS\n"
                + "Test Tags    smoke\n"
                + "\n"
                + "*** Variables ***\n"
                + "${RETRIES: int}        3\n"
                + "${PASSWORD: Secret}    %{APP_PASSWORD}\n"
                + "\n"
                + "*** Test Cases ***\n"
                + "Sign In\n"
                + "    [Tags]    -smoke\n"
                + "    GROUP    Prepare\n"
                + "        VAR    ${attempt: int}    0    scope=TEST\n"
                + "    END\n"
                + "    WHILE    ${attempt} < ${RETRIES}    limit=10x\n"
                + "        VAR    ${attempt}    ${{ $attempt + 1 }}\n"
                + "    END\n"
                + "    Log    ${PASSWORD}\n"
                + "\n"
                + "*** Keywords ***\n"
                + "Move ${distance: int} Meters\n"
                + "    [Arguments]    ${speed: float}=1.0\n"
                + "    [Setup]    Log    starting\n"
                + "    RETURN    ${distance}\n";

        @Test
        @DisplayName("a fully modern suite produces no syntax findings")
        void noFindings() {
            List<RobotFinding.Category> reported = RobotCodeAnalyzer.analyze(SUITE).stream()
                    .map(RobotFinding::getCategory)
                    .distinct()
                    .collect(Collectors.toList());
            assertEquals(List.of(), reported, "unexpected findings: " + reported);
        }

        @Test
        @DisplayName("the suite stays clean after formatting")
        void stableAfterFormatting() {
            String formatted = new RobotFormatter().format(SUITE);
            assertFalse(formatted.isEmpty());
            assertTrue(RobotCodeAnalyzer.analyze(formatted).isEmpty());
        }
    }
}
