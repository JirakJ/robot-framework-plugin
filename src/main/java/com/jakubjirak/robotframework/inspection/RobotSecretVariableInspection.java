/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;

import java.util.Set;

/**
 * Flags {@code Secret} variables (Robot Framework 7.4) that are given a literal
 * value in the {@code *** Variables ***} section.
 *
 * <p>Robot Framework rejects literal secrets on purpose: a secret written into
 * the suite is already in version control, which defeats the point. The value
 * has to come from another variable or from the environment.</p>
 */
public class RobotSecretVariableInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(RobotFinding.Category.SECRET_LITERAL_VALUE);
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }
}
