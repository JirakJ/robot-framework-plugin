/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.language;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RobotElementType}.
 */
@DisplayName("RobotElementType")
class RobotElementTypeTest {

    @Test
    @DisplayName("constructor creates element with correct debug name")
    void debugName() {
        RobotElementType type = new RobotElementType("TEST_TYPE");
        assertEquals("TEST_TYPE", type.toString());
    }

    @Test
    @DisplayName("element type is associated with RobotLanguage")
    void associatedLanguage() {
        RobotElementType type = new RobotElementType("MY_TYPE");
        assertEquals(RobotLanguage.INSTANCE, type.getLanguage());
    }

    @Test
    @DisplayName("element type extends IElementType")
    void extendsIElementType() {
        RobotElementType type = new RobotElementType("PARENT_CHECK");
        assertInstanceOf(IElementType.class, type);
    }

    @Test
    @DisplayName("different instances with same name are distinct objects")
    void distinctInstances() {
        RobotElementType a = new RobotElementType("SAME_NAME");
        RobotElementType b = new RobotElementType("SAME_NAME");
        assertNotSame(a, b);
    }
}
