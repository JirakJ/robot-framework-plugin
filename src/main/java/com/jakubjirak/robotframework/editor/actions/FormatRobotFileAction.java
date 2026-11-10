/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.editor.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.analysis.RobotFormatter;
import com.jakubjirak.robotframework.analysis.RobotFormatterOptions;
import com.jakubjirak.robotframework.language.RobotFile;
import com.jakubjirak.robotframework.settings.RobotSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Editor action that reformats the current Robot Framework file using
 * {@link RobotFormatter}, honouring the user's formatter settings.
 */
public class FormatRobotFileAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(file instanceof RobotFile && editor != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null || !(file instanceof RobotFile)) {
            return;
        }

        Document document = editor.getDocument();
        RobotFormatterOptions options = optionsFor(project);
        String formatted = new RobotFormatter(options).format(document.getText());
        if (formatted == null || formatted.equals(document.getText())) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(
                project,
                "Format Robot File",
                null,
                () -> document.setText(formatted),
                file);
    }

    private RobotFormatterOptions optionsFor(@NotNull Project project) {
        RobotSettings settings = RobotSettings.getInstance(project);
        return RobotFormatterOptions.defaults().toBuilder()
                .indentSize(settings.getFormatterIndentSize())
                .normalizeSectionHeaders(settings.isFormatterNormalizeHeaders())
                .build();
    }
}
