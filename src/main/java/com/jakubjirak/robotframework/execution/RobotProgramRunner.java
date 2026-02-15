package com.jakubjirak.robotframework.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class RobotProgramRunner implements ProgramRunner {

    private static final String RUNNER_ID = "RobotFrameworkRunner";

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
               profile instanceof RobotRunConfiguration;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        RunProfileState state = environment.getState();
        if (state == null) {
            return;
        }

        ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
        if (executionResult == null) {
            return;
        }

        RunContentDescriptor descriptor = new RunContentDescriptor(
                executionResult.getExecutionConsole(),
                executionResult.getProcessHandler(),
                executionResult.getExecutionConsole().getComponent(),
                environment.getRunProfile().getName()
        );

        RunContentManager.getInstance(environment.getProject()).showRunContent(
                environment.getExecutor(),
                descriptor
        );
    }
}
