/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.language;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for {@link RobotLexer}.
 * Tests the hand-written lexer's tokenization of Robot Framework syntax.
 */
@DisplayName("RobotLexer")
class RobotLexerTest {

    private RobotLexer lexer;

    @BeforeEach
    void setUp() {
        lexer = new RobotLexer();
    }

    /**
     * Collects all token types produced by lexing the given input.
     */
    private List<IElementType> tokenize(String input) {
        List<IElementType> tokens = new ArrayList<>();
        lexer.start(input, 0, input.length(), 0);
        while (lexer.getTokenType() != null) {
            tokens.add(lexer.getTokenType());
            lexer.advance();
        }
        return tokens;
    }

    /**
     * Collects all token texts produced by lexing the given input.
     */
    private List<String> tokenTexts(String input) {
        List<String> texts = new ArrayList<>();
        lexer.start(input, 0, input.length(), 0);
        while (lexer.getTokenType() != null) {
            texts.add(input.substring(lexer.getTokenStart(), lexer.getTokenEnd()));
            lexer.advance();
        }
        return texts;
    }

    @Test
    @DisplayName("empty input produces no tokens")
    void emptyInput() {
        lexer.start("", 0, 0, 0);
        assertNull(lexer.getTokenType());
    }

    @Test
    @DisplayName("getState always returns 0")
    void stateAlwaysZero() {
        lexer.start("hello", 0, 5, 0);
        assertEquals(0, lexer.getState());
    }

    @Test
    @DisplayName("getBufferSequence returns the buffer")
    void bufferSequence() {
        String input = "test content";
        lexer.start(input, 0, input.length(), 0);
        assertEquals(input, lexer.getBufferSequence().toString());
    }

    @Test
    @DisplayName("getBufferEnd returns the end offset")
    void bufferEnd() {
        String input = "test content";
        lexer.start(input, 0, input.length(), 0);
        assertEquals(input.length(), lexer.getBufferEnd());
    }

    @Nested
    @DisplayName("Whitespace Tokenization")
    class WhitespaceTests {
        @Test
        @DisplayName("spaces are tokenized as WHITE_SPACE")
        void spaces() {
            List<IElementType> tokens = tokenize("   ");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(0));
        }

