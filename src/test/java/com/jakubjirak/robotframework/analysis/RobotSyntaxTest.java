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

@DisplayName("RobotSyntax")
class RobotSyntaxTest {

    private List<String> cells(String line) {
        return RobotSyntax.splitCells(line).stream()
                .map(RobotSyntax.Cell::getText)
                .collect(Collectors.toList());
    }

    @Nested
    @DisplayName("Control structure markers")
    class MarkerTests {
        @Test
        @DisplayName("GROUP opens a block (RF 7.2)")
        void groupIsBlockOpener() {
            assertTrue(RobotSyntax.isBlockOpener("GROUP"));
            assertTrue(RobotSyntax.isMarker("GROUP"));
        }

        @Test
        @DisplayName("all block openers are recognized")
        void blockOpeners() {
            for (String marker : List.of("IF", "FOR", "WHILE", "TRY", "GROUP")) {
                assertTrue(RobotSyntax.isBlockOpener(marker), marker);
            }
        }

        @Test
        @DisplayName("ELSE, ELSE IF, EXCEPT and FINALLY continue a block")
        void continuations() {
            for (String marker : List.of("ELSE", "ELSE IF", "EXCEPT", "FINALLY")) {
                assertTrue(RobotSyntax.isBlockContinuation(marker), marker);
                assertFalse(RobotSyntax.isBlockOpener(marker), marker);
            }
        }

        @Test
        @DisplayName("markers are case insensitive and whitespace tolerant")
        void normalization() {
            assertEquals("ELSE IF", RobotSyntax.normalizeMarker("else   if"));
            assertTrue(RobotSyntax.isMarker("else if"));
            assertTrue(RobotSyntax.isForSeparator("in zip"));
        }

        @Test
        @DisplayName("END is the block terminator")
        void end() {
            assertTrue(RobotSyntax.isBlockEnd("end"));
            assertFalse(RobotSyntax.isBlockEnd("ENDS"));
        }

        @Test
        @DisplayName("SUITES scope is available (RF 7.1)")
        void suitesScope() {
            assertTrue(RobotSyntax.VAR_SCOPES.contains("SUITES"));
        }
    }

    @Nested
    @DisplayName("Deprecations")
    class DeprecationTests {
        @Test
        @DisplayName("[Return] maps to RETURN")
        void returnSetting() {
            RobotSyntax.Deprecation deprecation = RobotSyntax.deprecatedSetting("[Return]");
            assertNotNull(deprecation);
            assertEquals("RETURN", deprecation.getReplacement());
            assertEquals("7.0", deprecation.getSince());
        }

        @Test
        @DisplayName("Force Tags maps to Test Tags and is case insensitive")
        void forceTags() {
            RobotSyntax.Deprecation deprecation = RobotSyntax.deprecatedSetting("force tags");
            assertNotNull(deprecation);
            assertEquals("Test Tags", deprecation.getReplacement());
        }

        @Test
        @DisplayName("Default Tags has no mechanical replacement")
        void defaultTags() {
            RobotSyntax.Deprecation deprecation = RobotSyntax.deprecatedSetting("Default Tags");
            assertNotNull(deprecation);
            assertNull(deprecation.getReplacement());
        }

        @Test
        @DisplayName("current settings are not reported")
        void currentSettings() {
            assertNull(RobotSyntax.deprecatedSetting("Test Tags"));
            assertNull(RobotSyntax.deprecatedSetting("[Documentation]"));
        }

        @Test
        @DisplayName("loop keywords map to control structures")
        void loopKeywords() {
            assertEquals("BREAK", RobotSyntax.deprecatedKeyword("Exit For Loop").getReplacement());
            assertEquals("CONTINUE", RobotSyntax.deprecatedKeyword("Continue For Loop").getReplacement());
            assertEquals("RETURN", RobotSyntax.deprecatedKeyword("Return From Keyword").getReplacement());
        }

        @Test
        @DisplayName("keyword names are matched the Robot Framework way")
        void keywordNormalization() {
            assertNotNull(RobotSyntax.deprecatedKeyword("exit_for_loop"));
            assertNotNull(RobotSyntax.deprecatedKeyword("EXITFORLOOP"));
        }

        @Test
        @DisplayName("conditional variants have no mechanical replacement")
        void conditionalVariants() {
            assertNull(RobotSyntax.deprecatedKeyword("Exit For Loop If").getReplacement());
            assertNull(RobotSyntax.deprecatedKeyword("Run Keyword Unless").getReplacement());
        }

        @Test
        @DisplayName("Run Keyword If is still current syntax")
        void runKeywordIfIsNotDeprecated() {
            assertNull(RobotSyntax.deprecatedKeyword("Run Keyword If"));
        }
    }

    @Nested
    @DisplayName("Variable parsing")
    class VariableTests {
        @Test
        @DisplayName("plain variable has no type")
        void untyped() {
            RobotSyntax.TypedVariable variable = RobotSyntax.parseVariable("${NAME}");
            assertNotNull(variable);
            assertEquals("NAME", variable.getName());
            assertFalse(variable.isTyped());
        }

        @Test
        @DisplayName("typed variable splits name and type (RF 7.3)")
        void typed() {
            RobotSyntax.TypedVariable variable = RobotSyntax.parseVariable("${amount: int}");
            assertNotNull(variable);
            assertEquals("amount", variable.getName());
            assertEquals("int", variable.getType());
            assertEquals("int", "${amount: int}".substring(variable.getTypeStart(), variable.getTypeEnd()).trim());
        }

