package com.jakubjirak.robotframework.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RobotToolWindowFactory implements ToolWindowFactory {
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        RobotToolWindowContent content = new RobotToolWindowContent(project);
        Content windowContent = ContentFactory.getInstance()
            .createContent(content.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(windowContent);
    }
    
    private static class RobotToolWindowContent {
        private final JPanel contentPanel;
        
        public RobotToolWindowContent(Project project) {
            contentPanel = new JPanel();
            contentPanel.add(new JLabel("Robot Framework - Test Results"));
        }
        
        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}
