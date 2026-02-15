package com.jakubjirak.robotframework.execution;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RobotRunConfigurationEditor extends SettingsEditor<RobotRunConfiguration> {
    
    private JPanel panel;
    
    @Override
    protected void resetEditorFrom(@NotNull RobotRunConfiguration configuration) {
        // Reset editor from configuration
    }
    
    @Override
    protected void applyEditorTo(@NotNull RobotRunConfiguration configuration) throws ConfigurationException {
        // Apply editor to configuration
    }
    
    @NotNull
    @Override
    protected JComponent createEditor() {
        if (panel == null) {
            panel = new JPanel();
            panel.add(new JLabel("Robot Framework Test Configuration"));
        }
        return panel;
    }
}
