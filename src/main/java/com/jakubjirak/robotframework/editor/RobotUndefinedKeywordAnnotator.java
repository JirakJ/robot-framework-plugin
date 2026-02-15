package com.jakubjirak.robotframework.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Annotator for detecting undefined keyword references.
 * Provides warnings for keyword calls that don't have corresponding definitions.
 */
public class RobotUndefinedKeywordAnnotator implements Annotator {

    // Common built-in keywords that should not be flagged as undefined
    private static final Set<String> BUILT_IN_KEYWORDS = new HashSet<>(Arrays.asList(
        "log", "logtoconsole", "logmany", "logvariables",
        "shouldbeequal", "shouldnotbeequal", "shouldcontain", "shouldnotcontain",
        "shouldbetrue", "shouldbefalse", "shouldbeempty", "shouldnotbeempty",
        "setvariable", "setvariableif", "settestvariable", "setsuitevariable", "setglobalvariable",
        "createlist", "createdictionary", "appendtolist", "removefromlist",
        "settodictionary", "getfromdictionary", "removefromdictionary",
        "runkeyword", "runkeywordif", "runkeywordunless", "runkeywords",
        "runkeywordandcontinueonfailure", "runkeywordandexpecterror", "runkeywordandignorerror",
        "waituntilkeywordsucceeds", "repeatkeyword", "runkeywordandreturn",
        "passexecution", "fail", "fatalerror", "skip", "skipif",
        "getlength", "getcount", "lengthshouldbe",
        "converttointeger", "converttonumber", "converttostring", "converttoboolean",
        "sleep", "catenate", "gettime", "evaluate",
        "importlibrary", "importresource", "importvariables",
        "settags", "removetags", "getlibraryinstance", "reloadlibrary",
        "nooperation", "comment", "continueforloop", "exitforloop",
        "returnfromkeyword", "returnfromkeywordif",
        // RF 7+ keywords
        "if", "elseif", "else", "end", "try", "except", "finally",
        "while", "for", "break", "continue", "return", "var"
    ));

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getNode() == null) {
            return;
        }

        // Check if this is a keyword call (not in Keywords section definition)
        if (element.getNode().getElementType() == RobotTokenTypes.KEYWORD ||
            element.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {

            String keywordName = element.getText().trim();
            if (keywordName.isEmpty()) {
                return;
            }

            // Skip if it's a keyword definition (in Keywords section)
            if (isInKeywordsSection(element)) {
                return;
            }

            // Skip built-in keywords
            if (isBuiltInKeyword(keywordName)) {
                return;
            }

            // Check if keyword is defined in current file
            PsiFile file = element.getContainingFile();
            if (file != null && !isKeywordDefined(file, keywordName)) {
                holder.newAnnotation(HighlightSeverity.WARNING,
                    "Undefined keyword: '" + keywordName + "'")
                    .range(element)
                    .create();
            }
        }
    }

    /**
     * Check if element is inside Keywords section (definition, not call)
     */
    private boolean isInKeywordsSection(PsiElement element) {
        PsiElement current = element;
        boolean foundKeywordsHeader = false;

        // Walk backwards to find if we're in Keywords section
        while (current != null) {
            current = current.getPrevSibling();
            if (current != null && current.getNode() != null) {
                if (current.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                    foundKeywordsHeader = true;
                    break;
                }
                // If we hit another section header first, we're not in Keywords section
                if (current.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER) {
                    break;
                }
            }
        }

        return foundKeywordsHeader;
    }

    /**
     * Check if keyword name is a built-in keyword
     */
    private boolean isBuiltInKeyword(String keywordName) {
        String normalized = normalizeKeywordName(keywordName);
        return BUILT_IN_KEYWORDS.contains(normalized);
    }

    /**
     * Check if keyword is defined in the file
     */
    private boolean isKeywordDefined(PsiFile file, String keywordName) {
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

            // Look for keyword definition
            if (inKeywordsSection && child.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
                String text = child.getText().trim();
                if (normalizeKeywordName(text).equals(normalizeKeywordName(keywordName))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Normalize keyword name for comparison
     */
    private String normalizeKeywordName(String name) {
        return name.toLowerCase()
                   .replace(" ", "")
                   .replace("_", "")
                   .replace("-", "");
    }
}

