/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Starts {@code python -m robot} for a {@link RobotRunConfiguration}.
 *
 * <p>The arguments come from {@link RobotCommandBuilder} as a list and are handed
 * to {@link GeneralCommandLine} one by one, so nothing is ever interpreted by a
 * shell — a suite path or test name containing spaces or shell metacharacters is
 * passed through verbatim.</p>
 */
public class RobotCommandLineState extends CommandLineState {

    private final RobotRunConfiguration configuration;

    protected RobotCommandLineState(@NotNull ExecutionEnvironment environment,
                                    @NotNull RobotRunConfiguration configuration) {
        super(environment);
        this.configuration = configuration;
        setConsoleBuilder(TextConsoleBuilderFactory.getInstance()
                .createBuilder(environment.getProject()));
    }

    /** The command that will be executed. Exposed for the run configuration UI and tests. */
    public List<String> buildCommand() {
        return RobotCommandBuilder.build(configuration.toParams());
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        List<String> command = buildCommand();

        GeneralCommandLine commandLine = new GeneralCommandLine(command)
                .withWorkDirectory(configuration.resolveWorkingDirectory())
                .withCharset(StandardCharsets.UTF_8);
        // Robot writes UTF-8; without this Python may pick a narrow console codec.
        commandLine.withEnvironment("PYTHONIOENCODING", "utf-8");

        ProcessHandler processHandler = ProcessHandlerFactory.getInstance()
                .createColoredProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler);
        return processHandler;
    }
}
