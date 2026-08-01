/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
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
 * Quick fix that replaces the reported range with a fixed piece of text, used to
 * migrate deprecated Robot Framework syntax such as {@code [Return]} → {@code RETURN}
 * or {@code WITH NAME} → {@code AS}.
 */
public class RobotReplaceRangeFix implements LocalQuickFix {

    private final String replacement;
    private final String familyName;

    public RobotReplaceRangeFix(@NotNull String replacement) {
        this.replacement = replacement;
        this.familyName = "Replace with '" + replacement + "'";
    }

    @Override
    public @NotNull String getFamilyName() {
        return familyName;
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
        int start = base + rangeInElement.getStartOffset();
        int end = Math.min(base + rangeInElement.getEndOffset(), document.getTextLength());
        if (start < 0 || start >= end) {
            return;
        }

        document.replaceString(start, end, replacement);
        manager.commitDocument(document);
    }
}
