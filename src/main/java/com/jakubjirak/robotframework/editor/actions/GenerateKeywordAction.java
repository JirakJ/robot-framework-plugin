/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
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
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.analysis.RobotKeywordGenerator;
import com.jakubjirak.robotframework.language.RobotFile;
import org.jetbrains.annotations.NotNull;

/**
 * Generates a user keyword skeleton in the {@code *** Keywords ***} section.
 *
 * <p>When the caret sits on a keyword call, that call's name is used, which turns
 * the action into "implement the keyword I just wrote". Otherwise the name is
 * asked for.</p>
 */
public class GenerateKeywordAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabledAndVisible(e.getProject() != null && file instanceof RobotFile);
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
        String source = document.getText();
        int caret = editor.getCaretModel().getOffset();

        String suggested = RobotKeywordGenerator.keywordNameAt(source, caret);
        String name = Messages.showInputDialog(project,
                "Keyword name:",
                "Generate Robot Framework Keyword",
                Messages.getQuestionIcon(),
                suggested == null ? "" : suggested,
                null);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        RobotKeywordGenerator.Edit edit = RobotKeywordGenerator.generate(source, name);
        if (edit == null) {
            Messages.showInfoMessage(project,
                    "Keyword '" + name.trim() + "' already exists in this file.",
                    "Generate Robot Framework Keyword");
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, "Generate Robot Framework Keyword", null, () -> {
            document.insertString(edit.getOffset(), edit.getText());
            PsiDocumentManager.getInstance(project).commitDocument(document);
            editor.getCaretModel().moveToOffset(Math.min(edit.getCaretOffset(), document.getTextLength()));
            editor.getScrollingModel().scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER);
        }, file);
    }
}
