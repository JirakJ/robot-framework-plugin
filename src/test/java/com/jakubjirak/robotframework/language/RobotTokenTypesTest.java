/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.language;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RobotTokenTypes}.
 * Verifies that all token type constants are correctly defined and distinct.
 */
@DisplayName("RobotTokenTypes")
class RobotTokenTypesTest {

    @Test
    @DisplayName("WHITE_SPACE uses platform TokenType")
    void whiteSpaceIsPlatformType() {
        assertSame(TokenType.WHITE_SPACE, RobotTokenTypes.WHITE_SPACE);
    }

    @Test
    @DisplayName("all custom token types are non-null")
    void allTokenTypesNonNull() {
        assertNotNull(RobotTokenTypes.COMMENT);
        assertNotNull(RobotTokenTypes.IDENTIFIER);
        assertNotNull(RobotTokenTypes.KEYWORD);
        assertNotNull(RobotTokenTypes.VARIABLE);
        assertNotNull(RobotTokenTypes.STRING);
        assertNotNull(RobotTokenTypes.SECTION_HEADER);
        assertNotNull(RobotTokenTypes.SETTINGS_HEADER);
        assertNotNull(RobotTokenTypes.VARIABLES_HEADER);
        assertNotNull(RobotTokenTypes.TEST_CASES_HEADER);
        assertNotNull(RobotTokenTypes.KEYWORDS_HEADER);
        assertNotNull(RobotTokenTypes.SETTING);
        assertNotNull(RobotTokenTypes.NUMBER);
        assertNotNull(RobotTokenTypes.OPERATOR);
    }

    @Test
    @DisplayName("RF 7+ control flow token types are non-null")
    void controlFlowTokenTypesNonNull() {
        assertNotNull(RobotTokenTypes.IF_KEYWORD);
        assertNotNull(RobotTokenTypes.ELSE_IF_KEYWORD);
        assertNotNull(RobotTokenTypes.ELSE_KEYWORD);
        assertNotNull(RobotTokenTypes.TRY_KEYWORD);
        assertNotNull(RobotTokenTypes.EXCEPT_KEYWORD);
        assertNotNull(RobotTokenTypes.FINALLY_KEYWORD);
        assertNotNull(RobotTokenTypes.WHILE_KEYWORD);
        assertNotNull(RobotTokenTypes.FOR_KEYWORD);
        assertNotNull(RobotTokenTypes.END_KEYWORD);
        assertNotNull(RobotTokenTypes.BREAK_KEYWORD);
        assertNotNull(RobotTokenTypes.CONTINUE_KEYWORD);
        assertNotNull(RobotTokenTypes.RETURN_KEYWORD);
        assertNotNull(RobotTokenTypes.VAR_KEYWORD);
    }

    @Test
    @DisplayName("FOR loop modifier token types are non-null")
    void forLoopModifierTypesNonNull() {
        assertNotNull(RobotTokenTypes.IN_KEYWORD);
        assertNotNull(RobotTokenTypes.IN_RANGE_KEYWORD);
        assertNotNull(RobotTokenTypes.IN_ENUMERATE_KEYWORD);
        assertNotNull(RobotTokenTypes.IN_ZIP_KEYWORD);
    }

    @Test
    @DisplayName("special token types are non-null")
    void specialTokenTypesNonNull() {
        assertNotNull(RobotTokenTypes.EMBEDDED_ARGUMENT);
        assertNotNull(RobotTokenTypes.TAG);
    }

    @Test
    @DisplayName("all custom token types are instances of RobotElementType")
    void tokenTypesAreRobotElementType() {
        assertInstanceOf(RobotElementType.class, RobotTokenTypes.COMMENT);
        assertInstanceOf(RobotElementType.class, RobotTokenTypes.IDENTIFIER);
        assertInstanceOf(RobotElementType.class, RobotTokenTypes.KEYWORD);
        assertInstanceOf(RobotElementType.class, RobotTokenTypes.VARIABLE);
        assertInstanceOf(RobotElementType.class, RobotTokenTypes.STRING);
        assertInstanceOf(RobotElementType.class, RobotTokenTypes.SECTION_HEADER);
        assertInstanceOf(RobotElementType.class, RobotTokenTypes.TAG);
    }

