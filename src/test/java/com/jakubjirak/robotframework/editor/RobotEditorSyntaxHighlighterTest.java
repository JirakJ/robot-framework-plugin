/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.editor;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RobotSyntaxHighlighter} (editor package).
 * Tests the comprehensive token-to-TextAttributesKey mapping including RF 7+ control flow.
 */
@DisplayName("RobotSyntaxHighlighter (editor)")
class RobotEditorSyntaxHighlighterTest {

    private RobotSyntaxHighlighter highlighter;

    @BeforeEach
    void setUp() {
        highlighter = new RobotSyntaxHighlighter();
    }

    @Test
    @DisplayName("COMMENT maps to comment attributes")
    void commentHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.COMMENT);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("KEYWORD maps to keyword attributes")
    void keywordHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("VARIABLE maps to variable attributes")
    void variableHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.VARIABLE);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("STRING maps to string attributes")
    void stringHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.STRING);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("SETTING maps to setting attributes")
    void settingHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.SETTING);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("IDENTIFIER maps to identifier attributes")
    void identifierHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.IDENTIFIER);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("EMBEDDED_ARGUMENT maps to embedded argument attributes")
    void embeddedArgHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.EMBEDDED_ARGUMENT);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("TAG maps to tag attributes")
    void tagHighlight() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.TAG);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("all section headers map to section attributes")
    void sectionHeaders() {
        TextAttributesKey[] settings = highlighter.getTokenHighlights(RobotTokenTypes.SETTINGS_HEADER);
        TextAttributesKey[] variables = highlighter.getTokenHighlights(RobotTokenTypes.VARIABLES_HEADER);
        TextAttributesKey[] testCases = highlighter.getTokenHighlights(RobotTokenTypes.TEST_CASES_HEADER);
        TextAttributesKey[] keywords = highlighter.getTokenHighlights(RobotTokenTypes.KEYWORDS_HEADER);
        TextAttributesKey[] generic = highlighter.getTokenHighlights(RobotTokenTypes.SECTION_HEADER);

        assertEquals(1, settings.length);
        assertArrayEquals(settings, variables);
        assertArrayEquals(variables, testCases);
        assertArrayEquals(testCases, keywords);
        assertArrayEquals(keywords, generic);
    }

    @Test
    @DisplayName("IF_KEYWORD maps to control flow attributes")
    void ifKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.IF_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("ELSE_IF_KEYWORD maps to control flow attributes")
    void elseIfKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.ELSE_IF_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("ELSE_KEYWORD maps to control flow attributes")
    void elseKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.ELSE_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("TRY_KEYWORD maps to control flow attributes")
    void tryKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.TRY_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("EXCEPT_KEYWORD maps to control flow attributes")
    void exceptKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.EXCEPT_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("FINALLY_KEYWORD maps to control flow attributes")
    void finallyKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.FINALLY_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("WHILE_KEYWORD maps to control flow attributes")
    void whileKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.WHILE_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("FOR_KEYWORD maps to control flow attributes")
    void forKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.FOR_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("END_KEYWORD maps to control flow attributes")
    void endKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.END_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("BREAK_KEYWORD maps to control flow attributes")
    void breakKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.BREAK_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("CONTINUE_KEYWORD maps to control flow attributes")
    void continueKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.CONTINUE_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("RETURN_KEYWORD maps to control flow attributes")
    void returnKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.RETURN_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("VAR_KEYWORD maps to control flow attributes")
    void varKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.VAR_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("IN_KEYWORD maps to control flow attributes")
    void inKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.IN_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("IN_RANGE_KEYWORD maps to control flow attributes")
    void inRangeKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.IN_RANGE_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("IN_ENUMERATE_KEYWORD maps to control flow attributes")
    void inEnumerateKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.IN_ENUMERATE_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("IN_ZIP_KEYWORD maps to control flow attributes")
    void inZipKeyword() {
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.IN_ZIP_KEYWORD);
        assertEquals(1, keys.length);
    }

    @Test
    @DisplayName("all control flow keywords map to same attributes")
    void allControlFlowSameMapping() {
        TextAttributesKey[] ifKeys = highlighter.getTokenHighlights(RobotTokenTypes.IF_KEYWORD);
        TextAttributesKey[] elseKeys = highlighter.getTokenHighlights(RobotTokenTypes.ELSE_KEYWORD);
        TextAttributesKey[] forKeys = highlighter.getTokenHighlights(RobotTokenTypes.FOR_KEYWORD);
        TextAttributesKey[] whileKeys = highlighter.getTokenHighlights(RobotTokenTypes.WHILE_KEYWORD);
        TextAttributesKey[] tryKeys = highlighter.getTokenHighlights(RobotTokenTypes.TRY_KEYWORD);
        TextAttributesKey[] endKeys = highlighter.getTokenHighlights(RobotTokenTypes.END_KEYWORD);
        TextAttributesKey[] returnKeys = highlighter.getTokenHighlights(RobotTokenTypes.RETURN_KEYWORD);

        assertArrayEquals(ifKeys, elseKeys);
        assertArrayEquals(elseKeys, forKeys);
        assertArrayEquals(forKeys, whileKeys);
        assertArrayEquals(whileKeys, tryKeys);
        assertArrayEquals(tryKeys, endKeys);
        assertArrayEquals(endKeys, returnKeys);
    }

    @Test
    @DisplayName("unknown token type maps to empty keys")
    void unknownTokenType() {
        // NUMBER and OPERATOR are not handled in editor highlighter → empty
        TextAttributesKey[] keys = highlighter.getTokenHighlights(RobotTokenTypes.NUMBER);
        assertEquals(0, keys.length);
    }

    @Test
    @DisplayName("TextAttributesKey constants are non-null")
    void constantsNonNull() {
        assertNotNull(RobotSyntaxHighlighter.COMMENT);
        assertNotNull(RobotSyntaxHighlighter.KEYWORD);
        assertNotNull(RobotSyntaxHighlighter.CONTROL_FLOW);
        assertNotNull(RobotSyntaxHighlighter.VARIABLE);
        assertNotNull(RobotSyntaxHighlighter.STRING);
        assertNotNull(RobotSyntaxHighlighter.SECTION_HEADER);
        assertNotNull(RobotSyntaxHighlighter.SETTING);
        assertNotNull(RobotSyntaxHighlighter.IDENTIFIER);
        assertNotNull(RobotSyntaxHighlighter.EMBEDDED_ARGUMENT);
        assertNotNull(RobotSyntaxHighlighter.TAG);
    }

    @Test
    @DisplayName("getHighlightingLexer returns non-null lexer")
    void highlightingLexer() {
        assertNotNull(highlighter.getHighlightingLexer());
    }
}
