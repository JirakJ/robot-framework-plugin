package com.jakubjirak.robotframework.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Annotator for detecting missing resource and library imports.
 * Provides warnings when imported resources/libraries cannot be found.
 */
public class RobotMissingResourceAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getNode() == null) {
            return;
        }

        // Only check in Settings section
        if (!isInSettingsSection(element)) {
            return;
        }

        // Check if this is a Resource or Library import
        PsiElement prevSibling = element.getPrevSibling();
        if (prevSibling != null && prevSibling.getText() != null) {
            String prevText = prevSibling.getText().trim();

            if (prevText.equalsIgnoreCase("Resource")) {
                checkResourceExists(element, holder);
            } else if (prevText.equalsIgnoreCase("Library")) {
                checkLibraryExists(element, holder);
            }
        }
    }

    /**
     * Check if imported resource file exists in project
     */
    private void checkResourceExists(PsiElement element, AnnotationHolder holder) {
        String resourcePath = element.getText().trim();
        if (resourcePath.isEmpty()) {
            return;
        }

        // Remove quotes if present
        resourcePath = resourcePath.replaceAll("^\"|\"$", "");

        // Skip if it's a variable reference
        if (resourcePath.contains("${") || resourcePath.contains("%{")) {
            return;
        }

        PsiFile file = element.getContainingFile();
        if (file == null) {
            return;
        }

        Project project = file.getProject();
        VirtualFile currentFile = file.getVirtualFile();

        if (currentFile == null) {
            return;
        }

        // Try to find resource file
        boolean found = false;

        // Extract filename from path
        String filename = resourcePath;
        if (resourcePath.contains("/")) {
            String[] parts = resourcePath.split("/");
            filename = parts[parts.length - 1];
        } else if (resourcePath.contains("\\")) {
            String[] parts = resourcePath.split("\\\\");
            filename = parts[parts.length - 1];
        }

        // Search for .robot or .resource files with this name
        Collection<VirtualFile> virtualFiles = FilenameIndex.getVirtualFilesByName(
            filename,
            GlobalSearchScope.projectScope(project)
        );

        if (!virtualFiles.isEmpty()) {
            found = true;
        }

        // Also try with .robot extension if not present
        if (!found && !filename.endsWith(".robot") && !filename.endsWith(".resource")) {
            virtualFiles = FilenameIndex.getVirtualFilesByName(
                filename + ".robot",
                GlobalSearchScope.projectScope(project)
            );
            if (!virtualFiles.isEmpty()) {
                found = true;
            }

            if (!found) {
                virtualFiles = FilenameIndex.getVirtualFilesByName(
                    filename + ".resource",
                    GlobalSearchScope.projectScope(project)
                );
                if (!virtualFiles.isEmpty()) {
                    found = true;
                }
            }
        }

        if (!found) {
            holder.newAnnotation(HighlightSeverity.WARNING,
                "Resource file not found: '" + resourcePath + "'")
                .range(element)
                .create();
        }
    }

    /**
     * Check if library exists (basic check for common libraries)
     */
    private void checkLibraryExists(PsiElement element, AnnotationHolder holder) {
        String libraryName = element.getText().trim();
        if (libraryName.isEmpty()) {
            return;
        }

        // Remove quotes if present
        libraryName = libraryName.replaceAll("^\"|\"$", "");

        // Skip variable references
        if (libraryName.contains("${") || libraryName.contains("%{")) {
            return;
        }

        // Skip common built-in and standard libraries
        if (isCommonLibrary(libraryName)) {
            return;
        }

        // For now, we don't warn about unknown libraries since they might be Python libraries
        // This would require more sophisticated checks
    }

    /**
     * Check if library is a common/standard Robot Framework library
     */
    private boolean isCommonLibrary(String libraryName) {
        String lower = libraryName.toLowerCase();
        return lower.equals("builtin") ||
               lower.equals("collections") ||
               lower.equals("string") ||
               lower.equals("operatingsystem") ||
               lower.equals("datetime") ||
               lower.equals("dialogs") ||
               lower.equals("process") ||
               lower.equals("screenshot") ||
               lower.equals("telnet") ||
               lower.equals("xml") ||
               lower.startsWith("seleniumlibrary") ||
               lower.startsWith("requestslibrary") ||
               lower.startsWith("sshlibrary") ||
               lower.startsWith("databaselibrary");
    }

    /**
     * Check if element is in Settings section
     */
    private boolean isInSettingsSection(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            current = current.getPrevSibling();
            if (current != null && current.getNode() != null) {
                if (current.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER) {
                    return true;
                }
                if (current.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
                    current.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                    return false;
                }
            }
        }
        return false;
    }
}

