package com.jakubjirak.robotframework.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class WebInspectorToolWindowFactory implements ToolWindowFactory {
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        WebInspectorContent content = new WebInspectorContent(project);
        Content windowContent = ContentFactory.getInstance()
            .createContent(content.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(windowContent);
    }
    
    private static class WebInspectorContent {
        private final JPanel contentPanel;
        
        public WebInspectorContent(Project project) {
            contentPanel = new JPanel();
            JLabel label = new JLabel("<html><h2>Web Inspector - FREE Feature!</h2>" +
                "<p>Visual element selection and locator generation</p>" +
                "<p>• Click elements on web pages</p>" +
                "<p>• Generate CSS and XPath selectors automatically</p>" +
                "<p>• Validate locators against live pages</p></html>");
            contentPanel.add(label);
        }
        
        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}
