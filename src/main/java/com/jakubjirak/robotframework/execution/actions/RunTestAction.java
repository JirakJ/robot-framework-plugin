/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution.actions;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.execution.RobotRunConfiguration;
import com.jakubjirak.robotframework.language.RobotFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Runs the test case under the caret, or the whole suite when the caret is not
 * inside a test. The configuration is created from the editor context by
 * {@code RobotRunConfigurationProducer}.
 */
public class RunTestAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        execute(e, false);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(isRobotContext(e));
    }

    /** Shared by the DEBUG-log variant of this action. */
    protected static void execute(@NotNull AnActionEvent e, boolean debugLogLevel) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        ConfigurationContext context = ConfigurationContext.getFromContext(e.getDataContext(), e.getPlace());
        RunnerAndConfigurationSettings settings = context.getConfiguration();
        if (settings == null) {
            return;
        }
        if (settings.getConfiguration() instanceof RobotRunConfiguration robotConfiguration) {
            robotConfiguration.setDebugLogLevel(debugLogLevel);
        }
        RunManager.getInstance(project).setTemporaryConfiguration(settings);
        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
    }

    /** {@code true} when the action was invoked on a Robot Framework file. */
    protected static boolean isRobotContext(@NotNull AnActionEvent e) {
        return e.getProject() != null && robotFile(e) != null;
    }

    @Nullable
    private static PsiFile robotFile(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE);
        return file instanceof RobotFile ? file : null;
    }
}
