package com.jakubjirak.robotframework.editor;

import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.language.RobotFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides custom icons for Robot Framework files in Project View.
 */
public class RobotIconProvider extends IconProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull PsiElement element, int flags) {
        if (element instanceof PsiFile) {
            PsiFile file = (PsiFile) element;

            // Check if it's a Robot Framework file
            if (file.getFileType() == RobotFileType.INSTANCE) {
                // Use different icons for .robot and .resource files
                String fileName = file.getName().toLowerCase();

                if (fileName.endsWith(".resource")) {
                    return com.intellij.icons.AllIcons.Nodes.ResourceBundle;
                } else if (fileName.endsWith(".robot")) {
                    return com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
                } else if (fileName.endsWith(".txt")) {
                    return com.intellij.icons.AllIcons.FileTypes.Text;
                }
            }
        }

        return null;
    }
}

