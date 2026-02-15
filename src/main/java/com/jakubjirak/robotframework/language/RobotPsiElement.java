package com.jakubjirak.robotframework.language;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class RobotPsiElement extends ASTWrapperPsiElement {
    public RobotPsiElement(@NotNull ASTNode node) {
        super(node);
    }
}
