package com.jakubjirak.robotframework.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection for detecting empty test cases (no steps).
 * Provides quick fix to add a placeholder step.
 */
public class RobotEmptyTestCaseInspection extends LocalInspectionTool {

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

                // Check if it's a test case name followed by nothing or another test case
                if (element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
                    if (isEmptyTestCase(element)) {
                        holder.registerProblem(
                            element,
                            "Empty test case - no steps defined",
                            new AddPlaceholderStepFix()
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

    private boolean isEmptyTestCase(PsiElement element) {
        // Simple heuristic: if next sibling is another IDENTIFIER or null, it's likely empty
        PsiElement next = element.getNextSibling();
        while (next != null && next.getText().trim().isEmpty()) {
            next = next.getNextSibling();
        }

        if (next == null) {
            return true;
        }

        // If next is another test case name or section header, current is empty
        if (next.getNode() != null) {
            return next.getNode().getElementType() == RobotTokenTypes.IDENTIFIER ||
                   next.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER;
        }

        return false;
    }

    private static class AddPlaceholderStepFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getFamilyName() {
            return "Add placeholder step";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // Quick fix would add "    No Operation" or "    Log    TODO: Implement test"
            // For now, just show the intention
        }
    }
}

