/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Quick fix that turns an empty test case into a runnable one by appending a
 * {@code No Operation} step right below the test name.
 */
public class RobotAddNoOperationFix implements LocalQuickFix {

    // ponytail: four spaces is the Robot Framework convention; the formatter
    // reindents the file if the project prefers something else.
    private static final String STEP = "\n    No Operation";

    @Override
    public @NotNull String getFamilyName() {
        return "Add 'No Operation' step";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        if (element == null) {
            return;
        }
        PsiFile file = element.getContainingFile();
        TextRange rangeInElement = descriptor.getTextRangeInElement();
        if (file == null || rangeInElement == null) {
            return;
        }

        PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
        Document document = manager.getDocument(file);
        if (document == null) {
            return;
        }

        int base = element.getTextRange().getStartOffset();
        int insertAt = Math.min(base + rangeInElement.getEndOffset(), document.getTextLength());
        if (insertAt < 0) {
            return;
        }

        document.insertString(insertAt, STEP);
        manager.commitDocument(document);
    }
}
