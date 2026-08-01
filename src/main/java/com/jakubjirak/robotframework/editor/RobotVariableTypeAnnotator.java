/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jakubjirak.robotframework.analysis.RobotSyntax;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Paints the type annotation of a variable introduced by Robot Framework 7.3,
 * for example the {@code list[int]} in {@code ${ids: list[int]}}, so that the
 * type reads differently from the variable name.
 */
public class RobotVariableTypeAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getNode() == null
                || element.getNode().getElementType() != RobotTokenTypes.VARIABLE) {
            return;
        }

        RobotSyntax.TypedVariable variable = RobotSyntax.parseVariable(element.getText());
        if (variable == null || !variable.isTyped()) {
            return;
        }

        int base = element.getTextRange().getStartOffset();
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(new TextRange(base + variable.getTypeStart(), base + variable.getTypeEnd()))
                .textAttributes(RobotSyntaxHighlighter.VARIABLE_TYPE)
                .create();
    }
}
