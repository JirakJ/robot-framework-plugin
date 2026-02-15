package com.jakubjirak.robotframework.ui.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class OpenReplAction extends AnAction {
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        Messages.showInfoMessage(project,
            "Interactive REPL Console - FREE Feature!\n\n" +
            "Test keywords instantly without creating full test files.\n" +
            "Perfect for learning and experimentation!",
            "Robot Framework REPL");
    }
}
