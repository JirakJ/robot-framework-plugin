/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.jakubjirak.robotframework.analysis.RobotFinding;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Flags test cases and tasks without a single executable step. Robot Framework
 * fails such a test at runtime with "Test case contains no keywords".
 */
public class RobotEmptyTestCaseInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(RobotFinding.Category.EMPTY_TEST_CASE);
    }

    @Override
    protected LocalQuickFix @NotNull [] fixesFor(@NotNull RobotFinding finding) {
        return new LocalQuickFix[]{new RobotAddNoOperationFix()};
    }
}
