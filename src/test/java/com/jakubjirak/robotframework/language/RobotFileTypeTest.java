/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.language;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RobotFileType}.
 */
@DisplayName("RobotFileType")
class RobotFileTypeTest {

    @Test
    @DisplayName("singleton INSTANCE is not null")
    void instanceNotNull() {
        assertNotNull(RobotFileType.INSTANCE);
    }

    @Test
    @DisplayName("INSTANCE returns same object each time")
    void singletonBehavior() {
        assertSame(RobotFileType.INSTANCE, RobotFileType.INSTANCE);
    }

    @Test
    @DisplayName("getName returns 'Robot Framework'")
    void name() {
        assertEquals("Robot Framework", RobotFileType.INSTANCE.getName());
    }

    @Test
    @DisplayName("getDescription returns expected description")
    void description() {
        assertEquals("Robot Framework test file", RobotFileType.INSTANCE.getDescription());
    }

    @Test
    @DisplayName("getDefaultExtension returns 'robot'")
    void defaultExtension() {
        assertEquals("robot", RobotFileType.INSTANCE.getDefaultExtension());
    }

    @Test
    @DisplayName("file type is associated with RobotLanguage")
    void associatedLanguage() {
        assertSame(RobotLanguage.INSTANCE, RobotFileType.INSTANCE.getLanguage());
    }
}
