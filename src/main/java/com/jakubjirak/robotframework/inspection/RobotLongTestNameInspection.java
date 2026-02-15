package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection for detecting too long test case names (readability issue).
 * Suggests shorter, more descriptive names.
 */
public class RobotLongTestNameInspection extends LocalInspectionTool {

    private static final int MAX_TEST_NAME_LENGTH = 80;

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element.getNode() == null) {
                    return;
                }

                // Check if in Test Cases section
                if (!isInTestCasesSection(element)) {
                    return;
                }

                // Check if it's a test case name
                if (element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
                    String testName = element.getText().trim();
                    if (testName.length() > MAX_TEST_NAME_LENGTH) {
                        holder.registerProblem(
                            element,
                            "Test case name is too long (" + testName.length() + " characters). " +
                            "Consider using shorter, more descriptive name (max " + MAX_TEST_NAME_LENGTH + " recommended)."
                        );
                    }
                }
            }
        };
    }

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

