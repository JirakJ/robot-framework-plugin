/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;

import java.util.Set;

/**
 * Validates the variable type conversion syntax introduced in Robot Framework
 * 7.3, for example {@code ${amount: int}} or {@code @{ids: list[int]}}.
 *
 * <p>Only structural problems are reported — unbalanced brackets, an empty type
 * or a dangling union operator. Unknown type names are accepted because
 * libraries can register custom converters and enums.</p>
 */
public class RobotVariableTypeInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(RobotFinding.Category.INVALID_VARIABLE_TYPE);
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }
}
