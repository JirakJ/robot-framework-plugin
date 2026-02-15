package com.jakubjirak.robotframework.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotator for detecting undefined variable references.
 * Provides warnings for variable usage that don't have corresponding definitions.
 */
public class RobotUndefinedVariableAnnotator implements Annotator {

    // Common built-in variables that should not be flagged as undefined
    private static final Set<String> BUILT_IN_VARIABLES = new HashSet<>(Arrays.asList(
        // Paths and directories
        "${CURDIR}", "${TEMPDIR}", "${EXECDIR}", "${/}", "${:}",
        // Common values
        "${SPACE}", "${EMPTY}", "${True}", "${False}", "${None}", "${null}",
        // Test/Suite information
        "${TEST NAME}", "${TEST DOCUMENTATION}", "${TEST STATUS}", "${TEST MESSAGE}",
        "${TEST TAGS}", "${PREV TEST NAME}", "${PREV TEST STATUS}", "${PREV TEST MESSAGE}",
        "${SUITE NAME}", "${SUITE SOURCE}", "${SUITE DOCUMENTATION}", "${SUITE STATUS}", "${SUITE MESSAGE}",
        // Keyword information
        "${KEYWORD STATUS}", "${KEYWORD MESSAGE}",
        // Output and logging
        "${LOG LEVEL}", "${OUTPUT DIR}", "${OUTPUT FILE}", "${LOG FILE}", "${REPORT FILE}",
        "${DEBUG FILE}", "${SUMMARY FILE}"
    ));

    // Pattern to match variable references: ${var}, @{list}, &{dict}, %{env}
    private static final Pattern VARIABLE_PATTERN =
        Pattern.compile("(\\$\\{([^}]+)\\}|@\\{([^}]+)\\}|&\\{([^}]+)\\}|%\\{([^}]+)\\})");

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getNode() == null) {
            return;
        }

        // Check for variable references in any element
        if (element.getNode().getElementType() == RobotTokenTypes.VARIABLE ||
            element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER ||
            element.getNode().getElementType() == RobotTokenTypes.STRING) {

            String text = element.getText();
            Matcher matcher = VARIABLE_PATTERN.matcher(text);

            while (matcher.find()) {
                String fullMatch = matcher.group(1);

                // Skip built-in variables
                if (isBuiltInVariable(fullMatch)) {
                    continue;
                }

                // Check if variable is defined in current file
                PsiFile file = element.getContainingFile();
                if (file != null && !isVariableDefined(file, fullMatch)) {
                    int startOffset = element.getTextRange().getStartOffset() + matcher.start();
                    int endOffset = element.getTextRange().getStartOffset() + matcher.end();

                    holder.newAnnotation(HighlightSeverity.WARNING,
                        "Undefined variable: '" + fullMatch + "'")
                        .range(new TextRange(startOffset, endOffset))
                        .create();
                }
            }
        }
    }

    /**
     * Check if variable name is a built-in variable
     */
    private boolean isBuiltInVariable(String variableName) {
        String upper = variableName.toUpperCase();

        // Check exact match
        if (BUILT_IN_VARIABLES.contains(upper)) {
            return true;
        }

        // Check if it's an environment variable
        if (upper.startsWith("%{")) {
            return true;
        }

        return false;
    }

    /**
     * Check if variable is defined in the file
     */
    private boolean isVariableDefined(PsiFile file, String variableName) {
        boolean inVariablesSection = false;
        String normalizedSearch = normalizeVariableName(variableName);

        for (PsiElement child : file.getChildren()) {
            if (child.getNode() == null) continue;

            // Check if we entered Variables section
            if (child.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER) {
                inVariablesSection = true;
                continue;
            }

            // Check if we left Variables section
            if (inVariablesSection && (
                child.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER)) {
                break;
            }

            // Look for variable definition
            if (inVariablesSection && child.getNode().getElementType() == RobotTokenTypes.VARIABLE) {
                String definedVar = child.getText().trim();
                if (normalizeVariableName(definedVar).equals(normalizedSearch)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Normalize variable name for comparison
     */
    private String normalizeVariableName(String name) {
        return name.toUpperCase().trim();
    }
}

