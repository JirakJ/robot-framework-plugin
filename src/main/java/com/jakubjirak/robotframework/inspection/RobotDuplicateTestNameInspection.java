/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;

/**
 * Flags duplicate test-case names. Robot Framework silently runs only one test
 * per unique (normalized) name, so duplicates hide tests.
 */
public class RobotDuplicateTestNameInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected RobotFinding.Category category() {
        return RobotFinding.Category.DUPLICATE_TEST_NAME;
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }
}
