package com.jakubjirak.robotframework.language;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class RobotFile extends PsiFileBase {
    public RobotFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RobotLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return RobotFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Robot Framework File";
    }
}
