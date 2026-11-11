/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("RobotRunConfiguration validation")
class RobotRunConfigurationValidationTest {

    @TempDir
    Path dir;

    @Test
    void missingSuiteIsReportedFirst() {
        assertEquals("Set the test suite file or directory to run.",
                RobotRunConfiguration.validationError("", "/nope", "/nope"));
    }

    @Test
    void nonexistentSuite() {
        String suite = dir.resolve("missing.robot").toString();
        assertEquals("Test suite not found: " + suite, RobotRunConfiguration.validationError(suite, "", ""));
    }

    @Test
    void invalidPathCharactersCountAsMissing() {
        assertEquals("Test suite not found: bad\u0000path",
                RobotRunConfiguration.validationError("bad\u0000path", "", ""));
    }

    @Test
    void existingSuiteWithDefaultsIsValid() throws IOException {
        Path suite = Files.writeString(dir.resolve("a.robot"), "*** Test Cases ***\n");
        assertNull(RobotRunConfiguration.validationError(suite.toString(), "", ""));
        assertNull(RobotRunConfiguration.validationError(dir.toString(), dir.toString(), suite.toString()));
    }

    @Test
    void missingWorkingDirectoryAndInterpreter() throws IOException {
        Path suite = Files.writeString(dir.resolve("a.robot"), "");
        String missing = dir.resolve("gone").toString();
        assertEquals("Working directory not found: " + missing,
                RobotRunConfiguration.validationError(suite.toString(), missing, ""));
        assertEquals("Python interpreter not found: " + missing,
                RobotRunConfiguration.validationError(suite.toString(), "", missing));
    }

    @Test
    void orDefaultPrefersExplicitValue() {
        assertEquals("explicit", RobotRunConfiguration.orDefault("explicit", "fallback"));
        assertEquals("fallback", RobotRunConfiguration.orDefault("", "fallback"));
    }
}
