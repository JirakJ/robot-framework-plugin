/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RobotCommenter}.
 */
@DisplayName("RobotCommenter")
class RobotCommenterTest {

    private RobotCommenter commenter;

    @BeforeEach
    void setUp() {
        commenter = new RobotCommenter();
    }

    @Test
    @DisplayName("line comment prefix is '#'")
    void lineCommentPrefix() {
        assertEquals("#", commenter.getLineCommentPrefix());
    }

    @Test
    @DisplayName("block comment prefix is null (not supported)")
    void blockCommentPrefix() {
        assertNull(commenter.getBlockCommentPrefix());
    }

    @Test
    @DisplayName("block comment suffix is null (not supported)")
    void blockCommentSuffix() {
        assertNull(commenter.getBlockCommentSuffix());
    }

    @Test
    @DisplayName("commented block comment prefix is null")
    void commentedBlockCommentPrefix() {
        assertNull(commenter.getCommentedBlockCommentPrefix());
    }

    @Test
    @DisplayName("commented block comment suffix is null")
    void commentedBlockCommentSuffix() {
        assertNull(commenter.getCommentedBlockCommentSuffix());
    }
}
