package com.jakubjirak.robotframework.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.jakubjirak.robotframework.language.RobotSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Color settings page for Robot Framework syntax highlighting.
 * Allows users to customize colors in Settings > Editor > Color Scheme > Robot Framework.
 */
public class RobotColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Section Header", RobotSyntaxHighlighter.SECTION_HEADER),
            new AttributesDescriptor("Comment", RobotSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Keyword", RobotSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Setting", RobotSyntaxHighlighter.SETTING),
            new AttributesDescriptor("Variable", RobotSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("String", RobotSyntaxHighlighter.STRING),
            new AttributesDescriptor("Number", RobotSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Operator", RobotSyntaxHighlighter.OPERATOR),
            new AttributesDescriptor("Parameter", RobotSyntaxHighlighter.PARAMETER),
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
        return "# Robot Framework Example\n" +
               "*** Settings ***\n" +
               "Library    SeleniumLibrary\n" +
               "Resource   common.resource\n" +
               "\n" +
               "*** Variables ***\n" +
               "${USERNAME}    admin\n" +
               "${PASSWORD}    secret123\n" +
               "@{BROWSERS}    chrome    firefox\n" +
               "\n" +
               "*** Test Cases ***\n" +
               "Login Test\n" +
               "    [Documentation]    Test user login\n" +
               "    [Tags]    smoke    login\n" +
               "    Open Browser    https://example.com    chrome\n" +
               "    Input Text    id:username    ${USERNAME}\n" +
               "    Input Text    id:password    ${PASSWORD}\n" +
               "    Click Button    id:login\n" +
               "    \n" +
               "*** Keywords ***\n" +
               "Custom Keyword With ${arg}\n" +
               "    [Arguments]    ${value}\n" +
               "    Log    ${arg} = ${value}\n" +
               "    Should Be Equal    ${value}    expected\n";
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

