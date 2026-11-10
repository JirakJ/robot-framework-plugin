/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.inspection;

import com.jakubjirak.robotframework.analysis.RobotFinding;

import java.util.Set;

/**
 * Flags test case and task names that are too long to read comfortably in the
 * log, the report and the runner output.
 */
public class RobotLongTestNameInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(RobotFinding.Category.LONG_TEST_NAME);
    }
}
