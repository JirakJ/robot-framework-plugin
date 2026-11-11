/*
 * Copyright (c) 2024-2025 Jakub Jirak.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * Project-level configurable for the Robot Framework plugin.
 * Provides a settings UI under Settings &gt; Tools &gt; Robot Framework.
 */
public class RobotSettingsConfigurable implements SearchableConfigurable {

    private final Project project;
    private JBTextField interpreterPathField;
    private JBTextField robotArgsField;
    private JBTextField outputDirectoryField;
    private JBTextField formatterIndentSizeField;
    private JBCheckBox formatterNormalizeHeadersCheckBox;
    private JPanel myPanel;

    public RobotSettingsConfigurable(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "robot.framework.settings";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Robot Framework";
    }

    @Override
    public @Nullable JComponent createComponent() {
        interpreterPathField = new JBTextField();
        robotArgsField = new JBTextField();
        outputDirectoryField = new JBTextField();
        formatterIndentSizeField = new JBTextField();
        formatterNormalizeHeadersCheckBox = new JBCheckBox("Normalize section headers (e.g. *** test cases *** → *** Test Cases ***)");

        myPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Python interpreter path:"), interpreterPathField, 1, false)
                .addLabeledComponent(new JBLabel("Robot Framework arguments:"), robotArgsField, 1, false)
                .addLabeledComponent(new JBLabel("Output directory:"), outputDirectoryField, 1, false)
                .addSeparator()
                .addComponent(new JBLabel("Formatter"))
                .addLabeledComponent(new JBLabel("Indent size (spaces):"), formatterIndentSizeField, 1, false)
                .addComponent(formatterNormalizeHeadersCheckBox, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        reset();
        return myPanel;
    }

    @Override
    public boolean isModified() {
        RobotSettings settings = RobotSettings.getInstance(project);
        return !Objects.equals(interpreterPathField.getText(), settings.getInterpreterPath()) ||
                !Objects.equals(robotArgsField.getText(), settings.getRobotArgs()) ||
                !Objects.equals(outputDirectoryField.getText(), settings.getOutputDirectory()) ||
                parseIndentSize() != settings.getFormatterIndentSize() ||
                formatterNormalizeHeadersCheckBox.isSelected() != settings.isFormatterNormalizeHeaders();
    }

    private int parseIndentSize() {
        return parseIndentSize(formatterIndentSizeField.getText(), RobotSettings.getInstance(project).getFormatterIndentSize());
    }

    /**
     * Parses the indent-size field. Values below 2 are raised to 2 (Robot Framework needs at least two
     * spaces as a cell separator); non-numeric input keeps {@code fallback}.
     */
    static int parseIndentSize(@Nullable String text, int fallback) {
        if (text == null) {
            return fallback;
        }
        try {
            return Math.max(2, Integer.parseInt(text.trim()));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @Override
    public void apply() {
        RobotSettings settings = RobotSettings.getInstance(project);
        settings.setInterpreterPath(interpreterPathField.getText());
        settings.setRobotArgs(robotArgsField.getText());
        settings.setOutputDirectory(outputDirectoryField.getText());
        settings.setFormatterIndentSize(parseIndentSize());
        settings.setFormatterNormalizeHeaders(formatterNormalizeHeadersCheckBox.isSelected());
    }

    @Override
    public void reset() {
        RobotSettings settings = RobotSettings.getInstance(project);
        interpreterPathField.setText(settings.getInterpreterPath());
        robotArgsField.setText(settings.getRobotArgs());
        outputDirectoryField.setText(settings.getOutputDirectory());
        formatterIndentSizeField.setText(String.valueOf(settings.getFormatterIndentSize()));
        formatterNormalizeHeadersCheckBox.setSelected(settings.isFormatterNormalizeHeaders());
    }
}
