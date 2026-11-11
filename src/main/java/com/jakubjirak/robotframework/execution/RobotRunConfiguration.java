/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.jakubjirak.robotframework.settings.RobotSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Run configuration that executes a Robot Framework suite, a directory of
 * suites, or a single test case.
 */
public class RobotRunConfiguration extends RunConfigurationBase<RunConfigurationOptions> {

    private static final String SUITE_PATH = "suitePath";
    private static final String TEST_NAME = "testName";
    private static final String INCLUDE_TAGS = "includeTags";
    private static final String EXCLUDE_TAGS = "excludeTags";
    private static final String OUTPUT_DIRECTORY = "outputDirectory";
    private static final String EXTRA_ARGUMENTS = "extraArguments";
    private static final String INTERPRETER_PATH = "interpreterPath";
    private static final String WORKING_DIRECTORY = "workingDirectory";
    private static final String DEBUG_LOG_LEVEL = "debugLogLevel";

    private String suitePath = "";
    private String testName = "";
    private String includeTags = "";
    private String excludeTags = "";
    private String outputDirectory = "";
    private String extraArguments = "";
    private String interpreterPath = "";
    private String workingDirectory = "";
    private boolean debugLogLevel;

    protected RobotRunConfiguration(@NotNull Project project,
                                    @NotNull ConfigurationFactory factory,
                                    @Nullable String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new RobotRunConfigurationEditor(getProject());
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor,
                                    @NotNull ExecutionEnvironment environment) {
        return new RobotCommandLineState(environment, this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        String error = validationError(suitePath, workingDirectory, interpreterPath);
        if (error != null) {
            throw new RuntimeConfigurationError(error);
        }
    }

    /**
     * Returns the first problem with the given paths, or {@code null} when the configuration can run.
     * Empty working directory / interpreter mean "use the default" and are valid.
     */
    @Nullable
    static String validationError(String suitePath, String workingDirectory, String interpreterPath) {
        if (suitePath.isEmpty()) {
            return "Set the test suite file or directory to run.";
        }
        if (!exists(suitePath)) {
            return "Test suite not found: " + suitePath;
        }
        if (!workingDirectory.isEmpty() && !exists(workingDirectory)) {
            return "Working directory not found: " + workingDirectory;
        }
        if (!interpreterPath.isEmpty() && !exists(interpreterPath)) {
            return "Python interpreter not found: " + interpreterPath;
        }
        return null;
    }

    /** The configuration's own value when set, otherwise the project-level default. */
    static String orDefault(String explicit, String fallback) {
        return explicit.isEmpty() ? fallback : explicit;
    }

    private static boolean exists(String path) {
        try {
            return Files.exists(Path.of(path));
        } catch (InvalidPathException e) {
            return false;
        }
    }

    /** Command line parameters for this configuration, with project settings as defaults. */
    public RobotCommandBuilder.Params toParams() {
        RobotSettings settings = RobotSettings.getInstance(getProject());
        String interpreter = orDefault(interpreterPath, settings.getInterpreterPath());
        String output = orDefault(outputDirectory, settings.getOutputDirectory());
        String extra = orDefault(extraArguments, settings.getRobotArgs());

        return new RobotCommandBuilder.Params()
                .interpreterPath(interpreter)
                .suitePath(suitePath)
                .testName(testName)
                .includeTags(includeTags)
                .excludeTags(excludeTags)
                .outputDirectory(output)
                .extraArguments(extra)
                .debugLogLevel(debugLogLevel);
    }

    /** Directory the process starts in: the explicit setting, else the project root. */
    public String resolveWorkingDirectory() {
        if (!workingDirectory.isEmpty()) {
            return workingDirectory;
        }
        String basePath = getProject().getBasePath();
        return basePath != null ? basePath : System.getProperty("user.dir");
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        suitePath = read(element, SUITE_PATH);
        testName = read(element, TEST_NAME);
        includeTags = read(element, INCLUDE_TAGS);
        excludeTags = read(element, EXCLUDE_TAGS);
        outputDirectory = read(element, OUTPUT_DIRECTORY);
        extraArguments = read(element, EXTRA_ARGUMENTS);
        interpreterPath = read(element, INTERPRETER_PATH);
        workingDirectory = read(element, WORKING_DIRECTORY);
        debugLogLevel = Boolean.parseBoolean(read(element, DEBUG_LOG_LEVEL));
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        write(element, SUITE_PATH, suitePath);
        write(element, TEST_NAME, testName);
        write(element, INCLUDE_TAGS, includeTags);
        write(element, EXCLUDE_TAGS, excludeTags);
        write(element, OUTPUT_DIRECTORY, outputDirectory);
        write(element, EXTRA_ARGUMENTS, extraArguments);
        write(element, INTERPRETER_PATH, interpreterPath);
        write(element, WORKING_DIRECTORY, workingDirectory);
        write(element, DEBUG_LOG_LEVEL, Boolean.toString(debugLogLevel));
    }

    private static String read(Element element, String name) {
        String value = JDOMExternalizerUtil.readField(element, name);
        return value == null ? "" : value;
    }

    private static void write(Element element, String name, String value) {
        if (value != null && !value.isEmpty()) {
            JDOMExternalizerUtil.writeField(element, name, value);
        }
    }

    // ---- Accessors -------------------------------------------------------------

    public String getSuitePath() {
        return suitePath;
    }

    public void setSuitePath(String value) {
        this.suitePath = value == null ? "" : value;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String value) {
        this.testName = value == null ? "" : value;
    }

    public String getIncludeTags() {
        return includeTags;
    }

    public void setIncludeTags(String value) {
        this.includeTags = value == null ? "" : value;
    }

    public String getExcludeTags() {
        return excludeTags;
    }

    public void setExcludeTags(String value) {
        this.excludeTags = value == null ? "" : value;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String value) {
        this.outputDirectory = value == null ? "" : value;
    }

    public String getExtraArguments() {
        return extraArguments;
    }

    public void setExtraArguments(String value) {
        this.extraArguments = value == null ? "" : value;
    }

    public String getInterpreterPath() {
        return interpreterPath;
    }

    public void setInterpreterPath(String value) {
        this.interpreterPath = value == null ? "" : value;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String value) {
        this.workingDirectory = value == null ? "" : value;
    }

    public boolean isDebugLogLevel() {
        return debugLogLevel;
    }

    public void setDebugLogLevel(boolean value) {
        this.debugLogLevel = value;
    }
}
