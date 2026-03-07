/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.language;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RobotLanguage}.
 */
@DisplayName("RobotLanguage")
class RobotLanguageTest {

    @Test
    @DisplayName("singleton INSTANCE is not null")
    void instanceNotNull() {
        assertNotNull(RobotLanguage.INSTANCE);
    }

    @Test
    @DisplayName("INSTANCE returns same object each time")
    void singletonBehavior() {
        assertSame(RobotLanguage.INSTANCE, RobotLanguage.INSTANCE);
    }

    @Test
    @DisplayName("language ID is RobotFramework")
    void languageId() {
        assertEquals("RobotFramework", RobotLanguage.INSTANCE.getID());
    }

    @Test
    @DisplayName("language is instance of Language")
    void instanceOfLanguage() {
        assertInstanceOf(com.intellij.lang.Language.class, RobotLanguage.INSTANCE);
    }
}
