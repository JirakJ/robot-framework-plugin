package com.jakubjirak.robotframework.editor;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.Nullable;

/**
 * Provides documentation for Robot Framework keywords on hover.
 * Shows keyword documentation when hovering over keyword calls.
 */
public class RobotDocumentationProvider extends AbstractDocumentationProvider {

    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element == null || element.getNode() == null) {
            return null;
        }

        // Check if this is a keyword
        if (element.getNode().getElementType() == RobotTokenTypes.KEYWORD ||
            element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {

            String keywordName = element.getText().trim();

            // Try to find keyword definition
            PsiFile file = element.getContainingFile();
            if (file != null) {
                String documentation = findKeywordDocumentation(file, keywordName);
                if (documentation != null) {
                    return formatDocumentation(keywordName, documentation);
                }
            }

            // Provide basic documentation for built-in keywords
            String builtInDoc = getBuiltInKeywordDocumentation(keywordName);
            if (builtInDoc != null) {
                return formatDocumentation(keywordName, builtInDoc);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element == null || element.getNode() == null) {
            return null;
        }

        if (element.getNode().getElementType() == RobotTokenTypes.KEYWORD ||
            element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
            return "Robot Framework Keyword: " + element.getText().trim();
        }

        return null;
    }

    private String findKeywordDocumentation(PsiFile file, String keywordName) {
        boolean inKeywordsSection = false;
        boolean foundKeyword = false;
        String normalizedSearch = normalizeKeywordName(keywordName);

        for (PsiElement child : file.getChildren()) {
            if (child.getNode() == null) continue;

            // Check section headers
            if (child.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                inKeywordsSection = true;
                continue;
            }

            if (inKeywordsSection && (
                child.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER)) {
                break;
            }

            // Look for keyword definition
            if (inKeywordsSection && child.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
                String text = child.getText().trim();
                if (normalizeKeywordName(text).equals(normalizedSearch)) {
                    foundKeyword = true;
                    continue;
                }
            }

            // Look for [Documentation] after keyword name
            if (foundKeyword && child.getText().trim().equalsIgnoreCase("[Documentation]")) {
                PsiElement next = child.getNextSibling();
                if (next != null) {
                    return next.getText().trim();
                }
            }
        }

        return null;
    }

    private String normalizeKeywordName(String name) {
        return name.toLowerCase().replace(" ", "").replace("_", "").replace("-", "");
    }

    private String getBuiltInKeywordDocumentation(String keywordName) {
        String normalized = normalizeKeywordName(keywordName);

        switch (normalized) {
            case "log":
                return "Logs the given message with the given level.";
            case "logtoconsole":
                return "Logs the given message to the console.";
            case "shouldbeequal":
                return "Fails if the given objects are unequal.";
            case "shouldcontain":
                return "Fails if container does not contain item.";
            case "setvariable":
                return "Returns the given values which can then be assigned to a variables.";
            case "sleep":
                return "Pauses the test executed for the given time.";
            default:
                return null;
        }
    }

    private String formatDocumentation(String keywordName, String documentation) {
        return "<html><body>" +
               "<h3>" + keywordName + "</h3>" +
               "<p>" + documentation + "</p>" +
               "</body></html>";
    }
}

