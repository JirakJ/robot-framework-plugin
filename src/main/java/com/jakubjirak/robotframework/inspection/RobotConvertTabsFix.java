/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.settings.RobotSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Quick fix that converts every tab character on the affected line to spaces,
 * using the configured Robot Framework indent size.
 */
public class RobotConvertTabsFix implements LocalQuickFix {

    @Override
    public @NotNull String getFamilyName() {
        return "Convert tabs to spaces";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        if (element == null) {
            return;
        }
        PsiFile file = element.getContainingFile();
        if (file == null || descriptor.getTextRangeInElement() == null) {
            return;
        }

        PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
        Document document = manager.getDocument(file);
        if (document == null) {
            return;
        }

        int base = element.getTextRange().getStartOffset();
        int offset = base + descriptor.getTextRangeInElement().getStartOffset();
        if (offset < 0 || offset >= document.getTextLength()) {
            return;
        }

        int lineNumber = document.getLineNumber(offset);
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        String lineText = document.getText().substring(lineStart, lineEnd);
        if (lineText.indexOf('\t') < 0) {
            return;
        }

        int indentSize = RobotSettings.getInstance(project).getFormatterIndentSize();
        String replacement = lineText.replace("\t", " ".repeat(Math.max(2, indentSize)));

        document.replaceString(lineStart, lineEnd, replacement);
        manager.commitDocument(document);
    }
}
