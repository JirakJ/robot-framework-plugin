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
 * Flags tab characters used for cell separation, which Robot Framework collapses
 * to a single separator and frequently breaks alignment.
 */
public class RobotTabCharacterInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(RobotFinding.Category.TAB_INDENTATION);
    }

    @Override
    protected LocalQuickFix @NotNull [] fixesFor(@NotNull RobotFinding finding) {
        return new LocalQuickFix[]{new RobotConvertTabsFix()};
    }
}
