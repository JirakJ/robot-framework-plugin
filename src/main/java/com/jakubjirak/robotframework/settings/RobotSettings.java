/*
 * Copyright (c) 2024-2025 Jakub Jirak. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent project-level settings for the Robot Framework plugin.
 * Stores configuration for the Python interpreter, test execution, and web inspector.
 */
@State(name = "RobotSettings", storages = @Storage("robot-framework-settings.xml"))
public class RobotSettings implements PersistentStateComponent<RobotSettings.State> {

    private State myState = new State();

    public static class State {
        public String interpreterPath = "";
        public String robotArgs = "";
        public String outputDirectory = "";
        public String browserName = "chrome";
        public boolean enableAutoImport = true;
        public int formatterIndentSize = 4;
        public boolean formatterNormalizeHeaders = true;
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public String getInterpreterPath() {
        return myState.interpreterPath;
    }

    public void setInterpreterPath(String value) {
        myState.interpreterPath = value;
    }

    public String getRobotArgs() {
        return myState.robotArgs;
    }

    public void setRobotArgs(String value) {
        myState.robotArgs = value;
    }

    public String getOutputDirectory() {
        return myState.outputDirectory;
    }

    public void setOutputDirectory(String value) {
        myState.outputDirectory = value;
    }

    public String getBrowserName() {
        return myState.browserName;
    }

    public void setBrowserName(String value) {
        myState.browserName = value;
    }

    public boolean isEnableAutoImport() {
        return myState.enableAutoImport;
    }

    public void setEnableAutoImport(boolean value) {
        myState.enableAutoImport = value;
    }

    public int getFormatterIndentSize() {
        return myState.formatterIndentSize;
    }

    public void setFormatterIndentSize(int value) {
        myState.formatterIndentSize = Math.max(2, value);
    }

    public boolean isFormatterNormalizeHeaders() {
        return myState.formatterNormalizeHeaders;
    }

    public void setFormatterNormalizeHeaders(boolean value) {
        myState.formatterNormalizeHeaders = value;
    }

    public static RobotSettings getInstance(@NotNull Project project) {
        return project.getService(RobotSettings.class);
    }
}
