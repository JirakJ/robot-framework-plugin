/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;

import java.util.Set;

/**
 * Flags {@code IF}, {@code FOR}, {@code WHILE}, {@code TRY} and {@code GROUP}
 * blocks that are never closed with {@code END}, and {@code END} markers without
 * a matching opener. {@code GROUP} was added in Robot Framework 7.2; inline
 * {@code IF} (RF 7.0) correctly needs no {@code END}.
 */
public class RobotBlockStructureInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(
                RobotFinding.Category.UNCLOSED_BLOCK,
                RobotFinding.Category.ORPHAN_END);
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }
}
