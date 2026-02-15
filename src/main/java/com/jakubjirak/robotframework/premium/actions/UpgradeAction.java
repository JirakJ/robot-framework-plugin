package com.jakubjirak.robotframework.premium.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jakubjirak.robotframework.premium.LicenseManager;
import org.jetbrains.annotations.NotNull;

public class UpgradeAction extends AnAction {
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        LicenseManager licenseManager = LicenseManager.getInstance();
        
        String message = String.format(
            "Current Tier: %s\n\n" +
            "Upgrade to unlock premium features:\n\n" +
            "Professional ($19/month):\n" +
            "• Advanced debugging with conditional breakpoints\n" +
            "• Performance profiling and optimization\n" +
            "• Code quality tools (Robocop, auto-formatting)\n" +
            "• Enhanced reporting with trend analysis\n\n" +
            "Team ($49/month per user, min 5 users):\n" +
            "• All Professional features\n" +
            "• Team collaboration and shared libraries\n" +
            "• CI/CD integration (GitLab, GitHub Actions, Azure DevOps)\n" +
            "• Test Management Systems integration\n\n" +
            "Enterprise (Custom pricing):\n" +
            "• All Team features\n" +
            "• SSO/SAML, RBAC, Audit logging\n" +
            "• AI-powered test generation and self-healing\n" +
            "• Dedicated support with 4-hour SLA\n\n" +
            "Visit our website to upgrade!",
            licenseManager.getTierName()
        );
        
        Messages.showInfoMessage(project, message, "Upgrade to Premium");
    }
}
