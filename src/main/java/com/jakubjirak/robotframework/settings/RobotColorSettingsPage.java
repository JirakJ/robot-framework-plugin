package com.jakubjirak.robotframework.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.jakubjirak.robotframework.editor.RobotSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Color settings page for Robot Framework syntax highlighting.
 * Allows users to customize colors in Settings &gt; Editor &gt; Color Scheme &gt; Robot Framework.
 */
public class RobotColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Section header", RobotSyntaxHighlighter.SECTION_HEADER),
            new AttributesDescriptor("Comment", RobotSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Built-in keyword", RobotSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Control structure", RobotSyntaxHighlighter.CONTROL_FLOW),
            new AttributesDescriptor("Setting", RobotSyntaxHighlighter.SETTING),
            new AttributesDescriptor("Variable", RobotSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("Variable type", RobotSyntaxHighlighter.VARIABLE_TYPE),
            new AttributesDescriptor("Embedded argument", RobotSyntaxHighlighter.EMBEDDED_ARGUMENT),
            new AttributesDescriptor("Tag", RobotSyntaxHighlighter.TAG),
            new AttributesDescriptor("String", RobotSyntaxHighlighter.STRING),
            new AttributesDescriptor("Identifier", RobotSyntaxHighlighter.IDENTIFIER),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return com.intellij.icons.AllIcons.FileTypes.Config;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new RobotSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "# Robot Framework 7.4 example\n"
                + "*** Settings ***\n"
                + "Library      SeleniumLibrary    AS    Browser\n"
                + "Resource     common.resource\n"
                + "Test Tags    smoke\n"
                + "\n"
                + "*** Variables ***\n"
                + "${USERNAME}              admin\n"
                + "${RETRIES: int}          3\n"
                + "@{BROWSERS: str}         chrome    firefox\n"
                + "${PASSWORD: Secret}      %{APP_PASSWORD}\n"
                + "\n"
                + "*** Test Cases ***\n"
                + "Login Test\n"
                + "    [Documentation]    Test user login\n"
                + "    [Tags]    login    -smoke\n"
                + "    GROUP    Sign in\n"
                + "        Open Browser    https://example.com    chrome\n"
                + "        Input Text      id:username    ${USERNAME}\n"
                + "    END\n"
                + "    WHILE    ${RETRIES} > 0    limit=10x\n"
                + "        VAR    ${RETRIES}    ${{ $RETRIES - 1 }}\n"
                + "    END\n"
                + "\n"
                + "*** Keywords ***\n"
                + "Move ${distance: int} Meters\n"
                + "    [Arguments]    ${speed: float}=1.0\n"
                + "    TRY\n"
                + "        Should Be Equal    ${distance}    10\n"
                + "    EXCEPT    AS    ${error}\n"
                + "        Log    ${error}\n"
                + "    END\n"
                + "    RETURN    ${distance}\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Robot Framework";
    }
}
