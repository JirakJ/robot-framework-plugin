/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.execution;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Settings form for {@link RobotRunConfiguration}.
 */
public class RobotRunConfigurationEditor extends SettingsEditor<RobotRunConfiguration> {

    private final Project project;

    private final TextFieldWithBrowseButton suitePathField = new TextFieldWithBrowseButton();
    private final JBTextField testNameField = new JBTextField();
    private final JBTextField includeTagsField = new JBTextField();
    private final JBTextField excludeTagsField = new JBTextField();
    private final TextFieldWithBrowseButton outputDirectoryField = new TextFieldWithBrowseButton();
    private final JBTextField extraArgumentsField = new JBTextField();
    private final TextFieldWithBrowseButton interpreterField = new TextFieldWithBrowseButton();
    private final TextFieldWithBrowseButton workingDirectoryField = new TextFieldWithBrowseButton();
    private final JBCheckBox debugLogLevelCheckBox =
            new JBCheckBox("Run with --loglevel DEBUG");

    private JPanel panel;

    public RobotRunConfigurationEditor(@NotNull Project project) {
        this.project = project;
        browseWith(suitePathField, FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor());
        browseWith(outputDirectoryField, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        browseWith(workingDirectoryField, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        // Explicit descriptor: the factory's single-file helper is deprecated.
        browseWith(interpreterField,
                new FileChooserDescriptor(true, false, false, false, false, false));
    }

    /**
     * Wires the browse button through {@link FileChooser}, which stays on the
     * public API in every supported IDE version.
     */
    private void browseWith(TextFieldWithBrowseButton field, FileChooserDescriptor descriptor) {
        field.addActionListener(event -> {
            VirtualFile chosen = FileChooser.chooseFile(descriptor, project, null);
            if (chosen != null) {
                field.setText(chosen.getPath());
            }
        });
    }

    @Override
    protected void resetEditorFrom(@NotNull RobotRunConfiguration configuration) {
        suitePathField.setText(configuration.getSuitePath());
        testNameField.setText(configuration.getTestName());
        includeTagsField.setText(configuration.getIncludeTags());
        excludeTagsField.setText(configuration.getExcludeTags());
        outputDirectoryField.setText(configuration.getOutputDirectory());
        extraArgumentsField.setText(configuration.getExtraArguments());
        interpreterField.setText(configuration.getInterpreterPath());
        workingDirectoryField.setText(configuration.getWorkingDirectory());
        debugLogLevelCheckBox.setSelected(configuration.isDebugLogLevel());
    }

    @Override
    protected void applyEditorTo(@NotNull RobotRunConfiguration configuration) {
        configuration.setSuitePath(suitePathField.getText().trim());
        configuration.setTestName(testNameField.getText().trim());
        configuration.setIncludeTags(includeTagsField.getText().trim());
        configuration.setExcludeTags(excludeTagsField.getText().trim());
        configuration.setOutputDirectory(outputDirectoryField.getText().trim());
        configuration.setExtraArguments(extraArgumentsField.getText().trim());
        configuration.setInterpreterPath(interpreterField.getText().trim());
        configuration.setWorkingDirectory(workingDirectoryField.getText().trim());
        configuration.setDebugLogLevel(debugLogLevelCheckBox.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        if (panel == null) {
            panel = FormBuilder.createFormBuilder()
                    .addLabeledComponent(new JBLabel("Test suite (file or directory):"), suitePathField, 1, false)
                    .addLabeledComponent(new JBLabel("Test name (--test):"), testNameField, 1, false)
                    .addLabeledComponent(new JBLabel("Include tags (--include):"), includeTagsField, 1, false)
                    .addLabeledComponent(new JBLabel("Exclude tags (--exclude):"), excludeTagsField, 1, false)
                    .addLabeledComponent(new JBLabel("Output directory (--outputdir):"), outputDirectoryField, 1, false)
                    .addLabeledComponent(new JBLabel("Additional arguments:"), extraArgumentsField, 1, false)
                    .addComponent(debugLogLevelCheckBox, 1)
                    .addSeparator()
                    .addLabeledComponent(new JBLabel("Python interpreter:"), interpreterField, 1, false)
                    .addLabeledComponent(new JBLabel("Working directory:"), workingDirectoryField, 1, false)
                    .addComponentFillVertically(new JPanel(), 0)
                    .getPanel();
        }
        return panel;
    }
}
