/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.settings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RobotSettings")
class RobotSettingsTest {

    @Test
    void defaults() {
        RobotSettings settings = new RobotSettings();
        assertEquals("", settings.getInterpreterPath());
        assertEquals("", settings.getRobotArgs());
        assertEquals("", settings.getOutputDirectory());
        assertEquals(4, settings.getFormatterIndentSize());
        assertTrue(settings.isFormatterNormalizeHeaders());
    }

    @Test
    void settersWriteThroughToState() {
        RobotSettings settings = new RobotSettings();
        settings.setInterpreterPath("/usr/bin/python3");
        settings.setRobotArgs("--dryrun");
        settings.setOutputDirectory("out");
        settings.setFormatterIndentSize(6);
        settings.setFormatterNormalizeHeaders(false);
        RobotSettings.State state = settings.getState();
        assertEquals("/usr/bin/python3", state.interpreterPath);
        assertEquals("--dryrun", state.robotArgs);
        assertEquals("out", state.outputDirectory);
        assertEquals(6, state.formatterIndentSize);
        assertFalse(state.formatterNormalizeHeaders);
    }

    @Test
    void indentSizeIsClampedToTwo() {
        RobotSettings settings = new RobotSettings();
        settings.setFormatterIndentSize(0);
        assertEquals(2, settings.getFormatterIndentSize());
    }

    @Test
    void loadStateReplacesState() {
        RobotSettings settings = new RobotSettings();
        RobotSettings.State loaded = new RobotSettings.State();
        loaded.robotArgs = "--exitonfailure";
        settings.loadState(loaded);
        assertSame(loaded, settings.getState());
        assertEquals("--exitonfailure", settings.getRobotArgs());
    }

    @Test
    void parseIndentSizeHandlesValidClampedAndInvalidInput() {
        assertEquals(6, RobotSettingsConfigurable.parseIndentSize(" 6 ", 4));
        assertEquals(2, RobotSettingsConfigurable.parseIndentSize("1", 4));
        assertEquals(2, RobotSettingsConfigurable.parseIndentSize("-8", 4));
        assertEquals(4, RobotSettingsConfigurable.parseIndentSize("four", 4));
        assertEquals(4, RobotSettingsConfigurable.parseIndentSize("", 4));
        assertEquals(4, RobotSettingsConfigurable.parseIndentSize(null, 4));
    }
}
