package com.jakubjirak.robotframework.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Enables rename refactoring for Robot Framework keywords.
 * Allows users to rename keywords and automatically update all references.
 */
public class RobotKeywordRenameProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        if (element.getNode() == null) {
            return false;
        }

        // Allow renaming of keywords in Keywords section
        if (element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
            return isInKeywordsSection(element);
        }

        return false;
    }

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
}

