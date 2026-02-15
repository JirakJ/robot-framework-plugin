package com.jakubjirak.robotframework.editor;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RobotStructureViewElement implements StructureViewTreeElement {
    private final PsiElement element;

    public RobotStructureViewElement(PsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (element instanceof NavigationItem) {
            ((NavigationItem) element).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return element instanceof NavigationItem &&
               ((NavigationItem) element).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element instanceof NavigationItem &&
               ((NavigationItem) element).canNavigateToSource();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        if (element instanceof PsiFile) {
            return new ItemPresentation() {
                @Nullable
                @Override
                public String getPresentableText() {
                    return ((PsiFile) element).getName();
                }

                @Nullable
                @Override
                public String getLocationString() {
                    return null;
                }

                @Nullable
                @Override
                public Icon getIcon(boolean unused) {
                    return element.getIcon(0);
                }
            };
        }

        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                String text = element.getText();
                if (text.length() > 50) {
                    text = text.substring(0, 50) + "...";
                }
                return text;
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                if (element.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER) {
                    return com.intellij.icons.AllIcons.Nodes.TestSourceFolder;
                }
                if (element.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                    return com.intellij.icons.AllIcons.Nodes.Method;
                }
                if (element.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER) {
                    return com.intellij.icons.AllIcons.General.Settings;
                }
                if (element.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER) {
                    return com.intellij.icons.AllIcons.Nodes.Variable;
                }
                return com.intellij.icons.AllIcons.Nodes.Tag;
            }
        };
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        if (element instanceof PsiFile) {
            List<TreeElement> children = new ArrayList<>();

            // Find all section headers
            Collection<PsiElement> sections = PsiTreeUtil.findChildrenOfAnyType(
                element,
                PsiElement.class
            );

            for (PsiElement section : sections) {
                if (section.getNode() != null && (
                    section.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                    section.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                    section.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
                    section.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER)) {
                    children.add(new RobotStructureViewElement(section));
                }
            }

            return children.toArray(new TreeElement[0]);
        }

        return EMPTY_ARRAY;
    }
}

