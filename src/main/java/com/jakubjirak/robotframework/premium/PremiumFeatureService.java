package com.jakubjirak.robotframework.premium;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PremiumFeatureService {
    
    private final Project project;
    
    public PremiumFeatureService(Project project) {
        this.project = project;
    }
    
    public static PremiumFeatureService getInstance(Project project) {
        return project.getService(PremiumFeatureService.class);
    }
    
    public boolean checkFeatureAccess(String featureName, String featureDisplayName) {
        LicenseManager licenseManager = LicenseManager.getInstance();
        
        if (licenseManager.hasFeature(featureName)) {
            return true;
        }
        
        showUpgradeNotification(featureDisplayName, getRequiredTier(featureName));
        return false;
    }
    
    private void showUpgradeNotification(String featureName, String requiredTier) {
        Notification notification = new Notification(
            "Robot Framework",
            "Premium Feature: " + featureName,
            "This feature requires " + requiredTier + " tier. Upgrade now to unlock advanced capabilities!",
            NotificationType.INFORMATION
        );
        
        notification.addAction(new NotificationAction("Upgrade Now") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                // TODO: Open upgrade dialog or browser to pricing page
                notification.expire();
            }
        });
        
        notification.addAction(new NotificationAction("Learn More") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                // TODO: Open feature comparison page
                notification.expire();
            }
        });
        
        Notifications.Bus.notify(notification, project);
    }
    
    private String getRequiredTier(String featureName) {
        LicenseManager licenseManager = LicenseManager.getInstance();
        
        if (licenseManager.hasFeature(featureName)) {
            return "Already activated";
        }
        
        // Check which tier is required
        LicenseManager.LicenseTier originalTier = licenseManager.currentTier;
        
        licenseManager.currentTier = LicenseManager.LicenseTier.PROFESSIONAL;
        if (licenseManager.hasFeature(featureName)) {
            licenseManager.currentTier = originalTier;
            return "Professional ($19/month)";
        }
        
        licenseManager.currentTier = LicenseManager.LicenseTier.TEAM;
        if (licenseManager.hasFeature(featureName)) {
            licenseManager.currentTier = originalTier;
            return "Team ($49/month per user)";
        }
        
        licenseManager.currentTier = originalTier;
        return "Enterprise (Custom pricing)";
    }
    
    public void trackFeatureUsage(String featureName) {
        // TODO: Implement analytics tracking for feature usage
        // This helps identify which free features drive premium conversion
    }
}
