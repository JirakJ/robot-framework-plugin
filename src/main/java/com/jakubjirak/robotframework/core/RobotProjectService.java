package com.jakubjirak.robotframework.core;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RobotProjectService {
    
    private final Project project;
    
    public RobotProjectService(@NotNull Project project) {
        this.project = project;
    }
    
    public static RobotProjectService getInstance(@NotNull Project project) {
        return project.getService(RobotProjectService.class);
    }
    
    public Project getProject() {
        return project;
    }
    
    public boolean isRobotFrameworkProject() {
        // Check if project contains .robot or .resource files
        // TODO: Implement proper detection logic
        return true;
    }
    
    public String getRobotFrameworkVersion() {
        // TODO: Detect RF version from Python environment
        return "7.0";
    }
}