    @Test
    @DisplayName("token types are distinct objects")
    void tokenTypesAreDistinct() {
        IElementType[] types = {
            RobotTokenTypes.COMMENT, RobotTokenTypes.IDENTIFIER,
            RobotTokenTypes.KEYWORD, RobotTokenTypes.VARIABLE,
            RobotTokenTypes.STRING, RobotTokenTypes.SECTION_HEADER,
            RobotTokenTypes.SETTINGS_HEADER, RobotTokenTypes.VARIABLES_HEADER,
            RobotTokenTypes.TEST_CASES_HEADER, RobotTokenTypes.KEYWORDS_HEADER,
            RobotTokenTypes.SETTING, RobotTokenTypes.NUMBER,
            RobotTokenTypes.OPERATOR, RobotTokenTypes.TAG
        };
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotSame(types[i], types[j],
                    "Token types at index " + i + " and " + j + " should be distinct");
            }
        }
    }

    @Test
    @DisplayName("control flow token types have correct debug names")
    void controlFlowDebugNames() {
        assertEquals("IF_KEYWORD", RobotTokenTypes.IF_KEYWORD.toString());
        assertEquals("ELSE_IF_KEYWORD", RobotTokenTypes.ELSE_IF_KEYWORD.toString());
        assertEquals("ELSE_KEYWORD", RobotTokenTypes.ELSE_KEYWORD.toString());
        assertEquals("TRY_KEYWORD", RobotTokenTypes.TRY_KEYWORD.toString());
        assertEquals("EXCEPT_KEYWORD", RobotTokenTypes.EXCEPT_KEYWORD.toString());
        assertEquals("FINALLY_KEYWORD", RobotTokenTypes.FINALLY_KEYWORD.toString());
        assertEquals("WHILE_KEYWORD", RobotTokenTypes.WHILE_KEYWORD.toString());
        assertEquals("FOR_KEYWORD", RobotTokenTypes.FOR_KEYWORD.toString());
        assertEquals("BREAK_KEYWORD", RobotTokenTypes.BREAK_KEYWORD.toString());
        assertEquals("CONTINUE_KEYWORD", RobotTokenTypes.CONTINUE_KEYWORD.toString());
        assertEquals("RETURN_KEYWORD", RobotTokenTypes.RETURN_KEYWORD.toString());
        assertEquals("VAR_KEYWORD", RobotTokenTypes.VAR_KEYWORD.toString());
    }

    @Test
    @DisplayName("basic token types have correct debug names")
    void basicDebugNames() {
        assertEquals("COMMENT", RobotTokenTypes.COMMENT.toString());
        assertEquals("IDENTIFIER", RobotTokenTypes.IDENTIFIER.toString());
        assertEquals("KEYWORD", RobotTokenTypes.KEYWORD.toString());
        assertEquals("VARIABLE", RobotTokenTypes.VARIABLE.toString());
        assertEquals("STRING", RobotTokenTypes.STRING.toString());
        assertEquals("TAG", RobotTokenTypes.TAG.toString());
    }

    @Test
    @DisplayName("FOR loop modifier types have correct debug names")
    void forModifierDebugNames() {
        assertEquals("IN_KEYWORD", RobotTokenTypes.IN_KEYWORD.toString());
        assertEquals("IN_RANGE_KEYWORD", RobotTokenTypes.IN_RANGE_KEYWORD.toString());
        assertEquals("IN_ENUMERATE_KEYWORD", RobotTokenTypes.IN_ENUMERATE_KEYWORD.toString());
        assertEquals("IN_ZIP_KEYWORD", RobotTokenTypes.IN_ZIP_KEYWORD.toString());
    }
}
