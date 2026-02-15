package com.jakubjirak.robotframework.execution;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.jakubjirak.robotframework.language.RobotIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RobotRunConfigurationType implements ConfigurationType {
    
    @NotNull
    @Override
    public String getDisplayName() {
        return "Robot Framework";
    }
    
    @Override
    public String getConfigurationTypeDescription() {
        return "Robot Framework test execution configuration";
    }
    
    @Override
    public Icon getIcon() {
        return RobotIcons.FILE;
    }
    
    @NotNull
    @Override
    public String getId() {
        return "ROBOT_FRAMEWORK_RUN_CONFIGURATION";
    }
    
    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new RobotConfigurationFactory(this)};
    }
}
