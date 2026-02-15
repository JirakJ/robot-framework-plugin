package com.jakubjirak.robotframework.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotRunConfiguration extends RunConfigurationBase<RobotRunConfigurationOptions> {
    
    protected RobotRunConfiguration(@NotNull Project project,
                                   @NotNull ConfigurationFactory factory,
                                   @Nullable String name) {
        super(project, factory, name);
    }
    
    @NotNull
    @Override
    protected RobotRunConfigurationOptions getOptions() {
        return (RobotRunConfigurationOptions) super.getOptions();
    }
    
    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new RobotRunConfigurationEditor();
    }
    
    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor,
                                   @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new RobotCommandLineState(environment);
    }
}
