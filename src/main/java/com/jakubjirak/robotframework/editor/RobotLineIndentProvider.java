package com.jakubjirak.robotframework.editor;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider;
import com.intellij.psi.tree.IElementType;
import com.jakubjirak.robotframework.language.RobotLanguage;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotLineIndentProvider implements LineIndentProvider {

    @Nullable
    @Override
    public String getLineIndent(@NotNull Project project, @NotNull Editor editor,
                                @Nullable Language language, int offset) {
        if (!(language instanceof RobotLanguage)) {
            return null;
        }

        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (file == null) {
            return null;
        }

        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        }
        IElementType elementType = element.getNode().getElementType();

        // No indent for section headers
        if (elementType == RobotTokenTypes.SETTINGS_HEADER ||
            elementType == RobotTokenTypes.VARIABLES_HEADER ||
            elementType == RobotTokenTypes.TEST_CASES_HEADER ||
            elementType == RobotTokenTypes.KEYWORDS_HEADER) {
            return "";
        }

        // Indent for content inside sections (typically 4 spaces or 1 tab)
        PsiElement parent = element.getParent();
        if (parent != null) {
            IElementType parentType = parent.getNode().getElementType();
            if (parentType == RobotTokenTypes.SETTINGS_HEADER ||
                parentType == RobotTokenTypes.VARIABLES_HEADER ||
                parentType == RobotTokenTypes.TEST_CASES_HEADER ||
                parentType == RobotTokenTypes.KEYWORDS_HEADER) {
                return "    "; // 4 spaces
            }
        }

        // Additional indent for control flow blocks
        if (isInsideControlFlow(element)) {
            return "        "; // 8 spaces (2 levels)
        }

        return null; // Use default behavior
    }

    private boolean isInsideControlFlow(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            IElementType type = current.getNode().getElementType();
            if (type == RobotTokenTypes.IF_KEYWORD ||
                type == RobotTokenTypes.TRY_KEYWORD ||
                type == RobotTokenTypes.WHILE_KEYWORD ||
                type == RobotTokenTypes.FOR_KEYWORD) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    @Override
    public boolean isSuitableFor(@Nullable Language language) {
        return language instanceof RobotLanguage;
    }
}

