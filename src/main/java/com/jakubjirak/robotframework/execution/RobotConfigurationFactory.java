package com.jakubjirak.robotframework.execution;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RobotConfigurationFactory extends ConfigurationFactory {
    
    protected RobotConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }
    
    @NotNull
    @Override
    public String getId() {
        return "Robot Framework";
    }
    
    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new RobotRunConfiguration(project, this, "Robot Framework");
    }
}
