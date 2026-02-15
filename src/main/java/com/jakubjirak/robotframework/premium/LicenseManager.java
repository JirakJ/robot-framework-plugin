package com.jakubjirak.robotframework.premium;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "RobotFrameworkLicenseManager",
    storages = @Storage("robotFrameworkLicense.xml")
)
public class LicenseManager implements PersistentStateComponent<LicenseManager> {
    
    public enum LicenseTier {
        FREE,
        PROFESSIONAL,
        TEAM,
        ENTERPRISE
    }
    
    public String licenseKey = "";
    public String licenseEmail = "";
    public LicenseTier currentTier = LicenseTier.FREE;
    public long licenseExpiryTimestamp = 0L;
    
    public static LicenseManager getInstance() {
        return ApplicationManager.getApplication().getService(LicenseManager.class);
    }
    
    @Nullable
    @Override
    public LicenseManager getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull LicenseManager state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    public boolean isPremium() {
        return currentTier != LicenseTier.FREE && !isExpired();
    }
    
    public boolean isTeamOrHigher() {
        return (currentTier == LicenseTier.TEAM || currentTier == LicenseTier.ENTERPRISE) && !isExpired();
    }
    
    public boolean isEnterprise() {
        return currentTier == LicenseTier.ENTERPRISE && !isExpired();
    }
    
    public boolean isExpired() {
        if (licenseExpiryTimestamp == 0) return false;
        return System.currentTimeMillis() > licenseExpiryTimestamp;
    }
    
    public void activateLicense(String key, String email, LicenseTier tier, long expiryTimestamp) {
        this.licenseKey = key;
        this.licenseEmail = email;
        this.currentTier = tier;
        this.licenseExpiryTimestamp = expiryTimestamp;
    }
    
    public void deactivateLicense() {
        this.licenseKey = "";
        this.licenseEmail = "";
        this.currentTier = LicenseTier.FREE;
        this.licenseExpiryTimestamp = 0L;
    }
    
    public String getTierName() {
        return currentTier.name();
    }
    
    public boolean hasFeature(String featureName) {
        switch (featureName) {
            case "advanced_debugging":
            case "performance_profiling":
            case "code_quality":
            case "enhanced_reporting":
                return isPremium();
                
            case "team_collaboration":
            case "ci_cd_integration":
            case "test_management":
            case "advanced_alerting":
                return isTeamOrHigher();
                
            case "sso_saml":
            case "rbac":
            case "audit_logging":
            case "ai_features":
            case "governance":
            case "dedicated_support":
                return isEnterprise();
                
            default:
                return false;
        }
    }
}
