/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Flags trailing whitespace, which can turn into significant (empty) cells in
 * Robot Framework and produces noisy diffs.
 */
public class RobotTrailingWhitespaceInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(RobotFinding.Category.TRAILING_WHITESPACE);
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }

    @Override
    protected LocalQuickFix @NotNull [] fixesFor(@NotNull RobotFinding finding) {
        return new LocalQuickFix[]{new RobotDeleteRangeFix("Remove trailing whitespace")};
    }
}
