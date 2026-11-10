/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.analysis.RobotStructure;
import com.jakubjirak.robotframework.language.RobotFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Puts a Run gutter icon next to every test case and task name, so a suite can
 * be started without opening the run configuration dialog.
 */
public class RobotRunLineMarkerContributor extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (element.getFirstChild() != null) {
            // Only leaves; otherwise the same line would get several icons.
            return null;
        }
        PsiFile file = element.getContainingFile();
        if (!(file instanceof RobotFile)) {
            return null;
        }
        int offset = element.getTextRange().getStartOffset();
        for (RobotStructure.Entry test : RobotStructure.testCases(file.getText())) {
            if (test.getNameStart() == offset) {
                return new Info(
                        AllIcons.RunConfigurations.TestState.Run,
                        ExecutorAction.getActions(0),
                        e -> "Run '" + test.getName() + "'");
            }
        }
        return null;
    }
}
