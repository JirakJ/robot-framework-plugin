package com.jakubjirak.robotframework.language;

import com.intellij.lang.Language;

public class RobotLanguage extends Language {
    public static final RobotLanguage INSTANCE = new RobotLanguage();

    private RobotLanguage() {
        super("RobotFramework");
    }
}
