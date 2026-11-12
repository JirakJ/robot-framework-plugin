/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the Marketplace screenshot fixtures honest: the suites under
 * {@code docs/screenshots/sample-project} must keep producing exactly the
 * findings the published screenshots show.
 */
@DisplayName("Screenshot sample project")
class SampleProjectTest {

    private static final Path SAMPLES = Path.of("docs", "screenshots", "sample-project");

    private Set<RobotFinding.Category> categoriesOf(String fileName) throws IOException {
        Path file = SAMPLES.resolve(fileName);
        assertTrue(Files.isRegularFile(file), "missing screenshot fixture: " + file.toAbsolutePath());
        String source = Files.readString(file, StandardCharsets.UTF_8);
        return RobotCodeAnalyzer.analyze(source).stream()
                .map(RobotFinding::getCategory)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RobotFinding.Category.class)));
    }

    @Test
    @DisplayName("the modern showcase suite is completely clean")
    void showcaseIsClean() throws IOException {
        assertEquals(EnumSet.noneOf(RobotFinding.Category.class), categoriesOf("rf74_showcase.robot"));
    }

    @Test
    @DisplayName("the migration suite triggers exactly the advertised inspections")
    void migrationSuiteFindings() throws IOException {
        Set<RobotFinding.Category> expected = EnumSet.of(
                RobotFinding.Category.WITH_NAME_ALIAS,
                RobotFinding.Category.DEPRECATED_SETTING,
                RobotFinding.Category.SECRET_LITERAL_VALUE,
                RobotFinding.Category.INVALID_VARIABLE_TYPE,
                RobotFinding.Category.VAR_WITHOUT_VALUE,
                RobotFinding.Category.UNCLOSED_BLOCK,
                RobotFinding.Category.UNUSED_VARIABLE);
        assertEquals(expected, categoriesOf("rf74_migration.robot"));
    }

    @Test
    @DisplayName("the formatter turns messy_suite.robot into exactly the formatted fixture")
    void formatterSampleMatchesScreenshot() throws IOException {
        String messy = Files.readString(SAMPLES.resolve("messy_suite.robot"), StandardCharsets.UTF_8);
        String expected = Files.readString(SAMPLES.resolve("messy_suite.formatted.robot"), StandardCharsets.UTF_8);
        assertEquals(expected, new RobotFormatter().format(messy));
    }
}
