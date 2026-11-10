/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Runs the test case under the caret with {@code --loglevel DEBUG}, so the
 * Robot Framework log records every keyword argument and return value.
 *
 * <p>This is Robot Framework's own troubleshooting mechanism, not an IDE
 * step-through debugger — Robot Framework has no debug protocol the IDE could
 * attach to.</p>
 */
public class DebugTestAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        RunTestAction.execute(e, true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(RunTestAction.isRobotContext(e));
    }
}
