package com.jakubjirak.robotframework.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotator for highlighting embedded arguments in Robot Framework keyword definitions.
 * Embedded arguments are variables embedded in keyword names, e.g., "User ${name} logs in".
 */
public class RobotEmbeddedArgumentAnnotator implements Annotator {

    private static final TextAttributesKey EMBEDDED_ARG_KEY =
        TextAttributesKey.createTextAttributesKey("ROBOT_EMBEDDED_ARG",
            DefaultLanguageHighlighterColors.PARAMETER);

    // Pattern to match embedded arguments: ${variable}, @{list}, &{dict}, %{env}
    private static final Pattern EMBEDDED_ARG_PATTERN =
        Pattern.compile("(\\$\\{[^}]+\\}|@\\{[^}]+\\}|&\\{[^}]+\\}|%\\{[^}]+\\})");

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // Check if this is likely a keyword definition (contains keyword-like text)
        if (element.getNode() == null) {
            return;
        }

        // Look for embedded arguments in identifier or keyword tokens
        if (element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER ||
            element.getNode().getElementType() == RobotTokenTypes.KEYWORD) {

            String text = element.getText();
            if (text != null && !text.isEmpty()) {
                highlightEmbeddedArguments(element, text, holder);
            }
        }
    }

    private void highlightEmbeddedArguments(@NotNull PsiElement element,
                                           @NotNull String text,
                                           @NotNull AnnotationHolder holder) {
        Matcher matcher = EMBEDDED_ARG_PATTERN.matcher(text);
        int elementStart = element.getTextRange().getStartOffset();

        while (matcher.find()) {
            int start = elementStart + matcher.start();
            int end = elementStart + matcher.end();

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(new TextRange(start, end))
                .textAttributes(EMBEDDED_ARG_KEY)
                .create();
        }
    }
}

