/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;

/**
 * Flags suite variables declared in {@code *** Variables ***} that are never
 * referenced within the same file. Disabled by default because variables may be
 * consumed from imported resource files.
 */
public class RobotUnusedVariableInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected RobotFinding.Category category() {
        return RobotFinding.Category.UNUSED_VARIABLE;
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.LIKE_UNUSED_SYMBOL;
    }
}
