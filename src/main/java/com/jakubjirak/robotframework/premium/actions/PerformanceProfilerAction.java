package com.jakubjirak.robotframework.premium.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jakubjirak.robotframework.premium.PremiumFeatureService;
import org.jetbrains.annotations.NotNull;

public class PerformanceProfilerAction extends AnAction {
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        PremiumFeatureService service = PremiumFeatureService.getInstance(project);
        
        if (service.checkFeatureAccess("performance_profiling", "Performance Profiler")) {
            // Open performance profiler
            // TODO: Implement performance profiler UI
        }
    }
}
