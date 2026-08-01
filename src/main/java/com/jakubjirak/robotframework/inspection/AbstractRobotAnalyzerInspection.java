/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.analysis.RobotCodeAnalyzer;
import com.jakubjirak.robotframework.analysis.RobotFinding;
import com.jakubjirak.robotframework.language.RobotFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base class for inspections that surface one or more {@link RobotFinding.Category}
 * values produced by the pure {@link RobotCodeAnalyzer}.
 *
 * <p>Running the analysis once per file (via {@link #checkFile}) keeps the
 * detection logic IDE-independent and fully unit testable while still presenting
 * each concern as its own configurable inspection.</p>
 */
public abstract class AbstractRobotAnalyzerInspection extends LocalInspectionTool {

    /** The finding categories this inspection reports. */
    protected abstract Set<RobotFinding.Category> categories();

    /** Highlight type; resolves severity from the configured inspection level. */
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }

    /** Optional quick fixes for a finding. Defaults to none. */
    protected LocalQuickFix @NotNull [] fixesFor(@NotNull RobotFinding finding) {
        return LocalQuickFix.EMPTY_ARRAY;
    }

    @Override
    public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file,
                                                    @NotNull InspectionManager manager,
                                                    boolean isOnTheFly) {
        if (!(file instanceof RobotFile)) {
            return null;
        }

        String text = file.getText();
        if (text.isEmpty()) {
            return null;
        }

        int fileLength = text.length();
        List<ProblemDescriptor> problems = new ArrayList<>();
        Set<RobotFinding.Category> categories = categories();

        for (RobotFinding finding : RobotCodeAnalyzer.analyze(text)) {
            if (!categories.contains(finding.getCategory())) {
                continue;
            }
            int start = Math.max(0, Math.min(finding.getStartOffset(), fileLength));
            int end = Math.max(start, Math.min(finding.getEndOffset(), fileLength));
            if (start == end) {
                continue;
            }
            problems.add(manager.createProblemDescriptor(
                    file,
                    new TextRange(start, end),
                    finding.getMessage(),
                    highlightType(),
                    isOnTheFly,
                    fixesFor(finding)));
        }

        return problems.isEmpty() ? null : problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }
}