        @Test
        @DisplayName("generic and union types are kept whole")
        void genericTypes() {
            assertEquals("list[int]", RobotSyntax.parseVariable("${ids: list[int]}").getType());
            assertEquals("int | float", RobotSyntax.parseVariable("${n: int | float}").getType());
            assertEquals("dict[str, int]", RobotSyntax.parseVariable("&{m: dict[str, int]}").getType());
        }

        @Test
        @DisplayName("Literal types with quoted values are kept whole")
        void literalType() {
            RobotSyntax.TypedVariable variable =
                    RobotSyntax.parseVariable("${dir: Literal[\"UP\", \"DOWN\"]}");
            assertNotNull(variable);
            assertEquals("dir", variable.getName());
            assertEquals("Literal[\"UP\", \"DOWN\"]", variable.getType());
        }

        @Test
        @DisplayName("a colon without a following space is part of the name")
        void colonWithoutSpace() {
            RobotSyntax.TypedVariable variable = RobotSyntax.parseVariable("${:}");
            assertNotNull(variable);
            assertFalse(variable.isTyped());
            assertEquals(":", variable.getName());
        }

        @Test
        @DisplayName("a colon inside a nested variable is not a type marker")
        void nestedVariable() {
            RobotSyntax.TypedVariable variable = RobotSyntax.parseVariable("${prefix${x: int}}");
            assertNotNull(variable);
            assertFalse(variable.isTyped());
        }

        @Test
        @DisplayName("list and dict decorations are supported")
        void decorations() {
            assertEquals("int", RobotSyntax.parseVariable("@{HIGH: int}").getType());
            assertEquals("Secret", RobotSyntax.parseVariable("&{DICT: Secret}").getType());
            assertEquals("Secret", RobotSyntax.parseVariable("${PW: Secret}").getType());
        }

        @Test
        @DisplayName("non-variables are rejected")
        void notAVariable() {
            assertNull(RobotSyntax.parseVariable("plain text"));
            assertNull(RobotSyntax.parseVariable("$notvar"));
            assertNull(RobotSyntax.parseVariable("${unclosed"));
        }
    }

    @Nested
    @DisplayName("Type expression validation")
    class TypeValidationTests {
        @Test
        @DisplayName("well-formed types pass")
        void valid() {
            for (String type : List.of("int", "list[int]", "int | float", "dict[str, int]",
                    "Literal[\"UP\", \"DOWN\"]", "MyCustomEnum", "Secret")) {
                assertNull(RobotSyntax.validateTypeExpression(type), type);
            }
        }

        @Test
        @DisplayName("unbalanced brackets are rejected")
        void unbalanced() {
            assertNotNull(RobotSyntax.validateTypeExpression("list[int"));
            assertNotNull(RobotSyntax.validateTypeExpression("list int]"));
        }

        @Test
        @DisplayName("empty and dangling types are rejected")
        void emptyOrDangling() {
            assertNotNull(RobotSyntax.validateTypeExpression(""));
            assertNotNull(RobotSyntax.validateTypeExpression("   "));
            assertNotNull(RobotSyntax.validateTypeExpression("int |"));
            assertNotNull(RobotSyntax.validateTypeExpression("| int"));
            assertNotNull(RobotSyntax.validateTypeExpression("int || float"));
            assertNotNull(RobotSyntax.validateTypeExpression("[int]"));
        }

        @Test
        @DisplayName("unterminated string literal is rejected")
        void unterminatedString() {
            assertNotNull(RobotSyntax.validateTypeExpression("Literal[\"UP]"));
        }

        @Test
        @DisplayName("brackets inside a string literal do not count")
        void bracketsInString() {
            assertNull(RobotSyntax.validateTypeExpression("Literal[\"a]b\"]"));
        }
    }

    @Nested
    @DisplayName("Cell splitting")
    class CellTests {
        @Test
        @DisplayName("two spaces separate cells, one space does not")
        void separators() {
            assertEquals(List.of("Log To Console", "hello world"),
                    cells("    Log To Console    hello world"));
        }

        @Test
        @DisplayName("tabs separate cells")
        void tabs() {
            assertEquals(List.of("Log", "hi"), cells("\tLog\thi"));
        }

        @Test
        @DisplayName("runs of three or more spaces collapse into one separator")
        void longSeparator() {
            assertEquals(List.of("VAR", "${x}", "1"), cells("    VAR      ${x}       1"));
        }

        @Test
        @DisplayName("trailing comments are dropped")
        void comments() {
            assertEquals(List.of("Log", "hi"), cells("    Log    hi    # explain"));
            assertEquals(List.of(), cells("    # only a comment"));
        }

        @Test
        @DisplayName("an escaped hash stays data")
        void escapedHash() {
            assertEquals(List.of("Log", "\\#tag"), cells("    Log    \\#tag"));
        }

        @Test
        @DisplayName("cell offsets point at the cell inside the line")
        void offsets() {
            List<RobotSyntax.Cell> parsed = RobotSyntax.splitCells("    Log    hi");
            assertEquals(4, parsed.get(0).getStart());
            assertEquals(7, parsed.get(0).getEnd());
            assertEquals(11, parsed.get(1).getStart());
        }

        @Test
        @DisplayName("empty and blank lines produce no cells")
        void empty() {
            assertEquals(List.of(), cells(""));
            assertEquals(List.of(), cells("      "));
        }
    }
}
