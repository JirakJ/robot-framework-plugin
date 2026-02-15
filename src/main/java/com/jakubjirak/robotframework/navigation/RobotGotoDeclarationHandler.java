package com.jakubjirak.robotframework.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.Nullable;

/**
 * Provides "Go to Declaration" functionality for Robot Framework keywords.
 * Allows Ctrl+Click navigation to keyword definitions.
 */
public class RobotGotoDeclarationHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement,
                                                   int offset,
                                                   Editor editor) {
        if (sourceElement == null || sourceElement.getNode() == null) {
            return null;
        }

        // Check if this is a variable reference
        if (sourceElement.getNode().getElementType() == RobotTokenTypes.VARIABLE) {
            String variableName = sourceElement.getText().trim();
            if (!variableName.isEmpty()) {
                // Find variable definition in current file
                PsiFile file = sourceElement.getContainingFile();
                if (file != null) {
                    PsiElement variableDefinition = findVariableDefinition(file, variableName);
                    if (variableDefinition != null) {
                        return new PsiElement[]{variableDefinition};
                    }
                }
            }
        }

        // Check if this is a keyword call
        if (sourceElement.getNode().getElementType() == RobotTokenTypes.KEYWORD ||
            sourceElement.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {

            String keywordName = sourceElement.getText().trim();
            if (keywordName.isEmpty()) {
                return null;
            }

            // Find keyword definition in current file
            PsiFile file = sourceElement.getContainingFile();
            if (file != null) {
                PsiElement keywordDefinition = findKeywordDefinition(file, keywordName);
                if (keywordDefinition != null) {
                    return new PsiElement[]{keywordDefinition};
                }
            }
        }

        return null;
    }

    /**
     * Find keyword definition in the file by searching for keyword name in Keywords section.
     */
    @Nullable
    private PsiElement findKeywordDefinition(PsiFile file, String keywordName) {
        // Search for keyword definitions in the file
        // In Robot Framework, keywords are defined in *** Keywords *** section

        boolean inKeywordsSection = false;
        PsiElement[] children = file.getChildren();

        for (PsiElement child : children) {
            if (child.getNode() == null) continue;

            // Check if we entered Keywords section
            if (child.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                inKeywordsSection = true;
                continue;
            }

            // Check if we left Keywords section
            if (inKeywordsSection && (
                child.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER)) {
                break;
            }

            // Look for keyword definition
            if (inKeywordsSection && child.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
                String text = child.getText().trim();
                if (normalizeKeywordName(text).equals(normalizeKeywordName(keywordName))) {
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * Find variable definition in the file by searching for variable name in Variables section.
     */
    @Nullable
    private PsiElement findVariableDefinition(PsiFile file, String variableName) {
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
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * Normalize keyword name by converting to lowercase and removing spaces/underscores.
     * In Robot Framework, "My Keyword", "my_keyword", "MyKeyword" are all equivalent.
     */
    private String normalizeKeywordName(String name) {
        return name.toLowerCase()
                   .replace(" ", "")
                   .replace("_", "")
                   .replace("-", "");
    }

    /**
     * Normalize variable name for comparison.
     */
    private String normalizeVariableName(String name) {
        return name.toUpperCase().trim();
    }
}

