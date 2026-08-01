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

        // Control structures document themselves.
        if (RobotTokenTypes.CONTROL_FLOW.contains(element.getNode().getElementType())) {
            String marker = element.getText().trim().toUpperCase(java.util.Locale.ROOT)
                    .replaceAll("\\s+", " ");
            String doc = getControlStructureDocumentation(marker);
            if (doc != null) {
                return formatDocumentation(marker, doc);
            }
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

            if (inKeywordsSection
                    && RobotTokenTypes.SECTION_HEADERS.contains(child.getNode().getElementType())) {
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

    /** Short reference for the control structures available up to Robot Framework 7.4. */
    @Nullable
    private String getControlStructureDocumentation(String marker) {
        switch (marker) {
            case "IF":
                return "Runs the following steps when the condition is true. Written on a single row "
                        + "(<code>IF&nbsp;&nbsp;&nbsp;&nbsp;condition&nbsp;&nbsp;&nbsp;&nbsp;Keyword</code>) "
                        + "it is an inline IF and needs no END. Since Robot Framework 7.0.";
            case "ELSE IF":
                return "Alternative condition of an IF block.";
            case "ELSE":
                return "Steps to run when no IF or ELSE IF condition matched.";
            case "FOR":
                return "Loops over values. Separators: IN, IN RANGE, IN ENUMERATE and IN ZIP. "
                        + "IN ZIP takes an explicit <code>mode=</code> since Robot Framework 7.0.";
            case "WHILE":
                return "Loops while the condition is true. Accepts <code>limit=</code> (a count such as "
                        + "<code>10x</code> or a time such as <code>1 minute</code>), <code>on_limit=</code> "
                        + "and <code>on_limit_message=</code>.";
            case "GROUP":
                return "Groups related steps under an optional name so logs and reports stay readable. "
                        + "Groups nest and share the enclosing variable scope. Closed with END. "
                        + "Added in Robot Framework 7.2.";
            case "TRY":
                return "Starts error handling. Followed by EXCEPT, ELSE and FINALLY branches.";
            case "EXCEPT":
                return "Handles a failure raised in the TRY block. Supports matching patterns and "
                        + "<code>AS&nbsp;&nbsp;&nbsp;&nbsp;${error}</code> to capture the message.";
            case "FINALLY":
                return "Steps that always run, whether or not the TRY block failed.";
            case "END":
                return "Closes an IF, FOR, WHILE, TRY or GROUP block.";
            case "BREAK":
                return "Leaves the innermost FOR or WHILE loop.";
            case "CONTINUE":
                return "Jumps to the next iteration of the innermost FOR or WHILE loop.";
            case "RETURN":
                return "Returns from a user keyword, optionally with values. Replaces the deprecated "
                        + "<code>[Return]</code> setting.";
            case "VAR":
                return "Creates a variable. <code>scope=</code> accepts LOCAL, TEST, TASK, SUITE, "
                        + "SUITES (Robot Framework 7.1) and GLOBAL; <code>separator=</code> controls how "
                        + "multiple values are joined. Types can be declared as "
                        + "<code>VAR&nbsp;&nbsp;&nbsp;&nbsp;${count: int}&nbsp;&nbsp;&nbsp;&nbsp;42</code> "
                        + "since Robot Framework 7.3. Added in Robot Framework 7.0.";
            case "IN":
            case "IN RANGE":
            case "IN ENUMERATE":
            case "IN ZIP":
                return "FOR loop separator.";
            default:
                return null;
        }
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

