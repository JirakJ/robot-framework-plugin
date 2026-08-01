/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.jakubjirak.robotframework.analysis.RobotFinding;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Flags syntax that Robot Framework 6.0–7.4 deprecated and offers the modern
 * replacement wherever the migration is mechanical.
 *
 * <p>Covered: {@code [Return]} → {@code RETURN} (7.0), {@code Force Tags} →
 * {@code Test Tags} (6.0), {@code Default Tags}, {@code WITH NAME} → {@code AS}
 * (6.0), singular section headers (6.0), {@code Exit For Loop} / {@code Continue
 * For Loop} / {@code Return From Keyword} / {@code Run Keyword Unless} (5.0),
 * {@code FOR IN ZIP} without {@code mode=} (7.0), literal {@code -tag} in
 * {@code Test Tags} (7.2) and {@code VAR} without a value (7.4).</p>
 */
public class RobotDeprecatedSyntaxInspection extends AbstractRobotAnalyzerInspection {

    @Override
    protected Set<RobotFinding.Category> categories() {
        return Set.of(
                RobotFinding.Category.DEPRECATED_SETTING,
                RobotFinding.Category.DEPRECATED_KEYWORD,
                RobotFinding.Category.SINGULAR_SECTION_HEADER,
                RobotFinding.Category.WITH_NAME_ALIAS,
                RobotFinding.Category.FOR_IN_ZIP_WITHOUT_MODE,
                RobotFinding.Category.HYPHEN_TAG_LITERAL,
                RobotFinding.Category.VAR_WITHOUT_VALUE);
    }

    @Override
    protected ProblemHighlightType highlightType() {
        return ProblemHighlightType.LIKE_DEPRECATED;
    }

    @Override
    protected LocalQuickFix @NotNull [] fixesFor(@NotNull RobotFinding finding) {
        String replacement = finding.getReplacement();
        if (replacement == null) {
            return LocalQuickFix.EMPTY_ARRAY;
        }
        return new LocalQuickFix[]{new RobotReplaceRangeFix(replacement)};
    }
}
