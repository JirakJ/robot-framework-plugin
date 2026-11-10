/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.jakubjirak.robotframework.analysis.RobotStructure;
import com.jakubjirak.robotframework.language.RobotFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Creates a run configuration from the editor or the project view: running a
 * {@code .robot} file runs the whole suite, running from inside a test case runs
 * just that test.
 */
public class RobotRunConfigurationProducer extends LazyRunConfigurationProducer<RobotRunConfiguration> {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(RobotRunConfigurationType.class)
                .getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull RobotRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        PsiFile file = context.getPsiLocation() == null ? null : context.getPsiLocation().getContainingFile();
        if (!(file instanceof RobotFile)) {
            return false;
        }
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }

        configuration.setSuitePath(virtualFile.getPath());

        RobotStructure.Entry test = testAt(file, context);
        if (test != null) {
            configuration.setTestName(test.getName());
            configuration.setName(test.getName());
        } else {
            configuration.setTestName("");
            configuration.setName(virtualFile.getName());
        }
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull RobotRunConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        PsiFile file = context.getPsiLocation() == null ? null : context.getPsiLocation().getContainingFile();
        if (!(file instanceof RobotFile)) {
            return false;
        }
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null || !virtualFile.getPath().equals(configuration.getSuitePath())) {
            return false;
        }
        RobotStructure.Entry test = testAt(file, context);
        String expected = test == null ? "" : test.getName();
        return Objects.equals(expected, configuration.getTestName());
    }

    @Nullable
    private static RobotStructure.Entry testAt(PsiFile file, ConfigurationContext context) {
        PsiElement location = context.getPsiLocation();
        if (location == null) {
            return null;
        }
        return RobotStructure.testCaseAt(file.getText(), location.getTextRange().getStartOffset());
    }
}
