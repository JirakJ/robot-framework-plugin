package com.jakubjirak.robotframework.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Annotator for detecting duplicate keyword definitions.
 * In Robot Framework, each keyword name must be unique within a file.
 */
public class RobotDuplicateKeywordAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // Only process at file level to check all keywords at once
        if (!(element instanceof PsiFile)) {
            return;
        }

        PsiFile file = (PsiFile) element;
        Map<String, PsiElement> keywordDefinitions = new HashMap<>();
        boolean inKeywordsSection = false;

        for (PsiElement child : file.getChildren()) {
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

            // Check for keyword definitions
            if (inKeywordsSection && child.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
                String keywordName = child.getText().trim();
                if (keywordName.isEmpty()) {
                    continue;
                }

                String normalizedName = normalizeKeywordName(keywordName);

                // Check if we already have this keyword
                if (keywordDefinitions.containsKey(normalizedName)) {
                    // Mark both the original and duplicate as errors
                    PsiElement original = keywordDefinitions.get(normalizedName);

                    holder.newAnnotation(HighlightSeverity.ERROR,
                        "Duplicate keyword definition: '" + keywordName + "'")
                        .range(child)
                        .create();

                    // Also mark the original if we haven't already
                    holder.newAnnotation(HighlightSeverity.ERROR,
                        "Duplicate keyword definition: '" + original.getText().trim() + "'")
                        .range(original)
                        .create();
                } else {
                    keywordDefinitions.put(normalizedName, child);
                }
            }
        }
    }

    /**
     * Normalize keyword name for comparison.
     * In Robot Framework, "My Keyword", "my_keyword", "MyKeyword" are all equivalent.
     */
    private String normalizeKeywordName(String name) {
        return name.toLowerCase()
                   .replace(" ", "")
                   .replace("_", "")
                   .replace("-", "");
    }
}