        @Test
        @DisplayName("tabs are tokenized as WHITE_SPACE")
        void tabs() {
            List<IElementType> tokens = tokenize("\t\t");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(0));
        }

        @Test
        @DisplayName("newlines are tokenized as WHITE_SPACE")
        void newlines() {
            List<IElementType> tokens = tokenize("\n\n");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(0));
        }

        @Test
        @DisplayName("mixed whitespace is a single token")
        void mixedWhitespace() {
            List<IElementType> tokens = tokenize(" \t \n ");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(0));
        }
    }

    @Nested
    @DisplayName("Comment Tokenization")
    class CommentTests {
        @Test
        @DisplayName("line comment is tokenized")
        void lineComment() {
            List<IElementType> tokens = tokenize("# this is a comment");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.COMMENT, tokens.get(0));
        }

        @Test
        @DisplayName("comment stops at newline")
        void commentStopsAtNewline() {
            List<IElementType> tokens = tokenize("# comment\nidentifier");
            assertEquals(3, tokens.size());
            assertEquals(RobotTokenTypes.COMMENT, tokens.get(0));
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(1));
            assertEquals(RobotTokenTypes.IDENTIFIER, tokens.get(2));
        }

        @Test
        @DisplayName("empty comment (just hash)")
        void emptyComment() {
            List<IElementType> tokens = tokenize("#");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.COMMENT, tokens.get(0));
        }
    }

    @Nested
    @DisplayName("Variable Tokenization")
    class VariableTests {
        @Test
        @DisplayName("scalar variable ${var}")
        void scalarVariable() {
            List<IElementType> tokens = tokenize("${var}");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(0));
        }

        @Test
        @DisplayName("list variable @{list}")
        void listVariable() {
            List<IElementType> tokens = tokenize("@{list}");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(0));
        }

        @Test
        @DisplayName("dictionary variable &{dict}")
        void dictVariable() {
            List<IElementType> tokens = tokenize("&{dict}");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(0));
        }

        @Test
        @DisplayName("environment variable %{env}")
        void envVariable() {
            List<IElementType> tokens = tokenize("%{env}");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(0));
        }

        @Test
        @DisplayName("nested variable ${var${nested}}")
        void nestedVariable() {
            List<IElementType> tokens = tokenize("${var${nested}}");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(0));
        }

        @Test
        @DisplayName("$ without brace is not a variable")
        void dollarWithoutBrace() {
            List<IElementType> tokens = tokenize("$abc");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.IDENTIFIER, tokens.get(0));
        }

        @Test
        @DisplayName("variable followed by text produces two tokens")
        void variableFollowedByText() {
            List<IElementType> tokens = tokenize("${var} text");
            assertEquals(3, tokens.size());
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(0));
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(1));
            assertEquals(RobotTokenTypes.IDENTIFIER, tokens.get(2));
        }
    }

    @Nested
    @DisplayName("Section Header Tokenization")
    class SectionHeaderTests {
        @Test
        @DisplayName("single star produces SECTION_HEADER token")
        void singleStar() {
            List<IElementType> tokens = tokenize("*");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.SECTION_HEADER, tokens.get(0));
        }

        @Test
        @DisplayName("bare *** is one unnamed SECTION_HEADER token")
        void tripleStars() {
            List<IElementType> tokens = tokenize("***");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.SECTION_HEADER, tokens.get(0));
        }

        @Test
        @DisplayName("*** Settings *** is a single SETTINGS_HEADER token")
        void settingsHeader() {
            List<IElementType> tokens = tokenize("*** Settings ***");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.SETTINGS_HEADER, tokens.get(0));
            assertEquals(List.of("*** Settings ***"), tokenTexts("*** Settings ***"));
        }

        @Test
        @DisplayName("*** Variables *** is a single VARIABLES_HEADER token")
        void variablesHeader() {
            assertEquals(List.of(RobotTokenTypes.VARIABLES_HEADER), tokenize("*** Variables ***"));
        }

        @Test
        @DisplayName("*** Test Cases *** is a single TEST_CASES_HEADER token")
        void testCasesHeader() {
            assertEquals(List.of(RobotTokenTypes.TEST_CASES_HEADER), tokenize("*** Test Cases ***"));
        }

        @Test
        @DisplayName("*** Tasks *** reuses TEST_CASES_HEADER")
        void tasksHeader() {
            assertEquals(List.of(RobotTokenTypes.TEST_CASES_HEADER), tokenize("*** Tasks ***"));
        }

        @Test
        @DisplayName("*** Keywords *** is a single KEYWORDS_HEADER token")
        void keywordsHeader() {
            assertEquals(List.of(RobotTokenTypes.KEYWORDS_HEADER), tokenize("*** Keywords ***"));
        }

        @Test
        @DisplayName("*** Comments *** is a single COMMENTS_HEADER token")
        void commentsHeader() {
            assertEquals(List.of(RobotTokenTypes.COMMENTS_HEADER), tokenize("*** Comments ***"));
        }

        @Test
        @DisplayName("singular headers are recognized too")
        void singularHeader() {
            assertEquals(List.of(RobotTokenTypes.TEST_CASES_HEADER), tokenize("*** Test Case ***"));
        }

        @Test
        @DisplayName("header spelling is case insensitive")
        void caseInsensitiveHeader() {
            assertEquals(List.of(RobotTokenTypes.KEYWORDS_HEADER), tokenize("*** keywords ***"));
        }

        @Test
        @DisplayName("unknown header name falls back to SECTION_HEADER")
        void unknownHeader() {
            assertEquals(List.of(RobotTokenTypes.SECTION_HEADER), tokenize("*** Whatever ***"));
        }

        @Test
        @DisplayName("an asterisk that is not at line start is data")
        void asteriskInsideLine() {
            List<IElementType> tokens = tokenize("Log    a*b");
            assertFalse(tokens.contains(RobotTokenTypes.SECTION_HEADER));
        }

        @Test
        @DisplayName("section header word alone is an IDENTIFIER")
        void sectionWordIsIdentifier() {
            List<IElementType> tokens = tokenize("Settings");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.IDENTIFIER, tokens.get(0));
        }
    }

    @Nested
    @DisplayName("Robot Framework 7.x Syntax")
    class ModernSyntaxTests {
        @Test
        @DisplayName("GROUP is a control structure marker (RF 7.2)")
        void groupMarker() {
            assertEquals(List.of(RobotTokenTypes.GROUP_KEYWORD), tokenize("GROUP"));
        }

        @Test
        @DisplayName("ELSE IF is one token")
        void elseIfIsOneToken() {
            assertEquals(List.of("ELSE IF"), tokenTexts("ELSE IF"));
            assertEquals(List.of(RobotTokenTypes.ELSE_IF_KEYWORD), tokenize("ELSE IF"));
        }

        @Test
        @DisplayName("IN ENUMERATE and IN ZIP are single tokens")
        void multiWordForSeparators() {
            assertEquals(List.of(RobotTokenTypes.IN_ENUMERATE_KEYWORD), tokenize("IN ENUMERATE"));
            assertEquals(List.of(RobotTokenTypes.IN_ZIP_KEYWORD), tokenize("IN ZIP"));
            assertEquals(List.of(RobotTokenTypes.IN_RANGE_KEYWORD), tokenize("IN RANGE"));
        }

        @Test
        @DisplayName("IN followed by an unrelated word stays a single IN token")
        void inNotExtended() {
            List<String> texts = tokenTexts("IN @{items}");
            assertEquals("IN", texts.get(0));
        }

        @Test
        @DisplayName("typed variable is a single VARIABLE token (RF 7.3)")
        void typedVariable() {
            assertEquals(List.of("${amount: list[int]}"), tokenTexts("${amount: list[int]}"));
            assertEquals(List.of(RobotTokenTypes.VARIABLE), tokenize("${amount: list[int]}"));
        }

        @Test
        @DisplayName("Comments section content is lexed as comment")
        void commentsSectionIsComment() {
            String input = "*** Comments ***\nthis is ignored data\n*** Settings ***\nLibrary    X";
            List<IElementType> tokens = tokenize(input);
            assertEquals(RobotTokenTypes.COMMENTS_HEADER, tokens.get(0));
            assertEquals(RobotTokenTypes.COMMENT, tokens.get(2));
            assertTrue(tokens.contains(RobotTokenTypes.SETTINGS_HEADER),
                "the Settings header must end the comments section");
        }

        @Test
        @DisplayName("comments-section state survives a restart at the same offset")
        void commentsSectionState() {
            String input = "*** Comments ***\nignored";
            lexer.start(input, 0, input.length(), 0);
            while (lexer.getTokenType() != null
                    && lexer.getTokenType() != RobotTokenTypes.WHITE_SPACE) {
                lexer.advance();
            }
            int resumeOffset = lexer.getTokenEnd();
            int resumeState = lexer.getState();

            RobotLexer resumed = new RobotLexer();
            resumed.start(input, resumeOffset, input.length(), resumeState);
            assertEquals(RobotTokenTypes.COMMENT, resumed.getTokenType(),
                "resuming inside a Comments section must keep lexing comments");
        }
    }

    @Nested
    @DisplayName("Token Ranges")
    class TokenRangeTests {
        @Test
        @DisplayName("tokens carry their own start and end offsets")
        void tokenOffsets() {
            String input = "Log    ${x}";
            assertEquals(List.of("Log", "    ", "${x}"), tokenTexts(input));
        }

        @Test
        @DisplayName("token ranges are contiguous and cover the whole buffer")
        void contiguousRanges() {
            String input = "*** Test Cases ***\nName\n    Log    hi\n";
            lexer.start(input, 0, input.length(), 0);
            int expectedStart = 0;
            while (lexer.getTokenType() != null) {
                assertEquals(expectedStart, lexer.getTokenStart());
                assertTrue(lexer.getTokenEnd() > lexer.getTokenStart(), "tokens must not be empty");
                expectedStart = lexer.getTokenEnd();
                lexer.advance();
            }
            assertEquals(input.length(), expectedStart);
        }
    }

    @Nested
    @DisplayName("String Literal Tokenization")
    class StringTests {
        @Test
        @DisplayName("double-quoted string")
        void doubleQuoted() {
            List<IElementType> tokens = tokenize("\"hello world\"");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.STRING, tokens.get(0));
        }

        @Test
        @DisplayName("single-quoted string")
        void singleQuoted() {
            List<IElementType> tokens = tokenize("'hello world'");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.STRING, tokens.get(0));
        }

        @Test
        @DisplayName("string with escaped quote")
        void escapedQuote() {
            List<IElementType> tokens = tokenize("\"hello \\\"world\\\"\"");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.STRING, tokens.get(0));
        }

        @Test
        @DisplayName("empty double-quoted string")
        void emptyDoubleQuoted() {
            List<IElementType> tokens = tokenize("\"\"");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.STRING, tokens.get(0));
        }

        @Test
        @DisplayName("empty single-quoted string")
        void emptySingleQuoted() {
            List<IElementType> tokens = tokenize("''");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.STRING, tokens.get(0));
        }
    }

    @Nested
    @DisplayName("Control Flow Keyword Tokenization")
    class ControlFlowTests {
        @Test
        @DisplayName("IF keyword")
        void ifKeyword() {
            List<IElementType> tokens = tokenize("IF");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.IF_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("ELSE keyword")
        void elseKeyword() {
            List<IElementType> tokens = tokenize("ELSE");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.ELSE_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("TRY keyword")
        void tryKeyword() {
            List<IElementType> tokens = tokenize("TRY");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.TRY_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("EXCEPT keyword")
        void exceptKeyword() {
            List<IElementType> tokens = tokenize("EXCEPT");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.EXCEPT_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("FINALLY keyword")
        void finallyKeyword() {
            List<IElementType> tokens = tokenize("FINALLY");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.FINALLY_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("WHILE keyword")
        void whileKeyword() {
            List<IElementType> tokens = tokenize("WHILE");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.WHILE_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("FOR keyword")
        void forKeyword() {
            List<IElementType> tokens = tokenize("FOR");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.FOR_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("END keyword")
        void endKeyword() {
            List<IElementType> tokens = tokenize("END");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.END_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("BREAK keyword")
        void breakKeyword() {
            List<IElementType> tokens = tokenize("BREAK");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.BREAK_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("CONTINUE keyword")
        void continueKeyword() {
            List<IElementType> tokens = tokenize("CONTINUE");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.CONTINUE_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("RETURN keyword")
        void returnKeyword() {
            List<IElementType> tokens = tokenize("RETURN");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.RETURN_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("VAR keyword")
        void varKeyword() {
            List<IElementType> tokens = tokenize("VAR");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.VAR_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("IN keyword")
        void inKeyword() {
            List<IElementType> tokens = tokenize("IN");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.IN_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("control flow keywords are case-insensitive")
        void caseInsensitive() {
            List<IElementType> tokens = tokenize("if");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.IF_KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("lowercase for keyword")
        void lowercaseFor() {
            List<IElementType> tokens = tokenize("for");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.FOR_KEYWORD, tokens.get(0));
        }
    }

    @Nested
    @DisplayName("Built-in Keyword Tokenization")
    class BuiltInKeywordTests {
        @Test
        @DisplayName("Log is recognized as KEYWORD")
        void logKeyword() {
            List<IElementType> tokens = tokenize("Log");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("log (lowercase) is recognized as KEYWORD")
        void logLowercase() {
            List<IElementType> tokens = tokenize("log");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("Sleep is recognized as KEYWORD")
        void sleepKeyword() {
            List<IElementType> tokens = tokenize("Sleep");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("Run is recognized as KEYWORD")
        void runKeyword() {
            List<IElementType> tokens = tokenize("Run");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.KEYWORD, tokens.get(0));
        }

        @Test
        @DisplayName("unknown word is IDENTIFIER not KEYWORD")
        void unknownWord() {
            List<IElementType> tokens = tokenize("MyCustomKeyword");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.IDENTIFIER, tokens.get(0));
        }
    }

    @Nested
    @DisplayName("Setting Tokenization")
    class SettingTests {
        @Test
        @DisplayName("[Tags] is tokenized as SETTING")
        void tagsDirective() {
            List<IElementType> tokens = tokenize("[Tags]");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.SETTING, tokens.get(0));
        }

        @Test
        @DisplayName("[Documentation] is tokenized as SETTING")
        void documentationDirective() {
            List<IElementType> tokens = tokenize("[Documentation]");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.SETTING, tokens.get(0));
        }

        @Test
        @DisplayName("[Setup] is tokenized as SETTING")
        void setupDirective() {
            List<IElementType> tokens = tokenize("[Setup]");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.SETTING, tokens.get(0));
        }

        @Test
        @DisplayName("[Teardown] is tokenized as SETTING")
        void teardownDirective() {
            List<IElementType> tokens = tokenize("[Teardown]");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.SETTING, tokens.get(0));
        }
    }

    @Nested
    @DisplayName("Tag Context Tokenization")
    class TagContextTests {
        @Test
        @DisplayName("tokens after [Tags] become TAG type")
        void tagContext() {
            List<IElementType> tokens = tokenize("[Tags] smoke");
            assertEquals(3, tokens.size());
            assertEquals(RobotTokenTypes.SETTING, tokens.get(0));
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(1));
            assertEquals(RobotTokenTypes.TAG, tokens.get(2));
        }

        @Test
        @DisplayName("tag context is reset by non-tag settings")
        void tagContextReset() {
            List<IElementType> tokens = tokenize("[Tags] smoke\n[Documentation] text");
            // [Tags] → SETTING, space → WS, smoke → TAG, \n → WS, [Documentation] → SETTING, space → WS, text → IDENTIFIER
            assertTrue(tokens.contains(RobotTokenTypes.TAG));
            // After [Documentation], context should reset
            int docIndex = -1;
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i) == RobotTokenTypes.SETTING && i > 0) {
                    docIndex = i;
                    break;
                }
            }
            assertTrue(docIndex > 0, "[Documentation] should appear after [Tags]");
            // Token after [Documentation] whitespace should NOT be TAG
            if (docIndex + 2 < tokens.size()) {
                assertNotEquals(RobotTokenTypes.TAG, tokens.get(docIndex + 2));
            }
        }

        @Test
        @DisplayName("multiple tags after [Tags]")
        void multipleTags() {
            List<IElementType> tokens = tokenize("[Tags] smoke regression");
            // [Tags] SETTING, WS, smoke TAG, WS, regression TAG
            long tagCount = tokens.stream().filter(t -> t == RobotTokenTypes.TAG).count();
            assertEquals(2, tagCount);
        }
    }

    @Nested
    @DisplayName("Complex Input Tokenization")
    class ComplexTests {
        @Test
        @DisplayName("variable in comment is still a comment")
        void variableInComment() {
            List<IElementType> tokens = tokenize("# ${var} is a comment");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.COMMENT, tokens.get(0));
        }

        @Test
        @DisplayName("multiple token types in sequence")
        void multipleTokens() {
            List<IElementType> tokens = tokenize("Log ${message}");
            assertEquals(3, tokens.size());
            assertEquals(RobotTokenTypes.KEYWORD, tokens.get(0));
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(1));
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(2));
        }

        @Test
        @DisplayName("string followed by variable")
        void stringThenVariable() {
            List<IElementType> tokens = tokenize("\"hello\" ${var}");
            assertEquals(3, tokens.size());
            assertEquals(RobotTokenTypes.STRING, tokens.get(0));
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(1));
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(2));
        }

        @Test
        @DisplayName("lexer handles partial buffer with offsets")
        void partialBuffer() {
            String input = "xxxhelloxxx";
            lexer.start(input, 3, 8, 0);
            assertNotNull(lexer.getTokenType());
            assertEquals(8, lexer.getBufferEnd());
        }

        @Test
        @DisplayName("tag context persists across newlines until reset by setting")
        void tagContextPersistsAcrossNewlines() {
            // In the lexer, tag context (from [Tags]) takes priority over control flow
            // and persists until a non-[Tags] setting resets it
            List<IElementType> tokens = tokenize("[Tags] tag1\ntag2");
            // [Tags] → SETTING, space → WS, tag1 → TAG, \n → WS, tag2 → TAG
            long tagCount = tokens.stream().filter(t -> t == RobotTokenTypes.TAG).count();
            assertEquals(2, tagCount, "Both tag1 and tag2 should be TAG in tag context");
        }

        @Test
        @DisplayName("tag context is reset by a different setting")
        void tagContextResetBySetting() {
            List<IElementType> tokens = tokenize("[Tags] smoke\n[Documentation] text");
            // After [Documentation], inTagsContext should be false
            // Last meaningful token "text" should NOT be TAG
            IElementType lastNonWs = null;
            for (IElementType t : tokens) {
                if (t != RobotTokenTypes.WHITE_SPACE) {
                    lastNonWs = t;
                }
            }
            assertEquals(RobotTokenTypes.IDENTIFIER, lastNonWs,
                "'text' after [Documentation] should be IDENTIFIER, not TAG");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        @Test
        @DisplayName("single character input")
        void singleChar() {
            List<IElementType> tokens = tokenize("a");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.IDENTIFIER, tokens.get(0));
        }

        @Test
        @DisplayName("only whitespace")
        void onlyWhitespace() {
            List<IElementType> tokens = tokenize("   \n\t  ");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.WHITE_SPACE, tokens.get(0));
        }

        @Test
        @DisplayName("unclosed variable runs to end")
        void unclosedVariable() {
            // ${var without closing brace - should read to end
            List<IElementType> tokens = tokenize("${unclosed");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.VARIABLE, tokens.get(0));
        }

        @Test
        @DisplayName("unclosed string runs to end")
        void unclosedString() {
            List<IElementType> tokens = tokenize("\"unclosed string");
            assertEquals(1, tokens.size());
            assertEquals(RobotTokenTypes.STRING, tokens.get(0));
        }
    }
}
