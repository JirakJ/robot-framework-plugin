/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;

import java.util.Set;

/**
 * Flags data that appears before the first {@code *** ... ***} section header.
 * Robot Framework silently ignores it, so tests written there never run.
 */
public class RobotContentOutsideSectionInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(RobotFinding.Category.CONTENT_OUTSIDE_SECTION);
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }
}
