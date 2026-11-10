/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.editor;

import com.intellij.lang.BracePair;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RobotBraceMatcher}.
 */
@DisplayName("RobotBraceMatcher")
class RobotBraceMatcherTest {

    private RobotBraceMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new RobotBraceMatcher();
    }

    @Test
    @DisplayName("getPairs returns exactly 2 brace pairs")
    void pairCount() {
        BracePair[] pairs = matcher.getPairs();
        assertEquals(2, pairs.length);
    }

    @Test
    @DisplayName("first pair matches VARIABLE tokens (for ${}, @{}, &{}, %{})")
    void variablePair() {
        BracePair pair = matcher.getPairs()[0];
        assertEquals(RobotTokenTypes.VARIABLE, pair.getLeftBraceType());
        assertEquals(RobotTokenTypes.VARIABLE, pair.getRightBraceType());
    }

    @Test
    @DisplayName("second pair matches SETTING tokens (for [...])")
    void settingPair() {
        BracePair pair = matcher.getPairs()[1];
        assertEquals(RobotTokenTypes.SETTING, pair.getLeftBraceType());
        assertEquals(RobotTokenTypes.SETTING, pair.getRightBraceType());
    }

    @Test
    @DisplayName("brace pairs are not structural")
    void notStructural() {
        for (BracePair pair : matcher.getPairs()) {
            assertFalse(pair.isStructural());
        }
    }

    @Test
    @DisplayName("isPairedBracesAllowedBeforeType always returns true")
    void pairedBracesAlwaysAllowed() {
        assertTrue(matcher.isPairedBracesAllowedBeforeType(RobotTokenTypes.VARIABLE, null));
        assertTrue(matcher.isPairedBracesAllowedBeforeType(RobotTokenTypes.SETTING, RobotTokenTypes.IDENTIFIER));
        assertTrue(matcher.isPairedBracesAllowedBeforeType(RobotTokenTypes.KEYWORD, RobotTokenTypes.COMMENT));
    }

    @Test
    @DisplayName("getCodeConstructStart returns the opening brace offset")
    void codeConstructStart() {
        assertEquals(0, matcher.getCodeConstructStart(null, 0));
        assertEquals(42, matcher.getCodeConstructStart(null, 42));
        assertEquals(100, matcher.getCodeConstructStart(null, 100));
    }
}
