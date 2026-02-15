package com.jakubjirak.robotframework.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class RobotProjectListener implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        // Initialize Robot Framework services when project opens
        RobotProjectService.getInstance(project);
    }
}
