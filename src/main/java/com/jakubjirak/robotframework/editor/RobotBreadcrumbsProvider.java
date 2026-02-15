package com.jakubjirak.robotframework.editor;

import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import com.jakubjirak.robotframework.language.RobotLanguage;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * Provides breadcrumb navigation for Robot Framework files.
 * Shows the current section and keyword/test case context in the editor's breadcrumb bar.
 */
public class RobotBreadcrumbsProvider implements BreadcrumbsProvider {

    private static final List<String> SECTION_NAMES = Arrays.asList(
        "Settings", "Variables", "Test Cases", "Keywords", "Comments"
    );

    @NotNull
    @Override
    public com.intellij.lang.Language[] getLanguages() {
        return new com.intellij.lang.Language[]{RobotLanguage.INSTANCE};
    }

    @Override
    public boolean acceptElement(@NotNull PsiElement element) {
        if (element.getNode() == null) {
            return false;
        }

        // Accept section headers and keywords/test cases
        return element.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
               element.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
               element.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
               element.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER ||
               (element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER &&
                isKeywordOrTestCase(element));
    }

    @NotNull
    @Override
    public String getElementInfo(@NotNull PsiElement element) {
        if (element.getNode() == null) {
            return "";
        }

        // For section headers, extract section name
        if (element.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER) {
            return "Settings";
        }
        if (element.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER) {
            return "Variables";
        }
        if (element.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER) {
            return "Test Cases";
        }
        if (element.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
            return "Keywords";
        }

        // For keywords/test cases, return the name
        return element.getText().trim();
    }

    @Nullable
    @Override
    public String getElementTooltip(@NotNull PsiElement element) {
        return null; // No tooltip by default
    }

    @Nullable
    @Override
    public Icon getElementIcon(@NotNull PsiElement element) {
        if (element.getNode() == null) {
            return null;
        }

        // Use appropriate icons for different element types
        if (element.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
            element.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
            element.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
            element.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
            return com.intellij.icons.AllIcons.Nodes.Folder;
        }

        if (element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
            // Check if it's a keyword or test case
            if (isInKeywordsSection(element)) {
                return com.intellij.icons.AllIcons.Nodes.Method;
            } else if (isInTestCasesSection(element)) {
                return com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
            }
        }

        return null;
    }

    /**
     * Check if element is a keyword or test case definition
     */
    private boolean isKeywordOrTestCase(PsiElement element) {
        return isInKeywordsSection(element) || isInTestCasesSection(element);
    }

    /**
     * Check if element is in Keywords section
     */
    private boolean isInKeywordsSection(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            current = current.getPrevSibling();
            if (current != null && current.getNode() != null) {
                if (current.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                    return true;
                }
                if (current.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Check if element is in Test Cases section
     */
    private boolean isInTestCasesSection(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            current = current.getPrevSibling();
            if (current != null && current.getNode() != null) {
                if (current.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER) {
                    return true;
                }
                if (current.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                    return false;
                }
            }
        }
        return false;
    }
}

