package com.jakubjirak.robotframework.editor;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = new BracePair[]{
        // Variables: ${var}, @{list}, &{dict}, %{env}
        new BracePair(RobotTokenTypes.VARIABLE, RobotTokenTypes.VARIABLE, false),
        // Settings: [Documentation], [Tags], etc.
        new BracePair(RobotTokenTypes.SETTING, RobotTokenTypes.SETTING, false)
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
