package com.jakubjirak.robotframework.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Annotator for detecting and highlighting syntax errors in Robot Framework files.
 */
public class RobotSyntaxErrorAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getNode() == null) {
            return;
        }

        // Check for common syntax errors
        checkMissingSectionHeader(element, holder);
        checkInvalidKeywordStructure(element, holder);
        checkUnmatchedControlFlow(element, holder);
    }

    /**
     * Check if content appears before any section header
     */
    private void checkMissingSectionHeader(PsiElement element, AnnotationHolder holder) {
        // Check if this is content that should be in a section
        if (element.getNode().getElementType() == RobotTokenTypes.KEYWORD ||
            element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
            
            // Check if we're in any section
            PsiElement parent = element.getParent();
            boolean inSection = false;
            
            if (parent != null) {
                // Look for section headers before this element
                PsiElement sibling = parent.getFirstChild();
                while (sibling != null && sibling != element) {
                    if (sibling.getNode() != null && (
                        sibling.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                        sibling.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                        sibling.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
                        sibling.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER)) {
                        inSection = true;
                        break;
                    }
                    sibling = sibling.getNextSibling();
                }
            }
            
            if (!inSection && !element.getText().trim().isEmpty()) {
                holder.newAnnotation(HighlightSeverity.ERROR, 
                    "Content must be inside a section (*** Settings ***, *** Variables ***, etc.)")
                    .range(element)
                    .create();
            }
        }
    }

    /**
     * Check for invalid keyword structure (e.g., missing indentation)
     */
    private void checkInvalidKeywordStructure(PsiElement element, AnnotationHolder holder) {
        // This is a simplified check - in real implementation, you'd want more sophisticated logic
        if (element.getNode().getElementType() == RobotTokenTypes.KEYWORD) {
            String text = element.getText();
            
            // Check for empty keyword name
            if (text.trim().isEmpty()) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Keyword name cannot be empty")
                    .range(element)
                    .create();
            }
        }
    }

    /**
     * Check for unmatched control flow keywords (IF without END, etc.)
     */
    private void checkUnmatchedControlFlow(PsiElement element, AnnotationHolder holder) {
        if (element.getNode() == null) return;

        // Check for IF without matching END
        if (element.getNode().getElementType() == RobotTokenTypes.IF_KEYWORD ||
            element.getNode().getElementType() == RobotTokenTypes.TRY_KEYWORD ||
            element.getNode().getElementType() == RobotTokenTypes.WHILE_KEYWORD ||
            element.getNode().getElementType() == RobotTokenTypes.FOR_KEYWORD) {
            
            if (!hasMatchingEnd(element)) {
                String keyword = element.getText().toUpperCase();
                holder.newAnnotation(HighlightSeverity.ERROR, 
                    keyword + " block must be closed with END")
                    .range(element)
                    .create();
            }
        }

        // Check for orphaned END
        if (element.getNode().getElementType() == RobotTokenTypes.END_KEYWORD) {
            if (!hasMatchingStart(element)) {
                holder.newAnnotation(HighlightSeverity.ERROR, 
                    "END without matching IF/TRY/WHILE/FOR")
                    .range(element)
                    .create();
            }
        }
    }

    /**
     * Check if control flow keyword has matching END
     */
    private boolean hasMatchingEnd(PsiElement startElement) {
        PsiElement sibling = startElement.getNextSibling();
        int depth = 1;

        while (sibling != null) {
            if (sibling.getNode() != null) {
                // Increment depth for nested control flow
                if (sibling.getNode().getElementType() == RobotTokenTypes.IF_KEYWORD ||
                    sibling.getNode().getElementType() == RobotTokenTypes.TRY_KEYWORD ||
                    sibling.getNode().getElementType() == RobotTokenTypes.WHILE_KEYWORD ||
                    sibling.getNode().getElementType() == RobotTokenTypes.FOR_KEYWORD) {
                    depth++;
                }
                
                // Decrement depth for END
                if (sibling.getNode().getElementType() == RobotTokenTypes.END_KEYWORD) {
                    depth--;
                    if (depth == 0) {
                        return true;
                    }
                }
            }
            sibling = sibling.getNextSibling();
        }

        return false;
    }

    /**
     * Check if END has matching start
     */
    private boolean hasMatchingStart(PsiElement endElement) {
        PsiElement sibling = endElement.getPrevSibling();
        int depth = 1;

        while (sibling != null) {
            if (sibling.getNode() != null) {
                // Increment depth for END
                if (sibling.getNode().getElementType() == RobotTokenTypes.END_KEYWORD) {
                    depth++;
                }
                
                // Decrement depth for control flow start
                if (sibling.getNode().getElementType() == RobotTokenTypes.IF_KEYWORD ||
                    sibling.getNode().getElementType() == RobotTokenTypes.TRY_KEYWORD ||
                    sibling.getNode().getElementType() == RobotTokenTypes.WHILE_KEYWORD ||
                    sibling.getNode().getElementType() == RobotTokenTypes.FOR_KEYWORD) {
                    depth--;
                    if (depth == 0) {
                        return true;
                    }
                }
            }
            sibling = sibling.getPrevSibling();
        }

        return false;
    }
}

