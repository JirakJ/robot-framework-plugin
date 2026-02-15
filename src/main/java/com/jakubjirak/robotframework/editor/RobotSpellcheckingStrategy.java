package com.jakubjirak.robotframework.editor;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Spellchecking strategy for Robot Framework files.
 * Enables spell checking in comments and documentation strings.
 */
public class RobotSpellcheckingStrategy extends SpellcheckingStrategy {

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        if (element.getNode() == null) {
            return EMPTY_TOKENIZER;
        }

        // Enable spell checking for comments
        if (element.getNode().getElementType() == RobotTokenTypes.COMMENT) {
            return TEXT_TOKENIZER;
        }

        // Enable spell checking for strings (documentation, etc.)
        if (element.getNode().getElementType() == RobotTokenTypes.STRING) {
            return TEXT_TOKENIZER;
        }

        // Enable spell checking for identifiers (test names, keyword names)
        // This helps catch typos in test/keyword names
        if (element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
            String text = element.getText().trim();

            // Only check if it's in Test Cases or Keywords section
            if (isInTestCasesOrKeywordsSection(element)) {
                return TEXT_TOKENIZER;
            }
        }

        return EMPTY_TOKENIZER;
    }

    private boolean isInTestCasesOrKeywordsSection(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            current = current.getPrevSibling();
            if (current != null && current.getNode() != null) {
                if (current.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                    return true;
                }
                if (current.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER) {
                    return false;
                }
            }
        }
        return false;
    }
}

